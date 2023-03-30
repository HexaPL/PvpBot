package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.controllers.AimController;
import com.github.hexa.pvpbot.ai.controllers.HitController;
import com.github.hexa.pvpbot.ai.controllers.HitController.ClickingMethod;
import com.github.hexa.pvpbot.ai.controllers.MovementController;
import com.github.hexa.pvpbot.events.PropertySetEvent;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.PropertyMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

public class BotAIBase implements BotAI {

    private ControllableBot bot;
    private Target target;
    private boolean enabled;

    private PropertyMap properties;

    public AimController aimController;
    public HitController hitController;
    public MovementController movementController;

    private int ping;
    private float reach;
    private float pingAmplifier;
    public int botCombo;
    public int opponentCombo;

    public BotAIBase(ControllableBot bot) {
        this.bot = bot;
        this.enabled = true;
        this.botCombo = 0;
        this.opponentCombo = 0;
        PvpBotPlugin.getInstance().getServer().getPluginManager().registerEvents(new Listener(), PvpBotPlugin.getInstance());
        this.initAI();
    }

    @Override
    public void tick() {
        this.updateTarget();
        this.updateControllers();
        target.updateLocationCache();
    }

    @Override
    public PropertyMap getProperties() {
        return properties;
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
        if (target.delay != this.ping * pingAmplifier) {
            target.delay = Math.round(this.ping * pingAmplifier);
            target.locationCacheSize = MathHelper.ceil(target.delay / 50F);
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

        // Increase bot's combo counter
        if (knockback) {
            botCombo++;
            opponentCombo = 0;
        }

        // Correct sprint state if needed
        if (knockback && bot.isSprinting() && !movementController.isFreshSprint()) {
            bot.setSprinting(false);
            Vector mot = bot.getMotion();
            bot.setMot(mot.getX() * 0.6, mot.getY(), mot.getZ() * 0.6);
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
        if (!enabled) {
            bot.setSprinting(false);
            bot.setMoveForward(0);
            bot.setMoveStrafe(0);
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

    public HitController.ClickingMethod getClickingMethod() {
        return hitController.clickingMethod;
    }

    public void setClickingMethod(HitController.ClickingMethod clickingMethod) {
        hitController.setClickingMethod(clickingMethod);
    }

    public void setClickingMethod(HitController.ClickingMethod clickingMethod, int cps) {
        hitController.setClickingMethod(clickingMethod, cps);
    }

    protected int getCPS() {
        return hitController.clicksPerSecond;
    }

    private void initAI() {
        this.reach = 3.0F;
        this.ping = 0;
        this.pingAmplifier = 1F;
        this.initControllers();
        this.initProperties();
    }

    private void initProperties() {
        this.properties = new PropertyMap(bot);
        properties.init("reach", 3.0F, Float.class);
        properties.init("ping", 0, Integer.class);
        properties.init("pingAmplifier", 1F, Float.class);
        properties.init("clickingMethod", ClickingMethod.AUTOCLICK, ClickingMethod.class);
        properties.init("cps", 7, Integer.class);
    }

    private void initControllers() {
        this.aimController = new AimController(this);
        this.hitController = new HitController(this);
        this.movementController = new MovementController(this);
    }

    public class Listener implements org.bukkit.event.Listener {

        @EventHandler
        public void onPropertySet(PropertySetEvent event) {
            if (event.getBot() != bot) {
                return;
            }
            String property = event.getProperty();
            if (property.equals("clickingMethod")) {
                BotAIBase.this.setClickingMethod((ClickingMethod) event.getValue());
            } else if (property.equals("cps")) {
                BotAIBase.this.hitController.setCPS((int) event.getValue());
            } else if (property.equals("reach")) {
                BotAIBase.this.setReach((float) event.getValue());
            } else if (property.equals("ping")) {
                BotAIBase.this.setPing((int) event.getValue());
            } else if (property.equals("pingAmplifier")) {
                BotAIBase.this.pingAmplifier = (float) event.getValue();
            }
        }

    }

    public static class Direction {
        public static final int FORWARD = 1;
        public static final int BACKWARD = -1;
        public static final int RIGHT = 1;
        public static final int LEFT = -1;
    }

}
