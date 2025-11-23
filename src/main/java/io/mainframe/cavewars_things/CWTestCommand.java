package io.mainframe.cavewars_things;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
import java.util.ArrayList;
import java.util.List;

public class CWTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args) {
        if (args.length == 0) {sender.sendMessage("Usage: /cwtest test"); return true;}
        String action = args[0].toLowerCase();

        switch (action) {
            case "test" -> {
                long startTime = System.currentTimeMillis();
                List<MultiverseWorld> worlds = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    Attempt<LoadedMultiverseWorld, CloneFailureReason> roomResult = MultiverseHandler.newRoom(sender,
                            "cavewars_map_TheOrigins_template", true);
                    //Not continue if there was an error. It was already logged to the player's chat.
                    if (roomResult == null || roomResult.getOrNull() == null) return true;
                    worlds.add(roomResult.getOrNull());
                }
                long endTime = System.currentTimeMillis();
                long passedTicks = (endTime - startTime) / 1000 * 20;
                double ticksPerSecond = 1000.0 / ((double) (endTime - startTime) / passedTicks);
                sender.sendMessage("Created 10 worlds in "+passedTicks / 20+" with average TPS of "+ticksPerSecond);

            }
            default -> sender.sendMessage("Unknown action: "+action);
        }

        return true;
    }
}
