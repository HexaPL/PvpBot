package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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
        BoundingBox targetBoundingBox = ai.getTarget().getDelayedBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(bot.getEyeLocation(), ai.getTarget().getDelayedBoundingBox());

        // Check if target is close enough to consider attacking
        if (distance > ai.getReach() + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), ai.getReach());
        if (result != null) {
            ai.attack(ai.getTarget().getPlayer());
            bot.swingArm();
            if (PvpBotPlugin.debug) {
                Bukkit.broadcastMessage("Bot reach: " + BoundingBoxUtils.distanceTo(this.bot.getEyeLocation(), targetBoundingBox));
            }
        }

    }

}
