package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.ai.gamemode.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractBotManager {

    public HashMap<String, Bot> bots = new HashMap<>();
    public HashMap<UUID, Bot> botUUIDs = new HashMap<>();

    public abstract Bot createBot(String name, GameMode gameMode, Player owner);

    public abstract Bot createBot(String name, GameMode gameMode, Player owner, Location location);

    public abstract void removeBot(Bot bot);

    public boolean botExists(String name) {
        return bots.containsKey(name);
    }

    public boolean botExists(UUID uuid) {
        return botUUIDs.containsKey(uuid);
    }

    public Bot getBotByName(String name) {
        if (!(bots.containsKey(name))) {
            return null;
        }
        return bots.get(name);
    }

    public Bot getBotByUUID(UUID uuid) {
        if (!(botUUIDs.containsKey(uuid))) {
            return null;
        }
        return botUUIDs.get(uuid);
    }

    public boolean entityIsBot(Entity entity) {
        return botExists(entity.getUniqueId());
    }

}
