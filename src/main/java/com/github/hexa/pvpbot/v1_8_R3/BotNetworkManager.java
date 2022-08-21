package com.github.hexa.pvpbot.v1_8_R3;

import net.minecraft.server.v1_8_R3.EnumProtocolDirection;
import net.minecraft.server.v1_8_R3.NetworkManager;

public class BotNetworkManager extends NetworkManager {
    public BotNetworkManager(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
    }

    @Override
    public boolean g() {
        return true;
    } // isConnected()

}
