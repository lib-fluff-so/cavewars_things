package io.mainframe.cavewars_things;

import alepando.dev.dialogapi.DialogAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        DialogAPI.INSTANCE.initialize(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("cwmv")).setExecutor(new CWMVCommand());
        Objects.requireNonNull(getCommand("cwtest")).setExecutor(new CWTestCommand());
        Objects.requireNonNull(getCommand("cwui")).setExecutor(new CWUICommand());
        getLogger().info("CaveWars Things loaded.");
    }
}
