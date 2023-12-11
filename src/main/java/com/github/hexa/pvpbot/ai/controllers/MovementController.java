package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.*;
import com.github.hexa.pvpbot.util.MathHelper;
import org.bukkit.Bukkit;

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

        if (bot.getMoveForward() != FORWARD && !isSprintResetting && !ai.hitController.isCritting) {
            //bot.setMoveForward(FORWARD); // TODO - update moveForward in sequences
        }

    }

    public Sequence sprintReset = new Sequence(4) {
        @Override
        public void onTick() {
            switch (step) {
                case 1:
                    this.wait(1);
                    break;
                case 2:
                    if (sprintResetMethod == WTAP) {
                        bot.setMoveForward(0);
                    } else if (sprintResetMethod == STAP) {
                        bot.setMoveForward(BACKWARD);
                    }
                    bot.setSprinting(false);
                    isSprintResetting = true;
                    break;
                case 3:
                    this.wait(getWTapLength());
                    break;
                case 4:
                    bot.setMoveForward(FORWARD);
                    bot.setSprinting(true);
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

    public Sequence straightlineCombo = Sequence.empty();

    public Sequence switchCombo = new Sequence(3) {
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

    public Sequence circleCombo = new Sequence(3) {
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

    public Sequence uppercutCombo = SequenceBuilder.create().onTick(1, () -> {
        bot.jump();
    }).save();

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
                case 4: // 4
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
            if (!finished && !MovementController.this.isFreshSprint()) {
                this.tickStep(8); // To end sprint reset safely
            }
        }
    };

    // TODO - working sprintcut
    public Sequence sprintcutCombo = new Sequence(2) {
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

    public Sequence critSpam = Sequence.empty(); // Critspam in HitController

    public Sequence jumpSequence = new Sequence(2) {
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
                case CRIT_SPAM:
                    return 5;
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
        if (bot.isSprinting()) {
            setFreshSprint(false);
        }
        if (bot.isSprinting() && this.comboMethod != WASD) {
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
