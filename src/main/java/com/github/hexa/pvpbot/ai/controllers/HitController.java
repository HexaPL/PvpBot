package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.VectorUtils;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class HitController extends Controller {

    private double pingDistance;

    public static float hitSpeed = 0.9F;

    public HitController(BotAIBase ai) {
        super(ai);
        pingDistance = 0;
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
        if (bot.getAI().getPing() == 0) {
            doAttack();
        } else {
            Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), this::doAttack, MathHelper.floor((bot.getAI().getPing() / 2F) / 50F));
        }
        if (PvpBotPlugin.debug) {
            double pingToPing = getPingDistance();
            double trueToPing = BoundingBoxUtils.distanceTo(bot.getEyeLocation(), ai.getTarget().getDelayedBoundingBox());
            Vector v = bot.getEyeLocation().toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion().multiply((bot.getAI().getPing() / 2F) / 50F)));
            double pingToTrue = BoundingBoxUtils.distanceTo(v.toLocation(bot.getEyeLocation().getWorld()), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox()));
            Bukkit.broadcastMessage("REACH - true: " + MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.bot.getEyeLocation(), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox())), 4) + ", ping-to-ping: " + MathHelper.roundTo((float) getPingDistance(), 4) + ", true-to-ping: " + MathHelper.roundTo((float) trueToPing, 4) + ", ping-to-true: " + MathHelper.roundTo((float) pingToTrue, 4) + ", boxes: " + BoundingBoxUtils.distanceTo(bot.getAI().getTarget().getPlayer().getEyeLocation(), ai.getTarget().getDelayedBoundingBox()));
        }
        return true;

    }

    public float getCurrentHitSpeed() {
        switch (ai.movementController.comboMethod) {
            case UPPERCUT:
                return 0.92F;
            default:
                return hitSpeed;
        }
    }

    private void doAttack() {
        ai.attack(ai.getTarget().getPlayer());
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
