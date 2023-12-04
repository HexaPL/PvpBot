package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.Sequence;
import com.github.hexa.pvpbot.ai.Timers;
import com.github.hexa.pvpbot.util.MathHelper;
import org.bukkit.Bukkit;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
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

    public static int wTapLength = 5;

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
            if (step != 4) {
                this.tickStep(4); // To end sprint reset safely
            }
            super.stop();
        }
    };

    public Sequence switchCombo = new Sequence(1) {
        @Override
        public void tick() {
            if (finished) {
                return;
            }
            bot.setMoveStrafe(bot.getMoveStrafe() == RIGHT ? LEFT : RIGHT);
            super.tick();
        }

        @Override
        public void stop() {
            bot.setMoveStrafe(0);
            super.stop();
        }
    };

    public Sequence circleCombo = new Sequence(1) {
        private int direction;
        @Override
        public void start() {
            this.direction = MathHelper.random(1, 2);
            super.start();
        }

        @Override
        public void tick() {
            if (finished) {
                return;
            }
            if (MathHelper.chanceOf(0.15F)) {
                this.direction = (direction == 1 ? 2 : 1);
            }
            bot.setMoveStrafe(direction == 1 ? LEFT : RIGHT);
            super.tick();
        }

        @Override
        public void stop() {
            bot.setMoveStrafe(0);
            super.stop();
        }
    };

    public MovementController(BotAIBase ai) {
        super(ai);
        this.sprintResetMethod = WTAP;
        this.comboMethod = CIRCLE;
        this.comboSequence = circleCombo;
        this.ticksSinceAttack = 0;
        this.canSprint = true;
        this.freshSprint = true;
        this.isSprintResetting = false;
    }

    @Override
    public void update() {
        this.ticksSinceAttack++;
        this.handleMovement();
        sprintReset.tick();
        comboSequence.tick();
        if (this.ticksSinceAttack > 15) {
            comboSequence.stop();
        }
    }

    protected void handleMovement() {

        if (bot.getMoveForward() != FORWARD && !isSprintResetting) {
            bot.setMoveForward(FORWARD);
        }

    }

    private int getWTapLength() {
        if (this.ai.botCombo <= 1) {
            return 8;
        } else {
            if (this.comboMethod == STRAIGHTLINE) {
                return wTapLength;
            } else if (this.comboMethod == SWITCH) {
                return 4;
            }
        }
        return wTapLength;
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
        setFreshSprint(false);
        sprintReset.start();
        if (ai.botCombo > 1) {
            comboSequence.start();
        }
    }

    public void registerDamage() {
        comboSequence.stop();
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

    public enum SprintResetMethod {
        WTAP, STAP
    }

    public enum ComboMethod {
        STRAIGHTLINE, CIRCLE, SWITCH, WASD, AD_TAP, UPPERCUT
    }

}
