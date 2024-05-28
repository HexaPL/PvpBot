package com.github.hexa.pvpbot.v1_16_R3;

import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PlayerConnection;

public class BotConnection extends PlayerConnection {

    private EntityPlayerBot bot;

    public BotConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayerBot bot) {
        super(minecraftserver, networkmanager, bot);
        this.bot = bot;
    }

    @Override
    public void sendPacket(Packet<?> packet) {

    }

}
