package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public interface ControllableBot extends Bot {

    public void setAI(Ai ai);

    public Ai getAI();

    public void setRotation(float yaw, float pitch);

    public void swingArm();

    public void attack(LivingEntity entity);

    public boolean canJump();

    public void jump();

    public Location getEyeLocation();

    public BoundingBox getBukkitBoundingBox();

    public void setMoveForward(float forward);

    public float getMoveForward();

    public void setMoveStrafe(float strafe);

    public float getMoveStrafe();

    public boolean isSprinting();

    public boolean isOnGround();

    public boolean canCrit();

    public void setFallDistance(float f);

    public void setMot(double x, double y, double z);

    public Vector getMotion();

    public void setSprinting(boolean isSprinting);

    public float getAttackCooldown();

    public class MoveDirection {
        public static final int FORWARD = 1;
        public static final int BACKWARD = -1;
        public static final int RIGHT = -1;
        public static final int LEFT = 1;
    }
}
