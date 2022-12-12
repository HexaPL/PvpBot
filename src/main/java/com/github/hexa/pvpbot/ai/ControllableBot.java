package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

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

    public void setSprinting(boolean isSprinting);

}