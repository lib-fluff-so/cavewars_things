package io.mainframe.cavewars_things;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.reasons.CloneFailureReason;
import static io.mainframe.cavewars_things.MultiverseHandler.*;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

            //if <players> starts with *, it is treated as json array: [player1, player2, player3...]
            case "preparefight" -> {
                if (args.length != 3) {sender.sendMessage("Usage: /cwmv preparefight <players> <template>"); return true;}
                String selector = args[1];
                String template = args[2];

                List<Player> targets;

                if (selector.startsWith("*")) {
                    targets = new ArrayList<>();
                    List<String> strings = new Gson().fromJson(selector.substring(1),
                            new TypeToken<List<String>>(){}.getType());
                    sender.sendMessage(strings.toString());
                    for (String i : strings) {
                        targets.add(Bukkit.getPlayer(i));
                    }
                } else {
                    targets = selectPlayersBySelector(sender, selector, true);
                }

                //Not continue if there was an error. The error was already logged into sender's chat.
                if (targets == null) return true; if (targets.isEmpty()) return true;

                Attempt<LoadedMultiverseWorld, CloneFailureReason> room = newRoom(sender, template, true);

                //Not continue if there was an error. It was already logged to the player's chat.
                if (room == null || room.getOrNull() == null) return true;

                String roomId = room.getOrNull().getName();
                Location spawnLocation = worldManager.getWorld(roomId).get().getSpawnLocation();

                multiverseCore.getSafetyTeleporter().to(spawnLocation).checkSafety(false).teleport(targets);

                new BukkitRunnable() {
                    public void run() {
                        for (Player player: targets) {
                            for (PotionEffect effect : player.getActivePotionEffects()){
                                player.removePotionEffect(effect.getType());
                            }
                            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 255));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 200, 255));
                            player.getInventory().clear();
                            player.updateInventory();
                            player.setGameMode(GameMode.ADVENTURE);
                        }

                        MultiverseWorld world = worldManager.getWorld(roomId).get();
                        Objects.requireNonNull(Bukkit.getWorld(roomId)).getBlockAt(-136, 69, 442).setType(Material.REDSTONE_BLOCK);
                        //chests (shuffle)
                        new BukkitRunnable() {
                            int count = 8;
                            @Override
                            public void run() {
                                count -= 1;
                                final Title title = getTitle();
                                for (Player player: targets) {
                                    player.showTitle(title);
                                    if (count == 0) {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
                                    } else {
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 0.6f);
                                    }
                                }
                                if (count <= 0) {
                                    cancel();
                                    for (Player player: targets) {
                                        for (PotionEffect effect : player.getActivePotionEffects()){
                                            player.removePotionEffect(effect.getType());
                                        }
                                        player.setGameMode(GameMode.SURVIVAL);
                                        player.teleport(world.getSpawnLocation());
                                        Objects.requireNonNull(Bukkit.getWorld(roomId)).getBlockAt(-136, 69, 442).setType(Material.AIR);
                                        player.getInventory().clear();
                                        player.updateInventory();
                                        player.getInventory().setBoots(ItemStack.of(Material.IRON_BOOTS));
                                        player.getInventory().setChestplate(ItemStack.of(Material.IRON_CHESTPLATE));
                                        player.getInventory().setLeggings(ItemStack.of(Material.IRON_LEGGINGS));
                                        player.getInventory().setHelmet(ItemStack.of(Material.IRON_BLOCK));
                                        player.getInventory().setItem(0, ItemStack.of(Material.IRON_SWORD));
                                        player.updateInventory();
                                    }
                                }
                            }

                            @NotNull
                            private Title getTitle() {
                                NamedTextColor textColor;
                                if (count == 3) {
                                    textColor = NamedTextColor.GREEN;
                                } else if (count == 2) {
                                    textColor = NamedTextColor.YELLOW;
                                } else if (count == 1) {
                                    textColor = NamedTextColor.RED;
                                } else if (count == 0) {
                                    textColor = NamedTextColor.LIGHT_PURPLE;
                                } else {
                                    textColor = NamedTextColor.WHITE;
                                }
                                final Title.Times times = Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(1400), Duration.ofMillis(300));
                                return Title.title(Component.text(count == 0 ? "СТАРТ!" : String.valueOf(count), textColor), Component.empty(), times);
                            }
                        }.runTaskTimer(getPlugin(Main.class), 0, 15);
                    }
                }.runTaskLater(getPlugin(Main.class), 40);
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