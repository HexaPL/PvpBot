package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.ai.SwordAi.ComboMethod.*;
import static com.github.hexa.pvpbot.ai.SwordAi.HitMethod.*;
import static com.github.hexa.pvpbot.ai.SwordAi.HitMethod.HOP;
import static com.github.hexa.pvpbot.ai.SwordAi.HitMethod.UPPERCUT;
import static com.github.hexa.pvpbot.ai.SwordAi.HitType.*;
import static com.github.hexa.pvpbot.ai.SwordAi.SprintResetMethod.*;
import static com.github.hexa.pvpbot.ai.ControllableBot.MoveDirection.*;
import static com.github.hexa.pvpbot.ai.SwordAi.StrafeMethod.*;

public class SwordAi implements Ai {

    public static SprintResetMethod defaultSprintResetMethod = W_TAP;
    public static HitMethod defaultFirstHitMethod = NORMAL_HIT;
    public static StrafeMethod defaultStrafeMethod = NO_STRAFE;
    public static ComboMethod defaultComboMethod = STRAIGHT_LINE;

    public static float hitSpeed = 0.93F;
    public static int wTapLength = 7;
    public static int sTapLength = 3;
    public static int sprintResetDelay = 1;
    public static int maxStrafeDistance = 15;

    public ControllableBot bot;
    public Target target;
    private boolean enabled;
    private PropertyMap properties;
    public SequencesSword sequences;

    public int botCombo;
    public int opponentCombo;

    public SprintResetMethod sprintResetMethod;
    public ComboMethod comboMethod;
    public Sequence sprintResetSequence;
    public StrafeMethod strafeMethod;
    public HitMethod firstHitMethod;
    public HitMethod comboHitMethod;
    public Sequence firstHitSequence;
    public Sequence strafeSequence;
    public Sequence comboHitSequence;
    public Sequence comboStrafeSequence;

    public double blockSpeed;
    public Vector motionVectorTowardsTarget;
    public double motionTowardsTarget;

    private boolean freshSprint;
    private boolean isSprintResetting;
    boolean firstHit;
    public int ticksSinceAttack;
    public int ticksSinceDamage;

    public HitType hitType;
    public Sequence hitSequence;
    boolean doSTap;

    private Location lastLoc;

    public SwordAi() {
        // Empty constructor for Sequences
    }

    public SwordAi(ControllableBot bot) {
        this.bot = bot;
        this.sequences = new SequencesSword(this);
        this.createProperties();
        this.enabled = true;
        this.target = this.selectTarget();
        this.lastLoc = bot.getEyeLocation();
        this.sprintResetMethod = defaultSprintResetMethod;
        this.setHitSequence(sequences.normalHit);
        this.setFirstHitMethod(defaultFirstHitMethod);
        this.setStrafeMethod(defaultStrafeMethod);
        this.setComboMethod(defaultComboMethod);
        this.updateFirstHitSequence();
        this.updateStrafeSequence();
        this.comboHitSequence = sequences.normalHit;
        this.sprintResetSequence = sequences.wTap;
        this.motionVectorTowardsTarget = new Vector(0, 0, 0);
        this.motionTowardsTarget = 0;
        this.blockSpeed = 0;

        this.ticksSinceAttack = 0;
        this.ticksSinceDamage = 0;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.firstHit = false;
        this.hitType = SPRINT_HIT;
        this.doSTap = false;
    }

    private void createProperties() {
        this.properties = bot.getProperties();
        properties.set("reach", 3.0F, Float.class);
        properties.set("ping", 0, Integer.class);
        properties.set("jumpReset", false, Boolean.class);
        properties.set("randomComboMethod", false, Boolean.class);
        properties.set("counterRunning", false, Boolean.class); // Experimental
        properties.set("counterCrits", false, Boolean.class); // Experimental
        //properties.set("strafe", false, Boolean.class);
    }

    @Override
    public void tick() {
        this.updateTarget();

        this.updateAim();

        this.updateSequences();

        this.tickTest();

        this.hitSequence.tick();
        this.strafeSequence.tick();

        this.blockSpeed = bot.getEyeLocation().distance(lastLoc);
        this.lastLoc = bot.getEyeLocation();
        this.ticksSinceAttack++;
        this.ticksSinceDamage++;

        ticks++;
    }

