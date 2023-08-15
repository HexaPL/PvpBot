package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;

public class HitController extends Controller {

    public HitController(BotAIBase ai) {
        super(ai);
    }

    @Override
    public void update() {
        this.handleHitting();
    }

    private void handleHitting() {

        // Simple hit system
        // Check attack cooldown
        //test
        if (this.bot.getAttackCooldown() != 1.0F) {
            return;
        }

    }

}
