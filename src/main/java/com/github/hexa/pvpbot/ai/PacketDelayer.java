package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.events.PacketEvent;
import com.github.hexa.pvpbot.v1_16_R3.PacketListener;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PacketDelayer implements Listener {

    private HashMap<Packet, Long> packetQueue;
    private HashSet<Packet> delayedPackets;
    public Player player;
    private PacketEventListener listener;

    public PacketDelayer(Player player) {
        this.player = player;
        this.packetQueue = new HashMap<>();
        this.delayedPackets = new HashSet<>();
        this.listener = new PacketEventListener();
        PvpBotPlugin.getInstance().getServer().getPluginManager().registerEvents(this.listener, PvpBotPlugin.getInstance());
    }

    public void tick() {

        Iterator<Map.Entry<Packet, Long>> iterator = packetQueue.entrySet().iterator();
        ArrayList<Integer> tickDelays = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<Packet, Long> item = iterator.next();
            if (System.currentTimeMillis() - item.getValue() >= 0) {
                Packet packet = item.getKey();
                tickDelays.add((int) (System.currentTimeMillis() - item.getValue()));
                delayedPackets.add(packet);
                ((CraftPlayer) this.player).getHandle().playerConnection.sendPacket(packet);
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
            this.packetDelayer = null;
        }

    }

    public class PacketEventListener implements Listener {

        @EventHandler
        public void onOutgoingPacket(PacketEvent.Outgoing event) {
            if (delayedPackets.contains(event.getPacket())) {
                delayedPackets.remove(event.getPacket());
                return;
            }
            if (!(event.getPacket() instanceof PacketPlayOutChat)) {
                long l = System.currentTimeMillis();
                Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> packetQueue.put(event.getPacket(), l));
                event.setCancelled(true);
            }
        }

    }

}
