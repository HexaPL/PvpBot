package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.command.BotCommand;
import com.github.hexa.pvpbot.v1_16_R3.PacketListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpBotPlugin extends JavaPlugin implements Listener {
    public static PvpBotPlugin instance;
    public static FileConfiguration CONFIG;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("PvpBot enabled!");
        getServer().getPluginManager().registerEvents(this, this);
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
        CONFIG = getConfig();
        CONFIG.options().copyDefaults(true);
        saveConfig();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PacketListener.addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PacketListener.removePlayer(event.getPlayer());
    }

}
