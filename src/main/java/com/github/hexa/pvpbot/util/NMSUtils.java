package com.github.hexa.pvpbot.util;

import org.bukkit.Bukkit;

public class NMSUtils {

    private static String nmsVersion;

    public static String getNMSVersion() {
        if (nmsVersion == null) {
            nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        return nmsVersion;
    }

}
