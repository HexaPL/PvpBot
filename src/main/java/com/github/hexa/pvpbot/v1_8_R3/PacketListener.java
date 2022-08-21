package com.github.hexa.pvpbot.v1_8_R3;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketListener extends ChannelDuplexHandler {

    private EntityPlayer player;
    private boolean active = false;

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
        super.channelRead(ctx, msg);
    }

    // Outgoing
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
