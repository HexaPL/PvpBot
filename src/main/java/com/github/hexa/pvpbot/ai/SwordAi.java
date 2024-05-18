package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.ai.ControllableBot.MoveDirection.*;
import static com.github.hexa.pvpbot.ai.SwordAi.HitType.*;
import static com.github.hexa.pvpbot.ai.SwordAi.ComboMethod.*;
import static com.github.hexa.pvpbot.ai.SwordAi.SprintResetMethod.*;
import static com.github.hexa.pvpbot.ai.SwordAi.FirstHitMethod.*;

public class SwordAi implements Ai {

    public static SprintResetMethod defaultSprintResetMethod = W_TAP;
    public static FirstHitMethod defaultFirstHitMethod = REACH_HIT;
    public static ComboMethod defaultComboMethod = STRAIGHTLINE;

    public static float hitSpeed = 0.93F;
    public static int wTapLength = 7;
    public static int maxStrafeDistance = 15;

    public ControllableBot bot;
    public Target target;
    private boolean enabled;
    private PropertyMap properties;
    public Sequences sequences;

    public int botCombo;
    public int opponentCombo;

    public SprintResetMethod sprintResetMethod;
    public Sequence sprintResetSequence;
    public ComboMethod comboMethod;
    public Sequence comboSequence;
    public FirstHitMethod firstHitMethod;
    public Sequence firstHitSequence;
    public Sequence strafeSequence;

    public Vector motionVectorTowardsTarget;
    public double motionTowardsTarget;

    private boolean canSprint;
    private boolean freshSprint;
    private boolean isSprintResetting;
    private boolean firstHit;
    public int ticksSinceAttack;
    public int ticksSinceDamage;

    public HitType hitType;
    public Sequence hitSequence;
    public boolean isCritting;
    private boolean doSTap;

    private Location lastLoc;


    public SwordAi(ControllableBot bot) {
        this.bot = bot;
        this.sequences = new Sequences();
        this.createProperties();
        this.enabled = true;
        this.target = this.selectTarget();

        this.lastLoc = bot.getEyeLocation();
        this.sprintResetMethod = defaultSprintResetMethod;
        this.setHitSequence(sequences.reachHit);
        this.setComboMethod(defaultComboMethod);
        this.setFirstHitMethod(defaultFirstHitMethod);
        this.strafeSequence = sequences.switchStrafe;
        this.sprintResetSequence = sequences.sprintReset;
        this.motionVectorTowardsTarget = new Vector(0, 0, 0);
        this.motionTowardsTarget = 0;
        this.ticksSinceAttack = 0;
        this.ticksSinceDamage = 0;
        this.canSprint = true;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.firstHit = false;
        this.hitType = SPRINT_HIT;
        this.isCritting = false;
        this.doSTap = false;
    }

