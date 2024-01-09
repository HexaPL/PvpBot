package com.github.hexa.pvpbot.v1_16_R3;

import com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class SkinNMS {

    public static GameProfile getGameProfile(Player player) {
        return ((CraftPlayer) player).getHandle().getProfile();
    }

}
