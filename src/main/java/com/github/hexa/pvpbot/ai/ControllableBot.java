package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public interface ControllableBot extends Bot {

    public void setAI(BotAI ai);

    public BotAI getAI();

    public void setRotation(float yaw, float pitch);

    public void swingArm();

    public void attack(LivingEntity entity);

    public Location getEyeLocation();

    public BoundingBox getBukkitBoundingBox();

    public void setMoveForward(float forward);

    public float getMoveForward();

    public void setMoveStrafe(float strafe);

    public float getMoveStrafe();

    public boolean isSprinting();

    public void setMot(double x, double y, double z);

    public Vector getMotion();

    public void setSprinting(boolean isSprinting);

    public float getAttackCooldown();

}
