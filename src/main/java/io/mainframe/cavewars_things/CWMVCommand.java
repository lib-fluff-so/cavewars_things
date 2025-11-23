package io.mainframe.cavewars_things;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
import static io.mainframe.cavewars_things.MultiverseHandler.*;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

import java.util.List;

public class CWMVCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args) {
        if (args.length == 0) {sender.sendMessage("Usage: /cwmv <action>"); return true;}
        String action = args[0].toLowerCase();

        switch (action) {
            case "newroom" -> {
                if (args.length != 2) {sender.sendMessage("Usage: /cwmv newroom"); return true;}
                String template = args[1];
                newRoom(sender, template, true);
            }

            case "deleteroom" -> {
                if (args.length != 2) {sender.sendMessage("Usage: /cwmv deleteroom <roomId>"); return true;}
                String roomId = args[1];
                deleteRoom(sender, roomId, true);
                //If there was an error, it was already logged to the player's chat.
            }

            case "preparefight" -> {
                if (args.length != 3) {sender.sendMessage("Usage: /cwmv preparefight <selector> <template>"); return true;}
                String selector = args[1];
                String template = args[2];
                Attempt<LoadedMultiverseWorld, CloneFailureReason> room = newRoom(sender, template, true);

                //Not continue if there was an error. It was already logged to the player's chat.
                if (room == null || room.getOrNull() == null) return true;

                String roomId = room.getOrNull().getName();
                Location spawnLocation = worldManager.getWorld(roomId).get().getSpawnLocation();

                List<Player> targets = selectPlayersBySelector(sender, selector, true);

                //Not continue if there was an error... The error was already logged into sender's chat...
                if (targets == null) return true; if (targets.isEmpty()) return true;

                multiverseCore.getSafetyTeleporter().to(spawnLocation).checkSafety(false).teleport(targets);

                sender.sendMessage("Done!");
            }

            case "stopfight" -> {
                if (args.length != 2) {sender.sendMessage("Usage: /cwmv stopfight <roomId>"); return true;}
                String roomId = args[1];

                MultiverseWorld world = worldManager.getDefaultWorld().get();
                Location spawnLocation = world.getSpawnLocation();
                World selectorRoom = Bukkit.getWorld(roomId);
                if (selectorRoom == null) {
                    sender.sendMessage("No such room found");
                    return true;
                }
                List<Player> players = selectorRoom.getPlayers().stream().toList();
                multiverseCore.getSafetyTeleporter().to(spawnLocation).checkSafety(false).teleport(players);

                sender.sendMessage("The room will be deleted after 2 seconds.");
                //Deleting the room after 2 seconds, so no wacky problems appear
                new BukkitRunnable() {
                    public void run() {
                        deleteRoom(sender, roomId, true);
                        //If there was an error, it was already logged to the player's chat.
                    }
                }.runTaskLater(getPlugin(Main.class), 40);
            }

            case "deleteallrooms" -> {
                if (args.length != 1) {sender.sendMessage("Usage: /cwmv deleteallrooms"); return true;}
                List<MultiverseWorld> cavewarsRooms = worldManager.getWorlds()
                        .stream()
                        .filter(world -> world.getName().startsWith("cavewars_room_"))
                        .toList();
                for (MultiverseWorld world : cavewarsRooms) {
                    deleteRoom(sender, world.getName(), true);
                }
            }

            default -> sender.sendMessage("Unknown action: "+action);
        }

        return true;
    }
}