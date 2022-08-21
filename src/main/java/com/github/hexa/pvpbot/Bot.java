package com.github.hexa.pvpbot;

import org.bukkit.entity.Player;

public interface Bot {

    public Player getTarget();

    public void setTarget(Player target);

    public void clearTarget();

    public void setCPS(int cps);

    public int getCPS();

    public void setMoveForward(float forward);

    public void setMoveStrafe(float strafe);

    public void canSprint(boolean canSprint);

    public boolean canSprint();

    public String getBotName();

}
