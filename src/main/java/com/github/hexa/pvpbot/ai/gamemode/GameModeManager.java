package com.github.hexa.pvpbot.ai.gamemode;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.ControllableBot;
import com.github.hexa.pvpbot.ai.gamemode.speed.SpeedAi;
import com.github.hexa.pvpbot.ai.gamemode.sword.SwordAi;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GameModeManager {

    public static void setGameMode(Bot bot, GameMode gameMode) {
        if (bot.getGameMode() != null) {
            return; // Bot already has gamemode
        }
        bot.setGameMode(gameMode);
        if (gameMode == GameMode.NONE) {
            return; // NONE gamemode (no AI)
        }
        ControllableBot cBot = bot.getControllable();
        switch (gameMode) {
            case SWORD:
                cBot.setAI(new SwordAi(cBot));
                break;
            case SPEED:
                cBot.setAI(new SpeedAi(cBot));
                cBot.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, true, false));
                break;
        }
        KitManager.loadKit(bot, gameMode);
    }

}
