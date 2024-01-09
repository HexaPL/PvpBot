package com.github.hexa.pvpbot.util;

import org.bukkit.Bukkit;

import java.util.logging.Level;

public class LogUtils {

    public static void warn(String s) {
        Bukkit.getLogger().log(Level.WARNING, "[PvpBot] " + s);
    }

}
