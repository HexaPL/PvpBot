package com.github.hexa.pvpbot.v1_8_R3;


import com.github.hexa.pvpbot.PvpBotPlugin;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;

public class BotConnection extends PlayerConnection {

    private EntityPlayerBot bot;

    public BotConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayerBot bot) {
        super(minecraftserver, networkmanager, bot);
        this.bot = bot;
    }

    @Override
    public void sendPacket(Packet packet) {
        if (!(packet instanceof PacketPlayOutEntityVelocity)) {
            return;
        }
        if (!bot.hasPendingKnockback()) {
            return;
        }
        bot.clearPendingKnockback();
        Vec3D knockback = bot.getMot();
        int tickDelay = Math.round(bot.getAI().getPing() / 50F);
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> bot.setMot(knockback), 1 + tickDelay);
    }

}
