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

    public HitController(BotAIBase ai) {
        super(ai);
    }

    @Override
    public void update() {
        this.handleHitting();
    }

    private void handleHitting() {

        // Simple hit system for now

        // Check attack cooldown
        if (this.bot.getAttackCooldown() != 1.0F) {
            return;
        }

        // Calculate distance to target
        Location eyeLocation = bot.getEyeLocation();
        Location predictedEyeLocation  = getPingPredictedLocation(eyeLocation);
        BoundingBox targetBoundingBox = ai.getTarget().getDelayedBoundingBox();
        double distancePing = BoundingBoxUtils.distanceTo(predictedEyeLocation, ai.getTarget().getDelayedBoundingBox());
        double distanceNormal = BoundingBoxUtils.distanceTo(eyeLocation, ai.getTarget().getDelayedBoundingBox());

        // Check if target is close enough to consider attacking
        if (distancePing > ai.getReach() + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), ai.getReach() + (distanceNormal - distancePing));
        if (result != null) {
            if (PvpBotPlugin.debug) {
                Bukkit.broadcastMessage("REACH - true: " + MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.bot.getEyeLocation(), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox())), 4) + ", delayed: " + MathHelper.roundTo((float) distancePing, 4));
            }
            if (bot.getAI().getPing() == 0) {
                doAttack();
            } else {
                Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), this::doAttack, MathHelper.floor((bot.getAI().getPing() / 2F) / 50F));
            }
        }

    }

    private void doAttack() {
        ai.attack(ai.getTarget().getPlayer());
        bot.swingArm();
    }

    public Location getPingPredictedLocation(Location location) {
        Vector pingLocation = location.toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion()).multiply((bot.getAI().getPing() / 2F) / 50F));
        return pingLocation.toLocation(bot.getEyeLocation().getWorld(), bot.getEyeLocation().getYaw(), bot.getEyeLocation().getPitch()); // TODO - predicted yaw and pitch
    }

}
