package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.*;
import com.github.hexa.pvpbot.ai.gamemode.GameMode;
import com.github.hexa.pvpbot.ai.gamemode.GameModeManager;
import com.github.hexa.pvpbot.skins.Skin;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BotManagerImpl extends AbstractBotManager {

    @Override
    public Bot createBot(String name, GameMode gameMode, Player owner) {
        return createBot(name, gameMode, owner, owner.getLocation());
    }

    @Override
    public Bot createBot(String name, GameMode gameMode, Player owner, Location location) {

        MinecraftServer nmsServer = ((CraftServer) Bukkit.getServer()).getServer();
        World world = location.getWorld();
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        PlayerInteractManager interactManager = new PlayerInteractManager(worldServer);
        EntityPlayerBot bot = new EntityPlayerBot(name, ((CraftPlayer) owner).getHandle(), nmsServer, worldServer, profile, interactManager);
        bot.playerConnection = new BotConnection(nmsServer, new BotNetworkManager(EnumProtocolDirection.CLIENTBOUND), bot);

        Skin.setForGameProfile(profile, Skin.getFromPlayer(owner));
        DataWatcher watcher = bot.getDataWatcher();
        watcher.set(new DataWatcherObject<>(16,DataWatcherRegistry.a), (byte) 127);
        PacketPlayOutEntityMetadata skinLayer = new PacketPlayOutEntityMetadata(bot.getId(), watcher, true);

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
            connection.sendPacket(skinLayer);
        }


        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
                connection.sendPacket(playerInfoRemove);
            }
        }, 3L);

        GameModeManager.setGameMode(bot, gameMode);
        botUUIDs.put(bot.getUniqueID(), bot);
        bots.put(name, bot);

        return bot;

    }

    @Override
    public void removeBot(Bot bot) {
        EntityPlayerBot nmsBot = getNmsBot(bot);
        WorldServer worldServer = nmsBot.getWorldServer();
        worldServer.removePlayer(nmsBot);
        bots.remove(bot.getBotName());
        botUUIDs.remove(((EntityPlayerBot) bot).getUniqueID());
    }

    public static EntityPlayerBot getNmsBot(Bot bot) {
        return (EntityPlayerBot) bot;
    }

}
