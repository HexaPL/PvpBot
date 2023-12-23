package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.PvpBotPlugin;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;

public class BotConnection extends PlayerConnection {

    private EntityPlayerBot bot;

    public BotConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayerBot bot) {
        super(minecraftserver, networkmanager, bot);
        this.bot = bot;
    }

    @Override
    public void sendPacket(Packet<?> packet) {
        if (!(packet instanceof PacketPlayOutEntityVelocity)) {
            return;
        }
        if (!bot.hasPendingKnockback()) {
            return;
        }
        bot.clearPendingKnockback();
        Vec3D knockback = bot.getMot();
        int tickDelay = Math.round((bot.getAI().getPing() / 2F) / 50F);
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> {
            bot.setMot(knockback);
            // Jump reset
            boolean jumpReset = bot.getAI().getProperties().getBoolean("jumpReset");
            if (jumpReset && bot.isOnGround()) {
                bot.jump();
            }
        }, 1 + tickDelay);
    }

}
