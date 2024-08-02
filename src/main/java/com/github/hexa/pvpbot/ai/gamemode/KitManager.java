package com.github.hexa.pvpbot.ai.gamemode;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.ai.gamemode.kits.SpeedKit;
import com.github.hexa.pvpbot.ai.gamemode.kits.SwordKit;

public class KitManager {

    public static void loadKit(Bot bot, GameMode gameMode) {
        loadKit(bot, getDefaultKit(gameMode));
    }

    public static void loadKit(Bot bot, Kit kit) {
        kit.load(bot.getControllable());
    }

    public static Kit getDefaultKit(GameMode gameMode) {
        return switch (gameMode) {
            case SWORD -> new SwordKit();
            case SPEED -> new SpeedKit();
            case NONE -> null; // Shouldn't happen but ok
        };
    }

}
