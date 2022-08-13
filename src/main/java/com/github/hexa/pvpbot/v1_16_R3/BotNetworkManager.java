package com.github.hexa.pvpbot.v1_16_R3;

import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.NetworkManager;

public class BotNetworkManager extends NetworkManager {
    public BotNetworkManager(EnumProtocolDirection enumprotocoldirection) {
        super(enumprotocoldirection);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

}
