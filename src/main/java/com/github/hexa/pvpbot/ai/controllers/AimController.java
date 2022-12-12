package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AimController extends Controller {

    public AimController(BotAIBase ai) {
        super(ai);
    }

    @Override
    public void update() {
        if (ai.getTarget() != null) {
            this.rotateToTarget();
        }
    }

    protected void rotateToTarget() {
        this.rotateToLocation(target.getDelayedHeadLocation());
    }

    protected void rotateToLocation(Location location) {
        Location from = bot.getEyeLocation();
        Vector direction = VectorUtils.getVectorFromTo(from, location);
        float vecYaw = VectorUtils.getVectorYaw(direction);
        float vecPitch = VectorUtils.getVectorPitch(direction);
        bot.setRotation(vecYaw, vecPitch);
    }

}
