package com.github.hexa.pvpbot.v1_8_R3;


import net.minecraft.server.v1_8_R3.*;

public class BotConnection extends PlayerConnection {
    public BotConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {
    }

}
