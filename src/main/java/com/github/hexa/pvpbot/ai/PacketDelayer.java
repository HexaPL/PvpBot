package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.events.PacketEvent;
import com.github.hexa.pvpbot.v1_16_R3.EntityPlayerBot;
import com.github.hexa.pvpbot.v1_16_R3.PacketListener;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PacketDelayer implements Listener {

    public static HashMap<Player, PacketDelayer> packetDelayers = new HashMap<>();

    private HashMap<Packet<?>, Long> incomingPacketQueue;
    private HashMap<Packet<?>, Long> outgoingPacketQueue;
    public HashSet<Packet<?>> delayedPackets;
    public Player player;
    private PacketEventListener listener;
    private int delay;

    public PacketDelayer(Player player) {
        this.player = player;
        this.delay = 50;
        this.incomingPacketQueue = new HashMap<>();
        this.outgoingPacketQueue = new HashMap<>();
        this.delayedPackets = new HashSet<>();
        this.listener = new PacketEventListener(PacketDirection.OUTGOING);
        PvpBotPlugin.getInstance().getServer().getPluginManager().registerEvents(this.listener, PvpBotPlugin.getInstance());
    }

    public void tick() {
        if (this.listener.direction != PacketDirection.INCOMING) this.tick(PacketDirection.OUTGOING, outgoingPacketQueue);
        if (this.listener.direction != PacketDirection.OUTGOING) this.tick(PacketDirection.INCOMING, incomingPacketQueue);
    }

    private void tick(PacketDirection direction, HashMap<Packet<?>, Long> queue) {
        Iterator<Map.Entry<Packet<?>, Long>> iterator = queue.entrySet().iterator();
        ArrayList<Integer> tickDelays = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Packet<?>, Long> item = iterator.next();
            if (System.currentTimeMillis() - item.getValue() >= this.delay) {
                Packet packet = item.getKey();
                tickDelays.add((int) (System.currentTimeMillis() - item.getValue()));
                delayedPackets.add(packet);
                PlayerConnection connection = ((CraftPlayer) this.player).getHandle().playerConnection;
                if (direction == PacketDirection.OUTGOING) {
                    connection.sendPacket(packet);
                } else {
                    packet.a(connection);
                }
                iterator.remove();
            }
        }
    }

    public static class PlayerJoinListener implements Listener {

        private PacketDelayer packetDelayer;

        @EventHandler
        public void onJoin(PlayerJoinEvent event) {
            Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> {
                this.packetDelayer = new PacketDelayer(event.getPlayer());
                packetDelayers.put(event.getPlayer(), packetDelayer);
                PacketListener.addPlayer(event.getPlayer());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (packetDelayer == null) {
                            Bukkit.getScheduler().cancelTask(getTaskId());
                            return;
                        }
                        packetDelayer.tick();
                    }
                }.runTaskTimer(PvpBotPlugin.getInstance(), 0L, 1L);
            }, 10L);
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            PacketListener.removePlayer(event.getPlayer());
            packetDelayers.remove(event.getPlayer());
            this.packetDelayer = null;
        }

    }

    public class PacketEventListener implements Listener {

        public PacketDirection direction;

        public PacketEventListener(PacketDirection direction) {
            this.direction = direction;
        }

        @EventHandler
        public void onOutgoingPacket(PacketEvent.Outgoing event) {
            /*
            if (this.direction == PacketDirection.INCOMING) {
                return;
            }
            if (delay == 0) {
                return;
            }
            if (delayedPackets.contains(event.getPacket())) {
                delayedPackets.remove(event.getPacket());
                return;
            }
            if (shouldDelayPacket(event.getPacket())) {
                long l = System.currentTimeMillis();
                Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> outgoingPacketQueue.put(event.getPacket(), l));
                event.setCancelled(true);
            }*/
            test2(event);
        }

        @EventHandler
        public void onIncomingPacket(PacketEvent.Incoming event) {

        }

        private void test2(PacketEvent.Outgoing event) {Packet<?> packet = event.getPacket();
            if (shouldBlockPacket(packet)) {
                if (packet instanceof PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook && delayedPackets.contains(packet)) {
                    delayedPackets.remove(packet);
                } else {
                    event.setCancelled(true);
                }
            }

        }

        private void test1(PacketEvent.Incoming event) {
            if (this.direction == PacketDirection.OUTGOING) {
                return;
            }
            if (delay == 0) {
                return;
            }
            if (delayedPackets.contains(event.getPacket())) {
                delayedPackets.remove(event.getPacket());
                return;
            }
            Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> {
                if (event.getPacket() instanceof PacketPlayInFlying) {
                    PacketPlayInFlying movePacket = ((PacketPlayInFlying) event.getPacket());
                    Location loc = new Location(event.getPlayer().getWorld(), movePacket.x, movePacket.y, movePacket.z);
                    EntityPlayerBot.packetLocations.put(event.getPlayer(), loc);
                }
            });
            if (shouldDelayPacket(event.getPacket())) {
                long l = System.currentTimeMillis();
                Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> {
                    incomingPacketQueue.put(event.getPacket(), l);
                });
                event.setCancelled(true);
            }
        }

    }

    private boolean shouldDelayPacket(Packet<?> packet) {
        String packetName = packet.getClass().getSimpleName();
        switch (packetName) {
            case "PacketPlayInPosition":
            case "PacketPlayInLook":
            case "PacketPlayInPositionLook":
            case "PacketPlayOutEntityLook":
            case "PacketPlayOutRelEntityMove":
            case "PacketPlayOutRelEntityMoveLook":
                return true;
            case "PacketPlayOutEntityVelocity":
                return true;
            default:
                return false;
        }
    }

    private boolean shouldBlockPacket(Packet<?> packet) {
        String packetName = packet.getClass().getSimpleName();
        switch (packetName) {
            case "PacketPlayOutEntityLook":
            case "PacketPlayOutRelEntityMove":
            case "PacketPlayOutRelEntityMoveLook":
                return true;
            default:
                return false;
        }
    }

    public enum PacketDirection {
        INCOMING, OUTGOING, BOTH
    }

}
