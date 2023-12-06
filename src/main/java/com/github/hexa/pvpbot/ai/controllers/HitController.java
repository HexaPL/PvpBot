package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.VectorUtils;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import com.github.hexa.pvpbot.v1_16_R3.EntityPlayerBot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.ai.controllers.HitController.HitType.*;

public class HitController extends Controller {

    public static float hitSpeed = 0.9F;
    public HitType hitType;

    public HitController(BotAIBase ai) {
        super(ai);
        this.hitType = SPRINT_HIT;
    }

    @Override
    public void update() {
        this.handleHitting();
    }

    private boolean handleHitting() {

        // Simple hit system for now

        // Check attack cooldown
        float currentHitSpeed = getCurrentHitSpeed();
        if (this.bot.getAttackCooldown() < currentHitSpeed) {
            return false;
        }

        // Calculate distance to target
        Location eyeLocation = bot.getEyeLocation();
        BoundingBox targetBoundingBox = ai.getTarget().getDelayedBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(eyeLocation, targetBoundingBox);

        // Check if target is close enough to consider attacking
        if (distance > ai.getReach() + 3) {
            return false;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), getPingReach());
        if (result == null) {
            return false;
        }

        doAttack(hitType);
        /* if (bot.getAI().getPing() == 0) {

        } else { // TODO - ping delayed hits
            Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), this::doAttack, MathHelper.floor((bot.getAI().getPing() / 2F) / 50F));
        }*/
        if (PvpBotPlugin.debug) {
            /*
            double pingToPing = getPingDistance();
            double trueToPing = BoundingBoxUtils.distanceTo(bot.getEyeLocation(), ai.getTarget().getDelayedBoundingBox());
            Vector v = bot.getEyeLocation().toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion().multiply((bot.getAI().getPing() / 2F) / 50F)));
            double pingToTrue = BoundingBoxUtils.distanceTo(v.toLocation(bot.getEyeLocation().getWorld()), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox()));
            Bukkit.broadcastMessage("REACH - true: " + MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.bot.getEyeLocation(), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox())), 4) + ", ping-to-ping: " + MathHelper.roundTo((float) getPingDistance(), 4) + ", true-to-ping: " + MathHelper.roundTo((float) trueToPing, 4) + ", ping-to-true: " + MathHelper.roundTo((float) pingToTrue, 4) + ", boxes: " + BoundingBoxUtils.distanceTo(bot.getAI().getTarget().getPlayer().getEyeLocation(), ai.getTarget().getDelayedBoundingBox()));

             */
        }
        return true;

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
        if (hitType == SPRINT_HIT) {
            ai.attack(ai.getTarget().getPlayer());
        } else if (hitType == CRITICAL_HIT) { // TODO - realistic crits (with w-release delays)
            if (!bot.canCrit() && !bot.isOnGround() && bot.getMotion().getY() >= 0) {
                return; // Wait for the falling phase of jump
            }
            bot.setSprinting(false);
            bot.setFallDistance(1.0F); // For some reason, bot always has 0 fall distance - so crits don't work without this trick
            ai.attack(ai.getTarget().getPlayer());
            bot.setFallDistance(0F);
            bot.setSprinting(true);
        }
        bot.swingArm();
    }

    public double getPingDistance() {
        Vector pingLocation = bot.getEyeLocation().toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion().multiply((bot.getAI().getPing() / 2F) / 50F)));
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
