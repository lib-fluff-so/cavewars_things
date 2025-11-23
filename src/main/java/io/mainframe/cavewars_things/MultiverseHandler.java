package io.mainframe.cavewars_things;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CloneWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
import org.mvplugins.multiverse.core.world.reasons.DeleteFailureReason;
import org.mvplugins.multiverse.external.vavr.control.Option;

import java.util.List;

public class MultiverseHandler {
    static final MultiverseCoreApi multiverseCore = MultiverseCoreApi.get();
    static final WorldManager worldManager = multiverseCore.getWorldManager();

    public static int getFreeRoomNumber() {
        //Get Room ID
        int roomNumber = 0;
        while (worldManager.getWorld("cavewars_room_"+roomNumber).getOrNull() != null) {
            roomNumber++;
        }
        return roomNumber;
    }

    public static String getRoomIdFromNumber(int roomNumber) {return "cavewars_room_"+roomNumber;}

    public static Attempt<LoadedMultiverseWorld, CloneFailureReason> createRoomFromTemplate(String roomId, String template) {
        Option<LoadedMultiverseWorld> loadedTemplateWorld = worldManager.getLoadedWorld(template);
        return worldManager.cloneWorld(CloneWorldOptions.fromTo(loadedTemplateWorld.get(), roomId));
    }

    public static Attempt<LoadedMultiverseWorld, CloneFailureReason> createRoomFromTemplate(String roomId, LoadedMultiverseWorld template) {
        return worldManager.cloneWorld(CloneWorldOptions.fromTo(template, roomId));
    }

    public static Attempt<LoadedMultiverseWorld, CloneFailureReason> createRoomFromTemplate(String roomId, MultiverseWorld template) {
        return worldManager.cloneWorld(CloneWorldOptions.fromTo(worldManager.getLoadedWorld(template.getName()).get(),
                roomId));
    }

    public static Attempt<LoadedMultiverseWorld, CloneFailureReason> newRoom(CommandSender sender, String template, boolean chatVerbose) {
        //Get Room ID
        String roomId = getRoomIdFromNumber(getFreeRoomNumber());

        //Create Room, if there is a such template
        if (worldManager.getLoadedWorld(template).getOrNull() == null) {sender.sendMessage("No such template: "+template);
            return null;}

        if (chatVerbose) sender.sendMessage("Creating Room with ID "+roomId+" from "+template+"...");

        Attempt<LoadedMultiverseWorld, CloneFailureReason> cloneResult =
                createRoomFromTemplate(roomId, template);

        if (chatVerbose) {
            if (cloneResult.getOrNull() != null) {
                sender.sendMessage("Successfully created new Room: "+cloneResult.get().getName());
            } else {
                sender.sendMessage("Creation of new Room failed: "+cloneResult.getFailureMessage().formatted());
            }
        }

        return cloneResult;
    }

    public static Attempt<String, DeleteFailureReason> deleteRoom(CommandSender sender, String roomId, boolean chatVerbose) {
        
        if (worldManager.getLoadedWorld(roomId).getOrNull() == null) {
            if (chatVerbose) sender.sendMessage("No such room: "+roomId);
            return null;
        }
        Attempt<String, DeleteFailureReason> deleteResult =
                worldManager.deleteWorld(DeleteWorldOptions.world(worldManager.getLoadedWorld(roomId).get()));
        if (chatVerbose) {
            if (deleteResult.getOrNull() != null) {
                sender.sendMessage("Done!");
            } else {
                sender.sendMessage("Deletion of room failed with error: "+deleteResult.getFailureMessage().formatted());
            }
        }
        return deleteResult;
    }

    public static List<Player> selectPlayersBySelector(CommandSender sender, String selector, boolean chatVerbose) {
        //Keep in mind that even if the selector returned no players, the function still returns empty list
        List<Player> targets = List.of();
        try {
            targets = Bukkit.selectEntities(sender, selector).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .toList();
        } catch (IllegalArgumentException e) {
            //Error handling.
            sender.sendMessage("The selector is invalid: "+selector);
            if (chatVerbose) return null;
        }

        //Error handling.
        if (targets.isEmpty() && chatVerbose) {
            sender.sendMessage("The selector returned no players: "+selector);
        }
        return targets;
    }
}