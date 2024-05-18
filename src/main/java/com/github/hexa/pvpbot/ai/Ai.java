package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.util.IDamageSource;
import com.github.hexa.pvpbot.util.PropertyMap;

public interface Ai {

    public void tick();

    public void onDamageEntity(IDamageSource damageSource);

    public PropertyMap getProperties();

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public Bot getBot();

    public Target getTarget();

    public int getPing();

    public void setPing(int ping);

}