    private void tickTest() {
        if (PvpBotPlugin.debug) {
            if (this.blockSpeed == 0 && bot.getEyeLocation().distance(lastLoc) > 0) {
                //Bukkit.broadcastMessage("Bot velocity (server): " + MathHelper.roundTo(bot.getEyeLocation().distance(lastLoc), 3) + ", " + LogUtils.getTimeString());
            }
        }
        if (getPingDistance() < 6.0F) {
            if (target.getPlayer().getItemInHand().getType() != Material.GOLD_INGOT) return;
            //double locationPing = MathHelper.roundTo(target.getPlayer().getEyeLocation().toVector().getZ(), 3);
            //double locationReal = MathHelper.roundTo(target.getHeadLocation().toVector().getZ(), 3);
            //Bukkit.broadcastMessage("SERVER location: " + locationPing + "(real " + locationReal + "), " + LogUtils.getTimeString());
            //Vector loc1 = target.getPlayer().getEyeLocation().toVector();
            Vector loc1 = target.getHeadLocation().toVector();
            Vector loc2 = bot.getEyeLocation().toVector();
            double distance = MathHelper.roundTo(loc1.distance(loc2), 3);
            double distance2 = MathHelper.roundTo(BoundingBoxUtils.distanceTo(bot.getEyeLocation(), target.getBoundingBox()), 3);
            double location = MathHelper.roundTo(bot.getEyeLocation().toVector().getZ(), 3);
            double locP = MathHelper.roundTo(loc1.getZ(), 3);
            double locB = MathHelper.roundTo(loc2.getZ(), 3);
            //Bukkit.broadcastMessage("SERVER location: " + location + ", " + LogUtils.getTimeString());
            //Bukkit.broadcastMessage("SERVER distance: " + distance + ", " + LogUtils.getTimeString());
            Bukkit.broadcastMessage("SERVER P: " + locP + " B: " + locB + " D: " + distance + " D2: " + distance2 + ", " + LogUtils.getTimeString());
        }
    }

    protected void updateTarget() {
        target.update();
        Location targetLocation = target.getHeadLocation();
        double distanceDelta = targetLocation.distance(lastLoc) - targetLocation.distance(bot.getEyeLocation());
        this.motionVectorTowardsTarget = VectorUtils.blockSpeedToMotion(VectorUtils.getVectorFromTo(lastLoc, targetLocation).normalize().multiply(distanceDelta));
        this.motionTowardsTarget = this.motionVectorTowardsTarget.length() * Math.signum(distanceDelta);
    }

    public Target selectTarget() {
        return new Target(bot.getOwner(), bot);
    }

    public void updateAim() {
        if (this.getTarget() != null) {
            this.rotateToTarget();
        }
    }

    public void updateSequences() {
        if (this.botCombo > 0 && this.ticksSinceAttack > 20) {
            this.botCombo = 0;
        }

        if (this.hitSequence.finished) {
            this.updateFirstHitSequence();
            this.updateComboHitSequence();
            this.updateSprintResetSequence();
        }

        // Check for first-hit
        boolean startFirstHit = false;
        if (!firstHit && (this.hitSequence.step == 1 || this.hitSequence.finished)) {
            //this.selectFirstHitMethod();
            if (this.firstHitMethod == NORMAL_HIT && this.getPingDistance() > 5 ||
                this.firstHitMethod == HIT_SELECT && this.getPingDistance() > 5 ||
                this.firstHitMethod == BAIT && this.getPingDistance() > 6 ||
                this.firstHitMethod == JUMP_CRIT && this.shouldJumpCrit() ||
                this.firstHitMethod == CRIT_DEFLECTION && this.getPingDistance() > 9
            ) {
                startFirstHit = true;
            }
        }

        /* TODO - crit spam
        if (this.strafeMethod == CRIT_SPAM && this.botCombo >= 2 && this.hitSequence.finished) {
            this.setHitSequence(sequences.jumpAndCrit);
            this.hitSequence.start();
        } else*/
        if (startFirstHit) {
            this.firstHit = true;
            this.setHitSequence(this.firstHitSequence);
            this.hitSequence.start();
        } else if (this.botCombo >= 1 && this.hitSequence.finished) {
            if (bot.getProperties().getBoolean("randomComboMethod")) this.randomizeCombo();
            this.updateStrafeSequence();
            this.setHitSequence(this.comboHitSequence);
            this.hitSequence.start();
        } else if (this.hitSequence.finished) {
            this.setHitSequence(sequences.normalHit);
            this.hitSequence.start();
        }
        if (this.ticksSinceAttack == 1 && this.botCombo >= 2) {
            // Strafing only in combo (for now)
            this.strafeSequence.start();
        }

    }

