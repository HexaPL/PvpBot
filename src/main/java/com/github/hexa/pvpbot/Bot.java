package com.github.hexa.pvpbot;

import com.github.hexa.pvpbot.ai.gamemode.GameMode;
import com.github.hexa.pvpbot.skins.Skin;
import com.github.hexa.pvpbot.util.PropertyMap;
import org.bukkit.entity.Player;

import java.net.URL;

public interface Bot {

    public String getBotName();

    default public ControllableBot getControllable() {
        return (ControllableBot) this;
    }

    public void setGameMode(GameMode gameMode);

    public GameMode getGameMode();

    public PropertyMap getProperties();

    public Player getOwner();

    public void setOwner(Player owner);

    public Skin getSkin();

    public void setSkin(Player player);

    public void setSkin(Skin skin);

    public void setSkin(URL mineSkinUrl);

}
