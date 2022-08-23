package com.github.hexa.pvpbot.ai;

public interface BotAI {

    public void tick();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public Target getTarget();

    public int getPing();

    public void setPing(int ping);

}
