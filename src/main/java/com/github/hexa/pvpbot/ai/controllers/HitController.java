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
        if (this.bot.getAttackCooldown() < hitSpeed) {
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
            Bukkit.broadcastMessage("REACH - true: " + MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.bot.getEyeLocation(), BoundingBoxUtils.bukkitToLegacy(bot.getAI().getTarget().getPlayer().getBoundingBox())), 4) + ", delayed: " + MathHelper.roundTo((float) pingDistance, 4) + ", pingReach: " + MathHelper.roundTo(getPingReach(), 4));
        }
        return true;

    }

    private void doAttack() {
        ai.attack(ai.getTarget().getPlayer());
        bot.swingArm();
    }

    public float getPingReach() {
        Location eyeLocation = bot.getEyeLocation();
        Vector pingLocation = bot.getEyeLocation().toVector().add(VectorUtils.motionToBlockSpeed(bot.getMotion()).multiply((bot.getAI().getPing() / 2F) / 50F));
        pingDistance = BoundingBoxUtils.distanceTo(pingLocation.toLocation(bot.getEyeLocation().getWorld()), ai.getTarget().getDelayedBoundingBox());
        double distanceNormal = BoundingBoxUtils.distanceTo(eyeLocation, ai.getTarget().getDelayedBoundingBox());
        return (float) (ai.getReach() + (distanceNormal - pingDistance));
    }

}