    public void updateStrafeSequence() {
        switch (this.strafeMethod) {
            case NO_STRAFE -> this.strafeSequence = sequences.noStrafe;
            case CIRCLE -> this.strafeSequence = sequences.circleStrafe;
            case SWITCH -> this.strafeSequence = sequences.switchStrafe;
            //case CRIT_SPAM: // TODO - crit spam
            //    this.comboSequence = sequences.critSpam;
            //    return;
        }
    }

    public void updateFirstHitSequence() {
        switch (this.firstHitMethod) {
            case NORMAL_HIT -> this.firstHitSequence = bot.getProperties().getBoolean("counterCrits") ?
                    sequences.normalHit_counterCrits :
                    sequences.normalHit;
            case HIT_SELECT -> this.firstHitSequence = sequences.hitSelect;
            case BAIT -> this.firstHitSequence = sequences.bait;
            case JUMP_CRIT -> this.firstHitSequence = sequences.jumpAndCrit;
            case CRIT_DEFLECTION -> this.firstHitSequence = sequences.critDeflection;
        }
    }

    public void updateComboHitSequence() {
        switch (this.comboHitMethod) {
            case NORMAL_HIT -> this.comboHitSequence = sequences.normalHit;
            case UPPERCUT -> this.comboHitSequence = sequences.upperCut;
            case WASD_HIT -> this.comboHitSequence = sequences.wasdHit;
            case HOP -> this.comboHitSequence = sequences.hop;
        }
    }

    public void updateSprintResetSequence() {
        switch (this.sprintResetMethod) {
            case W_TAP -> this.sprintResetSequence = bot.getProperties().getBoolean("counterRunning") ?
                    sequences.wTap_counterRunning :
                    sequences.wTap;
            case S_TAP -> this.sprintResetSequence = sequences.sTap;
        }
    }

    public void tickStrafe() {
        /* if (this.strafeSequence.finished && this.getPingDistance() < maxStrafeDistance && this.firstHit) {
            this.strafeSequence.start();
        }*/ // TODO - working pre-firsthit strafe
        this.strafeSequence.tick();
    }

    protected void rotateToTarget() {
        Location location = this.getTarget().getHeadLocation();
        this.rotateToLocation(location);
    }

    protected void rotateToLocation(Location location) {
        Location from = bot.getEyeLocation();
        Vector direction = VectorUtils.getVectorFromTo(from, location);
        float vecYaw = VectorUtils.getVectorYaw(direction);
        float vecPitch = VectorUtils.getVectorPitch(direction);
        bot.setRotation(vecYaw, vecPitch);
    }

    public static int ticks = 0;

    public void attack(Player player) {

        if (PvpBotPlugin.debug) {
            ticks = 0;
            double motY = MathHelper.roundTo(target.getMotion().getY(), 3);
            Bukkit.broadcastMessage("[DEBUG] Bot hit: D " + MathHelper.roundTo((float) getPingDistance(), 3) + ", tMotY: " + motY + ", tMotTB: " + MathHelper.roundTo(target.blockSpeedTowardsBot, 3) + ", "  + LogUtils.getTimeString());
            //Bukkit.broadcastMessage("[DEBUG] Bot hit: reach " + MathHelper.roundTo((float) getPingDistance(), 3) + ", " + LogUtils.getTimeString() + " (0)");
        }

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

        // Update sprint state if needed
        if (knockback && bot.isSprinting()) {
            if (!this.isFreshSprint()) {
                bot.setSprinting(false);
            }
            Vector mot = bot.getMotion();
            bot.setMot(mot.getX() * 0.6, mot.getY(), mot.getZ() * 0.6);
        }

        // Attack entity
        bot.attack(player);

        // Restore sprint state to not affect later movement
        bot.setSprinting(wasSprinting);

        this.ticksSinceAttack = 0;
        if (bot.isSprinting()) {
            this.setFreshSprint(false);
        }

        this.firstHit = false;

    }

