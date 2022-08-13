package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BotManager {

    public static HashMap<String, EntityPlayerBot> bots = new HashMap<>();
    public static HashMap<EntityPlayerBot, EntityPlayer> botTarget = new HashMap<>();

    public static EntityPlayerBot createBot(String name, World world, Location location, Player owner) {

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer);
        EntityPlayerBot bot = new EntityPlayerBot(name, ((CraftPlayer) owner).getHandle(), nmsServer, worldServer, profile, interactManager);
        bot.playerConnection = new BotConnection(nmsServer, new BotNetworkManager(EnumProtocolDirection.CLIENTBOUND), bot);

        bot.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        worldServer.addPlayerJoin(bot);

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

        bots.put(name, bot);
        botTarget.put(bot, ((CraftPlayer) owner).getHandle());

        return bot;

    }

    public static void removeBot(EntityPlayerBot bot) {

        WorldServer worldServer = bot.getWorldServer();
        worldServer.removePlayer(bot);
        bots.remove(bot.getBotName());
        botTarget.remove(bot);

    }

    public static EntityPlayerBot getBotByName(String name) {
        if (!(bots.containsKey(name))) {
            return null;
        }
        return bots.get(name);
    }

}
