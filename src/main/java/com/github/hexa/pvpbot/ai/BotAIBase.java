package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.VectorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.BotAIBase.SprintResetMethod.*;

public class BotAIBase implements BotAI {

    private ControllableBot bot;

    private boolean shouldAttack;
    private float reach;
    private int clicksPerSecond;
    private int clickDelay;
    private boolean freshSprint;
    private boolean isSprintResetting;
    private boolean sTapSlowdown;
    private int sprintResetDelay = 3;
    private int sprintResetLength = 4;
    private SprintResetMethod sprintResetMethod;
    private int tickMsTimer;
    private int sprintTicks;

    public BotAIBase(ControllableBot bot) {
        this.bot = bot;
        this.reach = 3.0F;
        this.clicksPerSecond = 0;
        this.clickDelay = 0;
        this.shouldAttack = false;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.sTapSlowdown = false;
        this.sprintResetMethod = WTAP;
        this.tickMsTimer = 0;
        this.sprintTicks = -1;

        //Test
        this.setCPS(20);
    }

    @Override
    public void tick() {
        this.shouldAttack = bot.getTarget() != null;
        this.updateRotation();
        bot.setSprinting(bot.canSprint() && !this.isSprintResetting);
        this.doHitLogic();
        this.handleSprintResetting();
    }

    protected void updateRotation() {
        if (bot.getTarget() != null) {
            this.rotateToEntity(bot.getTarget());
        }
    }

    protected void doHitLogic() {

        // Check for target and CPS
        if (bot.getTarget() == null || !this.shouldAttack || this.clicksPerSecond == 0) {
            this.tickMsTimer = 0;
            return;
        }

        // Return if on click cooldown
        if (this.tickMsTimer < this.clickDelay) {
            this.tickMsTimer += 50;
            return;
        }

        // Calculate distance to closest point of target's hitbox
        Location eyeLocation = bot.getEyeLocation();
        BoundingBox targetBoundingBox = bot.getTarget().getBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(eyeLocation, targetBoundingBox);

        // Check if target is close enough to swing or attack
        if (distance > this.reach + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), this.reach);

        // Swing hand and/or attack target, based on current click rate
        while (this.tickMsTimer >= this.clickDelay) {
            this.tickMsTimer -= this.clickDelay;
            bot.swingArm();
            if (result != null) {
                this.attack(bot.getTarget());
            }
        }

    }

    protected void handleSprintResetting() {

        // Check if any action is required
        if (!bot.canSprint() || this.sprintTicks == -1) {
            return;
        }

        // Reset s-tap slowdown to not move backwards
        if (this.sTapSlowdown) {
            this.sTapSlowdown = false;
            bot.setMoveForward(FORWARD);
        }

        // Bukkit.broadcastMessage("handleSprintResetting - sprintTicks" + )

        // Start sprint reset if needed
        if (bot.isSprinting() && bot.getMoveForward() > 0 && !this.freshSprint && !this.isSprintResetting && this.sprintTicks >= this.sprintResetDelay) {
            Bukkit.broadcastMessage("startSprintReset");
            bot.setSprinting(false);
            this.isSprintResetting = true;
            this.startSprintReset(this.sprintResetMethod);
            this.sprintTicks = 0;
        }

        // End sprint reset if needed
        if (isSprintResetting && this.sprintTicks >= sprintResetLength) {
            Bukkit.broadcastMessage("stopSprintReset");
            bot.setSprinting(true);
            this.freshSprint = true;
            this.isSprintResetting = false;
            this.endSprintReset(this.sprintResetMethod);
            this.sprintTicks = -1;
        }

        if (this.sprintTicks != -1) this.sprintTicks++;

    }

    protected void startSprintReset(SprintResetMethod method) {

        switch (method) {
            case WTAP:
                // Simply simulate releasing W key
                bot.setMoveForward(0);
                break;
            case STAP:
                // Do some opposite force to slow down faster
                bot.setMoveForward(-0.5F);
                this.sTapSlowdown = true;
                break;
            case BLOCKHIT:
                // Block sword
                // TODO - blockhit
                break;
        }

    }

    protected void endSprintReset(SprintResetMethod method) {
        switch (method) {
            case WTAP:
                bot.setMoveForward(FORWARD);
        }
    }

    protected void attack(LivingEntity entity) {

        AttackResult result = bot.attack(entity, this.freshSprint);

        Bukkit.broadcastMessage("attackResult: " + result);

        // Simulate client-server desync and make bot sprint-reset soon
        if (result.equals(AttackResult.KNOCKBACK) && this.freshSprint) {
            this.freshSprint = false;
            this.sprintTicks = 0;
        }

    }

    protected void rotateToEntity(LivingEntity entity) {
        Location location = entity.getEyeLocation();
        this.rotateToLocation(location);
    }

    protected void rotateToLocation(Location location) {
        Location from = bot.getEyeLocation();
        Vector direction = VectorUtils.getVectorFromTo(from, location);
        float vecYaw = VectorUtils.getVectorYaw(direction);
        float vecPitch = VectorUtils.getVectorPitch(direction);
        bot.setRotation(vecYaw, vecPitch);
    }

    protected void setCPS(int cps) {
        this.clicksPerSecond = cps;
        this.clickDelay = cps == 0 ? 0 : 1000 / cps;
    }

    protected int getCPS() {
        return this.clicksPerSecond;
    }

    public enum SprintResetMethod {
        WTAP, STAP, BLOCKHIT
    }

    public static class Direction {
        public static final int FORWARD = 1;
        public static final int BACKWARD = -1;
        public static final int RIGHT = 1;
        public static final int LEFT = -1;
    }

}