    public boolean shouldJumpCrit() {
        return this.getPingDistance() > 7 && this.getPingDistance() < 7.5; // TODO - acutal calculations
    }

    public boolean isTargetComboRunning() {
        if (botCombo >= 2 && target.motion.getY() == 0 && getTarget().blockSpeedTowardsBot <= 0) { // Releasing W / holding S while on ground
            //Bukkit.broadcastMessage("Running detected! - Holding S before hit");
            return true;
        } else if (botCombo >= 2 && target.hasJumpVelocity() && ( // Pressing S and SPACE to get launched backwards
                getTarget().blockSpeedTowardsBot < -0.5 || // Filter jump resets (block speed = -0.4)
                getTarget().blockSpeedTowardsBot > -0.25 // Detect S and SPACE before getting hit (block speed ~ -0.2)
        )) {
            //Bukkit.broadcastMessage("Running detected! - Holding S and SPACE");
            return true;
        }
        return false;
    }

    // Called whenever bot get damaged
    @Override
    public void onDamageEntity(IDamageSource damageSource) {
        if (!damageSource.getSource().equals("player")) {
            return;
        }
        this.botCombo = 0;
        this.opponentCombo++;
        this.ticksSinceDamage = 0;
    }

    // TODO - sprint reset length calculations
    public int getWTapLength() {
        if (this.botCombo <= 1) {
            return wTapLength;
        } else {
            switch (this.comboMethod) {
                case SWITCH_COMBO:
                case CIRCLE_COMBO:
                    return 3;
                case UPPERCUT:
                    return 6;
                default:
                    return wTapLength; // 7
            }
        }
    }

    public int getSTapLength() {
        if (this.botCombo <= 1) {
            return sTapLength;
        } else {
            switch (this.comboMethod) {
                case SWITCH_COMBO:
                case CIRCLE_COMBO:
                    return 2;
                case UPPERCUT:
                    return 2;
                default:
                    return sTapLength; // 3
            }
        }
    }

    public int getSprintResetDelay() {
        if (this.botCombo <= 1) {
            return sprintResetDelay;
        } else {
            switch (this.comboMethod) {
                case UPPERCUT:
                    return 0;
                default:
                    return sprintResetDelay; // 1
            }
        }
    }

    public boolean isFreshSprint() {
        return this.freshSprint;
    }

    public boolean isSprintResetting() {
        return this.isSprintResetting;
    }

    public SprintResetMethod getSprintResetMethod() {
        return this.sprintResetMethod;
    }

    public void setSprintResetMethod(SprintResetMethod sprintResetMethod) {
        this.sprintResetMethod = sprintResetMethod;
    }

    public void setStrafeMethod(StrafeMethod strafeMethod) {
        this.strafeMethod = strafeMethod;
    }

    public void setFirstHitMethod(HitMethod firstHitMethod) {
        this.firstHitMethod = firstHitMethod;
    }

