package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.Sequence;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.controllers.HitController.HitType.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.ComboMethod.*;

public class HitController extends Controller {

    public static float hitSpeed = 0.9F;
    public HitType hitType;
    public Sequence hitSequence;
    public boolean isCritting;

    public HitController(BotAIBase ai) {
        super(ai);
        this.setHitSequence(sprintHitSequence);
        this.hitType = SPRINT_HIT;
        this.isCritting = false;
    }

    @Override
    public void update() {
        // TODO - jumpcrits based on velocity
        /*if ((ai.movementController.ticksSinceAttack > 16 || ai.botCombo == 0) && this.getPingDistance() > 4 && (this.hitSequence != jumpAndCritSequence || this.hitSequence.finished)) {
            this.setHitSequence(jumpAndCritSequence);
            this.hitSequence.start();
        }*/
        if (ai.movementController.comboMethod == CRIT_SPAM && ai.botCombo >= 2 && this.hitSequence.finished) {
            this.setHitSequence(jumpAndCritSequence);
            this.hitSequence.start();
        } else if (this.hitSequence.finished) {
            this.setHitSequence(sprintHitSequence);
            this.hitSequence.start();
        }
        this.hitSequence.tick();
    }

    public Sequence sprintHitSequence = new Sequence(2) {
        @Override
        public void onStart() {
            if (!ai.movementController.isSprintResetting()) {
                bot.setMoveForward(FORWARD);
            }
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> canAttack());
                    break;
                case 2:
                    doAttack(SPRINT_HIT);
                    break;
            }
        }
    };

    public Sequence jumpAndCritSequence = new Sequence(5) {
        @Override
        public void onStart() {
            if (!ai.movementController.isSprintResetting()) {
                bot.setMoveForward(FORWARD);
            }
        }

        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.waitUntil(() -> !ai.movementController.isSprintResetting());
                    break;
                case 2:
                    this.tickSubsequence(ai.movementController.jumpSequence);
                    break;
                case 3:
                    this.wait(7);
                    break;
                case 4:
                    if (bot.isOnGround()) { // Stop the sequence if bot landed before it could hit the player
                        this.stop();
                        break;
                    }
                    this.nextStep();
                    break;
                case 5:
                    this.tickSubsequence(critSequence);
                    break;
            }
        }

        @Override
        public void onStop() {
            this.stopSubsequence();
        }
    };

    public Sequence critSequence = new Sequence(3) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    isCritting = true;
                    bot.setMoveForward(0);
                    break;
                case 2:
                    this.wait(2);
                    break;
                case 3:
                    boolean canHit = canHit();
                    if (canHit) {
                        doAttack(CRITICAL_HIT);
                    }
                    isCritting = false;
                    break;
            }
        }

        @Override
        public void onStop() {
            isCritting = false;
        }
    };

    public boolean canHit() {

        // Calculate distance to target
        Location eyeLocation = bot.getEyeLocation();
        BoundingBox targetBoundingBox = ai.getTarget().getDelayedBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(eyeLocation, targetBoundingBox);

        // Check if target is close enough to consider attacking
        if (distance > ai.getReach() + 3) {
            return false;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), ai.getReach() + 3);
        return result != null && distance <= this.getPingReach();
    }

    public boolean canAttack() {

        // Check attack cooldown
        float currentHitSpeed = getCurrentHitSpeed();
        if (this.bot.getAttackCooldown() < currentHitSpeed) {
            return false;
        }

        // Check target's no damage ticks

        return canHit();
    }

    public boolean canCrit() {
        return bot.canCrit();
    }

    public void setHitSequence(Sequence hitSequence) {
        if (this.hitSequence != null && !this.hitSequence.finished) {
            this.hitSequence.stop();
        }
        this.hitSequence = hitSequence;
    }

    public float getCurrentHitSpeed() {
        switch (ai.movementController.comboMethod) {
            case UPPERCUT:
                return 0.92F;
            case CRIT_SPAM:
                return hitSpeed;
            default:
                return hitSpeed;
        }
    }

    private void doAttack(HitType hitType) {
        if (hitType == CRITICAL_HIT) {
            bot.setFallDistance(1.0F);
            this.doAttack();
            bot.setFallDistance(0.0F);
        } else {
            this.doAttack();
        }
    }

    public void doAttack() {
        ai.attack(ai.getTarget().getPlayer());
        bot.swingArm();
    }

    public double getPingDistance() {
        //Vector pingLocation = bot.getEyeLocation().toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion().multiply((bot.getAI().getPing() / 2F) / 50F)));
        Vector pingLocation = bot.getEyeLocation().toVector();
        return BoundingBoxUtils.distanceTo(pingLocation.toLocation(bot.getEyeLocation().getWorld()), ai.getTarget().getDelayedBoundingBox());
    }

    public float getPingReach() {
        Location eyeLocation = bot.getEyeLocation();
        double distanceNormal = BoundingBoxUtils.distanceTo(eyeLocation, ai.getTarget().getDelayedBoundingBox());
        return (float) (ai.getReach() + (distanceNormal - getPingDistance()));
    }

    public enum HitType {
        SPRINT_HIT, CRITICAL_HIT
    }

}
