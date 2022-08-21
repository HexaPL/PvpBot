package com.github.hexa.pvpbot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public interface BotManager {

    public HashMap<String, Bot> bots = new HashMap<>();
    public HashMap<UUID, Bot> botUUIDs = new HashMap<>();

    public Bot createBot(String name, World world, Location location, Player owner);

    public void removeBot(Bot bot);

    public boolean botExists(String name);

    public boolean botExists(UUID uuid);

    public Bot getBotByName(String name);

    public Bot getBotByUUID(UUID uuid);

    public boolean entityIsBot(Entity entity);

}