    public boolean canHit() {

        // Calculate distance to target
        Location eyeLocation = bot.getEyeLocation();
        BoundingBox targetBoundingBox = this.getTarget().getBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(eyeLocation, targetBoundingBox);

        if (distance > this.getReach()) {
            return false;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), this.getReach() + 1);
        return result != null;
    }

    public boolean canAttack() {

        // Check attack cooldown
        float currentHitSpeed = getCurrentHitSpeed();
        if (this.bot.getAttackCooldown() < currentHitSpeed) {
            return false;
        }

        return canHit();
    }

    public boolean canCrit() {
        return bot.canCrit();
    }

    public void setHitSequence(Sequence hitSequence) {
        if (this.hitSequence != null && !this.hitSequence.finished) {
            this.hitSequence.stop();
        }
        this.hitSequence = hitSequence;
    }

    public float getCurrentHitSpeed() {
        return hitSpeed; // TODO - hit speed
    }

    public Sequence getComboHitSequence() {
        return comboHitSequence;
    }

    public void setComboHitSequence(Sequence comboHitSequence) {
        this.comboHitSequence = comboHitSequence;
    }

    public void randomizeCombo() {
        this.setSprintResetMethod(SprintResetMethod.values()[MathHelper.random(0, SprintResetMethod.values().length - 1)]);
        this.setComboMethod(ComboMethod.values()[MathHelper.random(0, ComboMethod.values().length - 1)]);
    }

    public void selectFirstHitMethod() {
        HitMethod[] methods = {NORMAL_HIT, BAIT};
        this.setFirstHitMethod(methods[MathHelper.random(0, methods.length - 1)]);
    }

    protected void doAttack(SwordAi.HitType hitType) {
        if (hitType == CRITICAL_HIT) {
            bot.setFallDistance(1.0F);
            this.doAttack();
            bot.setFallDistance(0.0F);
        } else {
            this.doAttack();
        }
    }

    protected void doAttack() {
        this.attack(this.getTarget().getPlayer());
        bot.swingArm();
    }

    public void setMoveForward(float direction) {
        if (direction == FORWARD && bot.getMoveForward() != FORWARD) {
            this.setFreshSprint(true);
        }
        bot.setMoveForward(direction);
    }

    public void setMoveStrafe(float direction) {
        bot.setMoveStrafe(direction);
    }

    @Override
    public PropertyMap getProperties() {
        return this.properties;
    }

    public double getPingDistance() {
        return BoundingBoxUtils.distanceTo(bot.getEyeLocation(), this.getTarget().getBoundingBox());
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public Target getTarget() {
        return target;
    }

    @Override
    public int getPing() {
        return this.getProperties().getInt("ping");
    }

    @Override
    public void setPing(int ping) {
        this.getProperties().set("ping", Math.max(ping, 0));
    }

    @Override
    public Bot getBot() {
        return bot;
    }

    public void setReach(float reach) {
        this.getProperties().set("reach", reach);
    }

    public float getReach() {
        return this.getProperties().getFloat("reach");
    }

    public void setFreshSprint(boolean freshSprint) {
        this.freshSprint = freshSprint;
    }

    public HitMethod getComboHitMethod() {
        return comboHitMethod;
    }

    public void setComboHitMethod(HitMethod comboHitMethod) {
        this.comboHitMethod = comboHitMethod;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.setMoveForward(0);
            this.setMoveStrafe(0);
            this.botCombo = 0;
            this.opponentCombo = 0;
        }
    }

    public void setComboMethod(ComboMethod comboMethod) {
        this.comboMethod = comboMethod;
        switch (comboMethod) {
            case STRAIGHT_LINE:
                this.setComboHitMethod(NORMAL_HIT);
                this.setStrafeMethod(NO_STRAFE);
                return;
            case UPPERCUT:
                this.setComboHitMethod(UPPERCUT);
                this.setStrafeMethod(NO_STRAFE);
                return;
            case SWITCH_COMBO:
                this.setComboHitMethod(NORMAL_HIT);
                this.setStrafeMethod(SWITCH);
                return;
            case CIRCLE_COMBO:
                this.setComboHitMethod(NORMAL_HIT);
                this.setStrafeMethod(CIRCLE);
                return;
            case WASD_SPAM:
                this.setComboHitMethod(WASD_HIT);
                this.setStrafeMethod(NO_STRAFE); // Strafe movement is contained in WASD_HIT
                return;
            case HOP:
                this.setComboHitMethod(HOP);
                this.setStrafeMethod(NO_STRAFE);
                return;
        }
    }

    public enum HitType {
        SPRINT_HIT, CRITICAL_HIT
    }

    public enum SprintResetMethod {
        W_TAP, S_TAP
    }

    public enum StrafeMethod {
        NO_STRAFE, CIRCLE, SWITCH
    }

    public enum HitMethod {
        NORMAL_HIT, HIT_SELECT, BAIT, CRIT_DEFLECTION, JUMP_CRIT, UPPERCUT, WASD_HIT, HOP
    }

    public enum ComboMethod {
        STRAIGHT_LINE, CIRCLE_COMBO, SWITCH_COMBO, UPPERCUT, WASD_SPAM, HOP
    }

}
