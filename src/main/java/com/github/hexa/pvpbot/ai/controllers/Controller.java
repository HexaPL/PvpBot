package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.ControllableBot;
import com.github.hexa.pvpbot.ai.Target;

public class Controller {

    protected BotAIBase ai;
    protected ControllableBot bot;
    protected Target target;

    public Controller(BotAIBase ai) {
        this.ai = ai;
        this.bot = ai.getBot().getControllable();
        this.target = ai.getTarget();
    }

    public void update() { }

}
