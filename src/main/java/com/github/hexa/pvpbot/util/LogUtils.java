package com.github.hexa.pvpbot.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class LogUtils {

    public static void warn(String s) {
        Bukkit.getLogger().log(Level.WARNING, "[PvpBot] " + s);
    }

    public static String getTimeString() {
        String timeS = String.valueOf(System.currentTimeMillis());
        return timeS.substring(timeS.length() - 5);
    }

}
