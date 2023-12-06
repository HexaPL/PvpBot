package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.*;
import com.github.hexa.pvpbot.util.MathHelper;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.controllers.HitController.HitType.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.SprintResetMethod.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.ComboMethod.*;

public class MovementController extends Controller {

    private SprintResetMethod sprintResetMethod;
    public ComboMethod comboMethod;
    public Sequence comboSequence;

    private boolean canSprint;
    private boolean freshSprint;
    private boolean isSprintResetting;
    public int ticksSinceAttack;
    public int ticksSinceDamage;

    public static int wTapLength = 5;

    public MovementController(BotAIBase ai) {
        super(ai);
        this.sprintResetMethod = WTAP;
        this.setComboMethod(CRIT_SPAM);
        this.ticksSinceAttack = 0;
        this.ticksSinceDamage = 0;
        this.canSprint = true;
        this.freshSprint = true;
        this.isSprintResetting = false;
    }

    @Override
    public void update() {
        this.handleMovement();
        if (this.comboMethod != WASD) {
            sprintReset.tick();
        }
        comboSequence.tick();
        this.ticksSinceAttack++;
        this.ticksSinceDamage++;
    }

    protected void handleMovement() {

        if (bot.getMoveForward() != FORWARD && !isSprintResetting) {
            bot.setMoveForward(FORWARD);
        }

    }

    public Sequence sprintReset = new Sequence(4) {
        @Override
        public void tick() {
            if (finished) {
                return;
            }
            switch (step) {
                case 1:
                    Timers.wait(this, 1);
                    break;
                case 2:
                    if (MovementController.this.sprintResetMethod == WTAP) {
                        bot.setMoveForward(0);
                    } else if (MovementController.this.sprintResetMethod == STAP) {
                        bot.setMoveForward(BACKWARD);
                    }
                    bot.setSprinting(false);
                    MovementController.this.isSprintResetting = true;
                    break;
                case 3:
                    Timers.wait(this, getWTapLength());
                    break;
                case 4:
                    bot.setMoveForward(FORWARD);
                    bot.setSprinting(true);
                    MovementController.this.setFreshSprint(true);
                    MovementController.this.isSprintResetting = false;
                    break;
            }
            super.tick();
        }

        @Override
        public void stop() {
            if (!finished && !MovementController.this.isFreshSprint()) {
                this.tickStep(4); // To end sprint reset safely
            }
            super.stop();
        }
    };

    public Sequence straightlineCombo = SequenceBuilder.emptySequence();

    public Sequence switchCombo = new Sequence(3) {
        private int previousDirection = RIGHT;
        @Override
        public void start() {
            super.start();
        }

        @Override
        public void tick() {
            if (finished) return;
            switch (step) {
                case 1:
                    int newDirection = previousDirection == RIGHT ? LEFT : RIGHT;
                    bot.setMoveStrafe(newDirection);
                    previousDirection = newDirection;
                    break;
                case 2:
                    Timers.waitUntil(this, () -> ticksSinceAttack > 15);
                    break;
                case 3:
                    bot.setMoveStrafe(0);
                    break;
            }
            super.tick();
        }

        @Override
        public void stop() {
            bot.setMoveStrafe(0);
            super.stop();
        }
    };

    public Sequence circleCombo = new Sequence(3) {
        private int direction = MathHelper.random(1, 2);
        private boolean interrupted = false;
        @Override
        public void start() {
            if (this.interrupted) {
                this.interrupted = false;
                this.direction = MathHelper.random(1, 2);
            }
            super.start();
        }

        @Override
        public void tick() {
            if (finished) return;
            switch (step) {
                case 1:
                    if (MathHelper.chanceOf(0.15F)) {
                        this.direction = (direction == 1 ? 2 : 1);
                    }
                    bot.setMoveStrafe(direction == 1 ? LEFT : RIGHT);
                    break;
                case 2:
                    Timers.waitUntil(this, () -> ticksSinceAttack > 15);
                    break;
                case 3:
                    bot.setMoveStrafe(0);
                    break;
            }
            super.tick();
        }

        @Override
        public void stop() {
            if (ticksSinceDamage == 0) {
                this.interrupted = true;
            }
            bot.setMoveStrafe(0);
            super.stop();
        }
    };

    public Sequence uppercutCombo = SequenceBuilder.create().onTick(1, () -> {
        bot.jump();
    }).save();

