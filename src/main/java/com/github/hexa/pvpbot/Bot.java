package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.ai.ControllableBot;

public interface Bot {

    public String getBotName();

    default public ControllableBot getController() {
        return (ControllableBot) this;
    }

}