    @Override
    public void tick() {
        this.updateTarget();

        this.updateAim();

        this.updateHitSequence();
        this.hitSequence.tick();

        this.updateStrafeSequence();
        this.tickStrafe();

        this.tickSprintReset();
        this.tickCombo();

        this.lastLoc = bot.getEyeLocation();
        this.ticksSinceAttack++;
        this.ticksSinceDamage++;
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

    public void updateHitSequence() {
        // TODO - jumpcrits based on velocity

        if (this.botCombo > 0 && this.ticksSinceAttack > 20) {
            this.botCombo = 0;
        }

        // Check for first-hit
        boolean startFirstHit = false;
        if (!firstHit) {
            if (this.firstHitMethod == REACH_HIT && this.getPingDistance() > 4 ||
                this.firstHitMethod == HIT_SELECT && this.getPingDistance() > 5 ||
                this.firstHitMethod == BAIT && this.getPingDistance() > 6 ||
                this.firstHitMethod == NOKB_CRIT && this.getPingDistance() > 8) {
                startFirstHit = true;
            }
        }

        if (this.comboMethod == CRIT_SPAM && this.botCombo >= 2 && this.hitSequence.finished) {
            this.setHitSequence(sequences.jumpAndCrit);
            this.hitSequence.start();
        } else if (startFirstHit) {
            this.firstHit = true;
            this.setHitSequence(this.firstHitSequence);
            this.hitSequence.start();
        } else if (this.hitSequence.finished) {
            this.setHitSequence(sequences.reachHit);
            this.hitSequence.start();
        }
    }

    public void updateStrafeSequence() {
        this.strafeSequence = sequences.switchStrafe;
    }

    public void tickSprintReset() {
        if (this.comboMethod != WASD) {
            sprintResetSequence.tick();
        }
    }

    public void tickCombo() {
        comboSequence.tick();
    }

    public void tickStrafe() {
        if (!bot.getProperties().getBoolean("strafe")) {
            return;
        }
        if (this.strafeSequence.finished && this.getPingDistance() < maxStrafeDistance && this.firstHit) {
            this.strafeSequence.start();
        }
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

        // Trigger sprint reset
        if (bot.isSprinting() && this.comboMethod != WASD) {
            if (this.sprintResetMethod == S_TAP && this.firstHit) {
                this.doSTap = true;
            }
            sprintResetSequence.start();
        }

        // Trigger combo
        if (this.botCombo > 1 || this.comboMethod == WASD) {
            comboSequence.start();
        }

        this.firstHit = false;

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
        if (!comboSequence.finished) {
            comboSequence.stop();
        }
    }

    private int getSprintResetLength() {
        if (this.botCombo <= 1) {
            return 8;
        } else {
            switch (this.comboMethod) {
                case STRAIGHTLINE:
                    return wTapLength;
                case SWITCH:
                    return 4;
                case UPPERCUT:
                    return 3;
                case CRIT_SPAM:
                    return 5;
                default:
                    return wTapLength;
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

    public void setComboMethod(ComboMethod comboMethod) {
        if (this.comboMethod == comboMethod) {
            return;
        }
        if (this.comboSequence != null && !this.comboSequence.finished) {
            this.comboSequence.stop();
        }
        this.comboMethod = comboMethod;
        switch (comboMethod) {
            case STRAIGHTLINE:
                this.comboSequence = sequences.straightlineCombo;
                return;
            case CIRCLE:
                this.comboSequence = sequences.circleCombo;
                return;
            case SWITCH:
                this.comboSequence = sequences.switchCombo;
                return;
            case WASD:
                this.comboSequence = sequences.wasdCombo;
                return;
            case UPPERCUT:
                this.comboSequence = sequences.uppercutCombo;
                return;
            case CRIT_SPAM:
                this.comboSequence = sequences.critSpam;
                return;
        }
    }

    public void setFirstHitMethod(FirstHitMethod firstHitMethod) {
        if (this.firstHitMethod == firstHitMethod) {
            return;
        }
        this.firstHitMethod = firstHitMethod;
        switch (firstHitMethod) {
            case REACH_HIT:
                this.firstHitSequence = sequences.reachHit;
                break;
            case HIT_SELECT:
                this.firstHitSequence = sequences.hitSelect;
                break;
            case BAIT:
                this.firstHitSequence = sequences.bait;
                break;
            case NOKB_CRIT:
                this.firstHitSequence = sequences.jumpAndCrit;
                break;
        }
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
        switch (this.comboMethod) {
            case UPPERCUT:
                return 0.95F;
            case CRIT_SPAM:
                return hitSpeed;
            default:
                return hitSpeed;
        }
    }

    public void randomizeCombo() {
        ComboMethod randomMethod = ComboMethod.values()[MathHelper.random(0, ComboMethod.values().length - 1)];
        if (randomMethod == CRIT_SPAM) {
            this.randomizeCombo();
            return;
        }
        this.setComboMethod(randomMethod);
    }

    private void doAttack(SwordAi.HitType hitType) {
        if (hitType == CRITICAL_HIT) {
            bot.setFallDistance(1.0F);
            this.doAttack();
            bot.setFallDistance(0.0F);
        } else {
            this.doAttack();
        }
    }

    public void doAttack() {
        this.attack(this.getTarget().getPlayer());
        bot.swingArm();
    }

    @Override
    public PropertyMap getProperties() {
        return this.properties;
    }

    private void createProperties() {
        properties = bot.getProperties();
        properties.set("reach", 3.0F, Float.class);
        properties.set("ping", 0, Integer.class);
        properties.set("jumpReset", false, Boolean.class);
        properties.set("strafe", false, Boolean.class);
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

    public void canSprint(boolean canSprint) {
        this.canSprint = canSprint;
    }

    public boolean canSprint() {
        return this.canSprint;
    }

    public void setFreshSprint(boolean freshSprint) {
        this.freshSprint = freshSprint;
    }


    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            bot.setMoveForward(0);
            bot.setMoveStrafe(0);
            this.botCombo = 0;
            this.opponentCombo = 0;
        }
    }

    public class Sequences {

        public final Sequence reachHit = new Sequence(2) {
            @Override
            public void onStart() {
                if (!isSprintResetting()) {
                    bot.setMoveForward(FORWARD);
                }
            }

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(SwordAi.this::canAttack);
                        break;
                    case 2:
                        doAttack();
                        break;
                }
            }
        };

        public final Sequence hitSelect = new Sequence(3) {
            @Override
            public void onStart() {
                if (!isSprintResetting()) {
                    bot.setMoveForward(FORWARD);
                }
            }

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> ticksSinceDamage == 0 || getPingDistance() < 2.0F);
                        break;
                    case 2:
                        this.wait(5);
                        break;
                    case 3:
                        if (canHit()) doAttack();
                        break;
                }
            }
        };

        public final Sequence bait = new Sequence(6) {
            @Override
            public void onStart() {
                if (!isSprintResetting()) {
                    bot.setMoveForward(FORWARD);
                }
            }

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> getPingDistance() < 5.5); // TODO - actual calculations
                        break;
                    case 2:
                        bot.setMoveForward(BACKWARD);
                        isSprintResetting = true;
                        break;
                    case 3:
                        this.wait(5);
                        break;
                    case 4:
                        bot.setMoveForward(FORWARD);
                        setFreshSprint(true);
                        isSprintResetting = false;
                        this.nextStep();
                        break;
                    case 5:
                        this.waitUntil(SwordAi.this::canHit);
                        break;
                    case 6:
                        doAttack();
                        break;
                }
            }

            @Override
            public void onStop() {
                if (isSprintResetting) {
                    this.tickStep(4);
                }
            }
        };

        public final Sequence jumpAndCrit = new Sequence(5) {
            @Override
            public void onStart() {
                if (!isSprintResetting()) {
                    bot.setMoveForward(FORWARD);
                }
            }

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> !isSprintResetting() && getPingDistance() < 7);
                        break;
                    case 2:
                        this.tickSubsequence(sequences.jump);
                        break;
                    case 3:
                        this.wait(7);
                        break;
                    case 4:
                        if (bot.isOnGround()) { // Stop the sequence if bot landed before it could hit the player
                            this.stop();
                            break;
                        }
                        this.nextStep();
                        break;
                    case 5:
                        this.tickSubsequence(crit);
                        break;
                }
            }

            @Override
            public void onStop() {
                this.stopSubsequence();
            }
        };

        public final Sequence crit = new Sequence(3) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        isCritting = true;
                        bot.setMoveForward(0);
                        break;
                    case 2:
                        this.wait(2);
                        break;
                    case 3:
                        boolean canHit = canHit();
                        if (canHit) {
                            doAttack(CRITICAL_HIT);
                        }
                        isCritting = false;
                        break;
                }
            }

            @Override
            public void onStop() {
                isCritting = false;
            }
        };

        public final Sequence sprintReset = new Sequence(4) {
            private int length = 0;

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.wait(1);
                        break;
                    case 2:
                        if (sprintResetMethod == S_TAP && doSTap) {
                            bot.setMoveForward(BACKWARD);
                            length = 3;
                            doSTap = false;
                        } else {
                            bot.setMoveForward(0);
                            length = getSprintResetLength();
                        }
                        isSprintResetting = true;
                        break;
                    case 3:
                        this.wait(length);
                        break;
                    case 4:
                        bot.setMoveForward(FORWARD);
                        setFreshSprint(true);
                        isSprintResetting = false;
                        break;
                }
            }

            @Override
            public void onStop() {
                if (!finished && !isFreshSprint()) {
                    this.tickStep(4); // To end sprint reset safely
                }
            }
        };

        public final Sequence straightlineCombo = Sequence.empty();

        public final Sequence switchCombo = new Sequence(3) {
            private int previousDirection = RIGHT;

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        int newDirection = previousDirection == RIGHT ? LEFT : RIGHT;
                        bot.setMoveStrafe(newDirection);
                        previousDirection = newDirection;
                        break;
                    case 2:
                        this.waitUntil(() -> ticksSinceAttack > 15);
                        break;
                    case 3:
                        bot.setMoveStrafe(0);
                        break;
                }
            }

            @Override
            public void onStop() {
                bot.setMoveStrafe(0);
            }
        };

        public final Sequence circleCombo = new Sequence(3) {
            private int direction = MathHelper.random(1, 2);
            private boolean interrupted = false;
            @Override
            public void onStart() {
                if (this.interrupted) {
                    this.interrupted = false;
                    this.direction = MathHelper.random(1, 2);
                }
            }

            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        if (MathHelper.chanceOf(0.15F)) {
                            this.direction = (direction == 1 ? 2 : 1);
                        }
                        bot.setMoveStrafe(direction == 1 ? LEFT : RIGHT);
                        break;
                    case 2:
                        this.waitUntil(() -> ticksSinceAttack > 15);
                        break;
                    case 3:
                        bot.setMoveStrafe(0);
                        break;
                }
            }

            @Override
            public void onStop() {
                if (ticksSinceDamage == 0) {
                    this.interrupted = true;
                }
                bot.setMoveStrafe(0);
            }
        };

        public final Sequence uppercutCombo = new Sequence(2) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.wait(1);
                        break;
                    case 2:
                        bot.jump();
                        break;
                }
            }
        };

        public final Sequence uppercutComboV2 = new Sequence(2) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(SwordAi.this::canAttack);
                        break;
                    case 2:
                        bot.jump();
                        doAttack();
                        break;
                }
            }
        };

        public final Sequence wasdCombo = new Sequence(8) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.wait(1);
                        break;
                    case 2:
                        bot.setMoveForward(0);
                        bot.setMoveStrafe(LEFT);
                        bot.setSprinting(false);
                        isSprintResetting = true;
                        break;
                    case 3:
                        this.wait(1);
                        break;
                    case 4:
                        bot.setMoveForward(BACKWARD);
                        bot.setMoveStrafe(0);
                        break;
                    case 5:
                        this.wait(1);
                        break;
                    case 6:
                        bot.setMoveForward(0);
                        bot.setMoveStrafe(RIGHT);
                        break;
                    case 7:
                        this.wait(1);
                        break;
                    case 8:
                        bot.setMoveForward(FORWARD);
                        bot.setMoveStrafe(0);
                        setFreshSprint(true);
                        isSprintResetting = false;
                        break;
                }
            }

            @Override
            public void onStop() {
                if (!finished && !isFreshSprint()) {
                    this.tickStep(8); // To end sprint reset safely
                }
            }
        };

        // TODO - working sprintcut
        public final Sequence sprintcutCombo = new Sequence(2) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> sprintReset.finished);
                        break;
                    case 2:
                        bot.jump();
                        break;
                }
            }
        };

        public final Sequence critSpam = Sequence.empty(); // Critspam in HitController

        public final Sequence jump = new Sequence(2) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> bot.canJump());
                        break;
                    case 2:
                        bot.jump();
                        break;
                }
            }
        };

        public final Sequence counterStrafe = new Sequence(2) {
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> getPingDistance() < maxStrafeDistance && firstHit);
                        break;
                    case 2:
                        if (getPingDistance() < maxStrafeDistance && firstHit) {
                            bot.setMoveStrafe(target.strafeDirection);
                            this.keepStep();
                        } else {
                            this.stop();
                        }
                        break;
                }
            }

            @Override
            public void onStop() {
                bot.setMoveStrafe(0);
            }
        };
        
        public final Sequence switchStrafe = new Sequence(2) {
            private float lastDirection = 1;
            @Override
            public void onTick() {
                switch (step) {
                    case 1:
                        this.waitUntil(() -> getPingDistance() < maxStrafeDistance && firstHit);
                        break;
                    case 2:
                        if (getPingDistance() < maxStrafeDistance && firstHit) {
                            bot.setMoveStrafe(lastDirection * -1);
                            this.keepStep();
                        } else {
                            this.stop();
                        }
                        break;
                }
            }

            @Override
            public void onStop() {
                lastDirection = bot.getMoveStrafe();
                bot.setMoveStrafe(0);
            }
        };

    }

    public enum HitType {
        SPRINT_HIT, CRITICAL_HIT
    }

    public enum SprintResetMethod {
        W_TAP, S_TAP
    }

    public enum ComboMethod {
        STRAIGHTLINE, CIRCLE, SWITCH, WASD, UPPERCUT, CRIT_SPAM
    }

    public enum FirstHitMethod {
        REACH_HIT, HIT_SELECT, BAIT, NOKB_CRIT
    }

}
