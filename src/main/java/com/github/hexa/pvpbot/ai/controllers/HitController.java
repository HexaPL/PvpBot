package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import org.bukkit.Location;

public class HitController extends Controller {

    public ClickingMethod clickingMethod;
    public int targetCps;
    public int currentCps;

    private int clickDelay;
    private int tickMsTimer;
    private double minCpsRange;
    private double maxCpsRange;
    private double cpsRange;

    public HitController(BotAIBase ai) {
        super(ai);
        this.targetCps = 0;
        this.currentCps = 0;
        this.clickDelay = 0;
        this.tickMsTimer = 0;
        this.minCpsRange = 0;
        this.maxCpsRange = 0;
        this.cpsRange = 0;
    }

    @Override
    public void update() {
        this.handleClicking();
    }

    protected void handleClicking() {

        // Check for target and CPS
        if (ai.getTarget() == null || this.targetCps == 0) {
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
        BoundingBox targetBoundingBox = ai.getTarget().getDelayedBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(bot.getEyeLocation(), ai.getTarget().getDelayedBoundingBox());

        // Check if target is close enough to swing or attack
        if (distance > ai.getReach() + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), ai.getReach());

        // Swing hand and/or attack target, based on current click rate
        while (this.tickMsTimer >= this.clickDelay) {
            this.tickMsTimer -= this.clickDelay;
            this.calculateNextClickDelay();
            bot.swingArm();
            if (result != null) {
                ai.attack(ai.getTarget().getPlayer());
            }
        }

    }

    public void setClickingMethod(ClickingMethod clickingMethod, int cps) {
        this.setClickingMethod(clickingMethod);
        this.setCPS(cps);
    }

    public void setClickingMethod(ClickingMethod clickingMethod) {
        this.clickingMethod = clickingMethod;
        this.setCPS(this.targetCps); // To ensure that CPS are in bound of new clicking method
    }

    public void setCPS(int cps) {
        this.targetCps = MathHelper.clamp(cps, clickingMethod.minCps, clickingMethod.maxCps);
        this.clickDelay = cps == 0 ? 0 : 1000 / cps;
        this.calculateCpsRange();
    }

    private void calculateCpsRange() {
        switch (this.clickingMethod) {
            case NORMAL_CLICK:
            case JITTER_CLICK:
                this.cpsRange = (0.4 + targetCps * 0.15) / 2;
                this.minCpsRange = MathHelper.clamp(this.targetCps - this.cpsRange, this.clickingMethod.minCps, this.clickingMethod.maxCps);
                this.maxCpsRange = MathHelper.clamp(this.targetCps + this.cpsRange, this.clickingMethod.minCps, this.clickingMethod.maxCps);
                break;
            case BUTTERFLY_CLICK:
                break; // TODO - butterfly click cps range
        }
    }

    private void calculateNextClickDelay() {
        double nextCps;
        int sign = MathHelper.random(0, 1) == 0 ? -1 : 1; // To determine if cps will increase or decrease
        switch (this.clickingMethod) {
            case NORMAL_CLICK:
            case JITTER_CLICK:
                nextCps = MathHelper.clamp(this.currentCps + this.cpsRange * 0.15 * sign, this.minCpsRange, this.maxCpsRange);
                break;
            case BUTTERFLY_CLICK:

        }
    }

    public enum ClickingMethod {

        NORMAL_CLICK(2, 11),
        JITTER_CLICK(10, 15),
        BUTTERFLY_CLICK(15, 21),
        AUTOCLICK(1, 30);

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
