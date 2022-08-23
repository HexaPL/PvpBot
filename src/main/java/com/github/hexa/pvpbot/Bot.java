package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.ai.ControllableBot;
import org.bukkit.entity.Player;

public interface Bot {

    public String getBotName();

    default public ControllableBot getController() {
        return (ControllableBot) this;
    }

    public Player getOwner();

    public void setOwner(Player owner);

}
