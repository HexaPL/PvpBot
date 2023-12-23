package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.events.PacketEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketListener extends ChannelDuplexHandler {

    private EntityPlayer player;

    public PacketListener(EntityPlayer player) {
        this.player = player;
    }

    public static void addPlayer(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = nmsPlayer.playerConnection.networkManager.channel;
        PacketListener packetListener = new PacketListener(nmsPlayer);
        channel.pipeline().addBefore("packet_handler", player.getName(), packetListener);
    }

    public static void removePlayer(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = nmsPlayer.playerConnection.networkManager.channel;
        if (channel.pipeline().get(player.getName()) != null) {
            channel.pipeline().remove(player.getName());
        }
    }

    public static PacketListener getFor(Player player) {
        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        Channel channel = nmsPlayer.playerConnection.networkManager.channel;
        return (PacketListener) channel.pipeline().get(player.getName());
    }

    // Incoming
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            PacketEvent.Incoming event = new PacketEvent.Incoming(packet, this.player.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            super.channelRead(ctx, event.getPacket());
            return;
        }
        super.channelRead(ctx, msg);
    }

    // Outgoing
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Packet) {
            Packet packet = (Packet) msg;
            PacketEvent.Outgoing event = new PacketEvent.Outgoing(packet, this.player.getBukkitEntity());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            super.write(ctx, event.getPacket(), promise);
            return;
        }
        super.write(ctx, msg, promise);
    }
}
