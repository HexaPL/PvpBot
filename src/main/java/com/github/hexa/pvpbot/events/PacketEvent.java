package com.github.hexa.pvpbot.events;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.server.v1_16_R3.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PacketEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;

    private Packet<?> packet;
    private Player player;




    public PacketEvent(Packet<?> packet, Player player) {
        super(true);
        this.packet = packet;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }

    public Player getPlayer() {
        return player;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public static class Incoming extends PacketEvent {

        public Incoming(Packet<?> packet, Player player) {
            super(packet, player);
        }

    }

    public static class Outgoing extends PacketEvent {

        public Outgoing(Packet<?> packet, Player player) {
            super(packet, player);
        }

    }

}
