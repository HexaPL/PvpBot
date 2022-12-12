package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;

public class HitController extends Controller {

    private ClickingMethod clickingMethod;

    private int clicksPerSecond;
    private int clickDelay;
    private int tickMsTimer;

    public HitController(BotAIBase ai) {
        super(ai);
        this.clicksPerSecond = 0;
        this.clickDelay = 0;
        this.tickMsTimer = 0;
    }

    @Override
    public void update() {
        this.handleClicking();
    }

    protected void handleClicking() {

        // Check for target and CPS
        if (target == null || this.clicksPerSecond == 0) {
            this.tickMsTimer = 0;
            return;
        }

        // Return if on click cooldown
        if (this.tickMsTimer < this.clickDelay) {
            this.tickMsTimer += 50;
            return;
        }

        // Calculate distance to target
        Location eyeLocation = bot.getEyeLocation();
        BoundingBox targetBoundingBox = target.getDelayedBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(bot.getEyeLocation(), target.getDelayedBoundingBox());

        // Check if target is close enough to swing or attack
        if (distance > ai.getReach() + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), ai.getReach());

        // Swing hand and/or attack target, based on current click rate
        while (this.tickMsTimer >= this.clickDelay) {
            this.tickMsTimer -= this.clickDelay;
            bot.swingArm();
            if (result != null) {
                ai.attack(target.getPlayer());
            }
        }

    }

    public ClickingMethod getClickingMethod() {
        return this.clickingMethod;
    }

    public void setClickingMethod(ClickingMethod clickingMethod, int cps) {
        this.clickingMethod = clickingMethod;
    }

    protected void setCPS(int cps) {
        this.clicksPerSecond = cps;
        this.clickDelay = cps == 0 ? 0 : 1000 / cps;
    }

    protected int getCPS() {
        return this.clicksPerSecond;
    }

    public enum ClickingMethod {

        NORMAL_CLICK(1, 11),
        JITTER_CLICK(10, 15),
        BUTTERFLY_CLICK(15, 21),
        AUTOCLICK(1, 50);

        private int minCps;
        private int maxCps;

        ClickingMethod(int minCps, int maxCps) {
            this.minCps = minCps;
            this.maxCps = maxCps;
        }

        public int getMinCps() {
            return minCps;
        }

        public int getMaxCps() {
            return maxCps;
        }

    }

}
