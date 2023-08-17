package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.util.PropertyMap;
import net.minecraft.server.v1_16_R3.DamageSource;

import java.util.HashMap;

public interface BotAI {

    public void tick();

    public void damageEntity(DamageSource damageSource);

    public PropertyMap getProperties();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public Target getTarget();

    public int getPing();

    public void setPing(int ping);

}
