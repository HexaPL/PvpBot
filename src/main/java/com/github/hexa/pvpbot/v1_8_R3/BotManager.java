package com.github.hexa.pvpbot.v1_8_R3;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BotManager implements com.github.hexa.pvpbot.BotManager {

    public static HashMap<String, EntityPlayerBot> bots = new HashMap<>();
    public static HashMap<UUID, EntityPlayerBot> botUUIDs = new HashMap<>();

    @Override
    public Bot createBot(String name, World world, Location location, Player owner) {

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer);
        EntityPlayerBot bot = new EntityPlayerBot(name, ((CraftPlayer) owner).getHandle(), nmsServer, worldServer, profile, interactManager);
        bot.playerConnection = new BotConnection(nmsServer, new BotNetworkManager(EnumProtocolDirection.CLIENTBOUND), bot);

        bot.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        worldServer.addEntity(bot);

        bot.getBukkitEntity().setNoDamageTicks(0);

        PacketPlayOutPlayerInfo playerInfoAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, bot);
        PacketPlayOutNamedEntitySpawn namedEntitySpawn = new PacketPlayOutNamedEntitySpawn(bot);
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(bot, (byte) ((location.getYaw() * 256f) / 360f));
        PacketPlayOutPlayerInfo playerInfoRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, bot);

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            connection.sendPacket(playerInfoAdd);
            connection.sendPacket(namedEntitySpawn);
            connection.sendPacket(headRotation);

        }


        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                    connection.sendPacket(playerInfoRemove);
                }
            }
        }, 1L);

        botUUIDs.put(bot.getUniqueID(), bot);
        bots.put(name, bot);

        return bot;

    }

    @Override
    public void removeBot(Bot bot) {

        EntityPlayerBot nmsBot = getBotHandle(bot);
        WorldServer worldServer = ((CraftWorld) nmsBot.getBukkitEntity().getWorld()).getHandle();
        worldServer.removeEntity(nmsBot);
        bots.remove(bot.getBotName());

    }

    @Override
    public boolean botExists(String name) {
        return bots.containsKey(name);
    }

    @Override
    public boolean botExists(UUID uuid) {
        return botUUIDs.containsKey(uuid);
    }

    @Override
    public Bot getBotByName(String name) {
        if (!(bots.containsKey(name))) {
            return null;
        }
        return bots.get(name);
    }

    @Override
    public Bot getBotByUUID(UUID uuid) {
        if (!(botUUIDs.containsKey(uuid))) {
            return null;
        }
        return botUUIDs.get(uuid);
    }

    @Override
    public boolean entityIsBot(Entity entity) {
        return botExists(entity.getUniqueId());
    }

    public static EntityPlayerBot getBotHandle(Bot bot) {
        return (EntityPlayerBot) bot;
    }

}
