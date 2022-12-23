package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.BotManager;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.util.org.bukkit.util.BoundingBox;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.org.bukkit.util.RayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class HitController extends Controller {

    public ClickingMethod clickingMethod;
    public int clicksPerSecond;

    private int clickDelay;
    private int tickMsTimer;

    private long test;

    public HitController(BotAIBase ai) {
        super(ai);
        this.clicksPerSecond = 0;
        this.clickDelay = 0;
        this.tickMsTimer = 0;
        test = System.currentTimeMillis();
    }

    @Override
    public void update() {
        this.handleClicking();
    }

    protected void handleClicking() {

        // Check for target and CPS
        if (ai.getTarget() == null || this.clicksPerSecond == 0) {
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
            bot.swingArm();
            if (result != null) {
                ai.attack(ai.getTarget().getPlayer());
            }
        }

    }

    public void setClickingMethod(ClickingMethod clickingMethod, int cps) {
        this.clickingMethod = clickingMethod;
        this.setCPS(cps);
    }

    public void setClickingMethod(ClickingMethod clickingMethod) {
        this.clickingMethod = clickingMethod;
    }

    public void setCPS(int cps) {
        this.clicksPerSecond = MathHelper.clamp(cps, clickingMethod.minCps, clickingMethod.maxCps);
        this.clickDelay = cps == 0 ? 0 : 1000 / cps;
    }

    private void error(String reason) {
        if (System.currentTimeMillis() - test < 1000) return;
        Bukkit.broadcastMessage("ERROR: " + reason);
        BotManager manager = PvpBotPlugin.getManager();
        manager.removeBot(manager.getBotByName("test"));
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
