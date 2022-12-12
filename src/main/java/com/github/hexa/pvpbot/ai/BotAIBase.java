package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.ai.controllers.AimController;
import com.github.hexa.pvpbot.ai.controllers.HitController;
import com.github.hexa.pvpbot.ai.controllers.MovementController;
import com.github.hexa.pvpbot.util.MathHelper;
import org.bukkit.entity.Player;

public class BotAIBase implements BotAI {

    private ControllableBot bot;
    private Target target;
    private boolean enabled;

    private AimController aimController;
    private HitController hitController;
    private MovementController movementController;

    private int ping;
    private float reach;

    public BotAIBase(ControllableBot bot) {
        this.bot = bot;
        this.enabled = true;
        this.initAI();
    }

    @Override
    public void tick() {
        this.updateTarget();
        this.updateControllers();
        target.updateLocationCache();
    }

    protected void updateControllers() {
        aimController.update();
        bot.setSprinting(movementController.canSprint() && !movementController.isSprintResetting());
        hitController.update();
        movementController.update();
    }

    protected void updateTarget() {
        if (target == null) {
            target = this.selectTarget();
        }
        if (target.delay != this.ping) {
            target.delay = this.ping;
            target.locationCacheSize = MathHelper.ceil(this.ping / 50F);
            target.flushLocationCache();
        }
        target.update();
    }

    protected Target selectTarget() {
        return new Target(bot.getOwner());
    }

    public void attack(Player player) {

        // Cache initial sprint state to restore it later
        boolean wasSprinting = bot.isSprinting();

        // Check if knockback will be applied to target
        boolean invulnerable = player.getNoDamageTicks() > player.getMaximumNoDamageTicks() / 2;
        boolean knockback = !invulnerable;

        // Correct sprint state if needed
        if (knockback && bot.isSprinting() && !movementController.isFreshSprint()) {
            bot.setSprinting(false);
        }

        // Attack entity
        bot.attack(player);

        // Simulate client-server desync and make bot sprint-reset soon
        if (knockback && movementController.isFreshSprint()) {
            movementController.setFreshSprint(false);
            movementController.setSprintTicks(0);
        }

        // Restore sprint state to not affect later movement
        bot.setSprinting(wasSprinting);

    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            this.initAI();
            // TODO Test
            this.setPing(80);
        }
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public int getPing() {
        return this.ping;
    }

    @Override
    public void setPing(int ping) {
        this.ping = Math.max(ping, 0);
    }

    public Bot getBot() {
        return bot;
    }

    public void setReach(float reach) {
        this.reach = reach;
    }

    public float getReach() {
        return this.reach;
    }

    private void initAI() {
        this.initControllers();
        this.reach = 3.0F;
        this.ping = 0;
    }

    private void initControllers() {
        this.aimController = new AimController(this);
        this.hitController = new HitController(this);
        this.movementController = new MovementController(this);
    }

    public static class Direction {
        public static final int FORWARD = 1;
        public static final int BACKWARD = -1;
        public static final int RIGHT = 1;
        public static final int LEFT = -1;
    }

}
