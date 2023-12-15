package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.command.BotCommand;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpBotPlugin extends JavaPlugin implements Listener {
    public static PvpBotPlugin instance;
    public BotManager botManager;
    public static FileConfiguration CONFIG;
    public static boolean debug = false;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("Registering BotManager for Craftbukkit version v1_16_R3");
        //this.getServer().getPluginManager().registerEvents(new PingHandler.PlayerListener(), this); TODO - ping system
        this.botManager = new com.github.hexa.pvpbot.v1_16_R3.BotManager();
        this.getCommand("bot").setExecutor(new BotCommand());
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    @Override
    public void onDisable() {
    }

    public static PvpBotPlugin getInstance() {
        return instance;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        CONFIG = getConfig();
        CONFIG.options().copyDefaults(true);
        saveConfig();
    }

    public static BotManager getManager() {
        return instance.botManager;
    }

}