    public final Sequence wasdCombo = new Sequence(8) {
        @Override
        public void tick() {
            if (finished) return;
            switch (step) {
                case 1:
                    Timers.wait(this, 1);
                    break;
                case 2:
                    bot.setMoveForward(0);
                    bot.setMoveStrafe(LEFT);
                    bot.setSprinting(false);
                    MovementController.this.isSprintResetting = true;
                    break;
                case 3:
                    Timers.wait(this, 1);
                    break;
                case 4: // 4
                    bot.setMoveForward(BACKWARD);
                    bot.setMoveStrafe(0);
                    break;
                case 5:
                    Timers.wait(this, 1);
                    break;
                case 6:
                    bot.setMoveForward(0);
                    bot.setMoveStrafe(RIGHT);
                    break;
                case 7:
                    Timers.wait(this, 1);
                    break;
                case 8:
                    bot.setMoveForward(FORWARD);
                    bot.setMoveStrafe(0);
                    MovementController.this.setFreshSprint(true);
                    MovementController.this.isSprintResetting = false;
                    break;
            }
            super.tick();
        }

        @Override
        public void stop() {
            if (!finished && !MovementController.this.isFreshSprint()) {
                this.tickStep(8); // To end sprint reset safely
            }
            super.stop();
        }
    };

    // TODO - working sprintcut
    public Sequence sprintcutCombo = new Sequence(2) {
        @Override
        public void tick() {
            if (finished) return;
            switch (step) {
                case 1:
                    Timers.waitUntil(this, () -> sprintReset.finished);
                    break;
                case 2:
                    bot.jump();
                    break;
            }
            super.tick();
        }
    };

    public Sequence critSpam = new Sequence(4) {
        private int ticks;
        @Override
        public void tick() {
            if (finished) return;
            switch (step) {
                case 1:
                    Timers.waitUntil(this, () -> bot.canJump());
                    break;
                case 2:
                    bot.jump();
                    ai.hitController.hitType = CRITICAL_HIT;
                    this.ticks = ticksSinceAttack;
                    break;
                case 3:
                    Timers.waitUntil(this, () -> (ticksSinceAttack - this.ticks) > 15);
                    break;
                case 4:
                    ai.hitController.hitType = SPRINT_HIT;
                    break;
            }
            super.tick();
        }

        @Override
        public void stop() {
            if (ticksSinceDamage == 0) {
                ai.hitController.hitType = SPRINT_HIT;
            }
            super.stop();
        }
    };

    private int getWTapLength() {
        if (this.ai.botCombo <= 1) {
            return 8;
        } else {
            switch (this.comboMethod) {
                case STRAIGHTLINE:
                    return wTapLength;
                case SWITCH:
                    return 4;
                case UPPERCUT:
                    return 6;
                default:
                    return wTapLength;
            }
        }
    }

    public void randomizeCombo() {
        ComboMethod randomMethod = ComboMethod.values()[MathHelper.random(0, ComboMethod.values().length - 1)];
        this.setComboMethod(randomMethod);
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

    public void registerAttack() {
        this.ticksSinceAttack = 0;
        //this.randomizeCombo();
        setFreshSprint(false);
        if (this.comboMethod != WASD) {
            sprintReset.start();
        }
        if (ai.botCombo > 1 || this.comboMethod == WASD) {
            comboSequence.start();
            if (comboMethod == UPPERCUT) {
                comboSequence.tick();
            }
        }
    }

    public void registerDamage() {
        this.ticksSinceDamage = 0;
        if (!comboSequence.finished) {
            comboSequence.stop();
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
                this.comboSequence = straightlineCombo;
                return;
            case CIRCLE:
                this.comboSequence = circleCombo;
                return;
            case SWITCH:
                this.comboSequence = switchCombo;
                return;
            case WASD:
                this.comboSequence = wasdCombo;
                return;
            case UPPERCUT:
                this.comboSequence = uppercutCombo;
                return;
            case CRIT_SPAM:
                this.comboSequence = critSpam;
                return;
        }
    }

    public enum SprintResetMethod {
        WTAP, STAP
    }

    public enum ComboMethod {
        STRAIGHTLINE, CIRCLE, SWITCH, WASD, UPPERCUT, CRIT_SPAM
    }

}
