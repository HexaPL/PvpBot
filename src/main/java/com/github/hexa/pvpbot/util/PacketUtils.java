package com.github.hexa.pvpbot.util;

import net.minecraft.server.v1_16_R3.*;

public class PacketUtils {

    public static void sendPacketNearby(EntityPlayer player, Packet packet) {
        ChunkProviderServer chunkproviderserver = ((WorldServer)player.world).getChunkProvider();
        chunkproviderserver.broadcast(player, packet);
    }

}
