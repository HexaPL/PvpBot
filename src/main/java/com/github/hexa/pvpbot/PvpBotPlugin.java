package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.command.BotCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpBotPlugin extends JavaPlugin {
    public static PvpBotPlugin instance;
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("PvpBot enabled!");
        this.getCommand("bot").setExecutor(new BotCommand());
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("PvpBot disabled!");
    }

    public static PvpBotPlugin getInstance() {
        return instance;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();
    }
}
