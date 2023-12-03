package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.Sequence;
import com.github.hexa.pvpbot.ai.Timers;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.SprintResetMethod.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.ComboMethod.*;

public class MovementController extends Controller {

    private SprintResetMethod sprintResetMethod;
    private ComboMethod comboMethod;

    private boolean canSprint;
    private boolean freshSprint;
    private boolean isSprintResetting;

    private int lastTickCombo;

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
                    Timers.wait(this, wTapLength);
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

    public MovementController(BotAIBase ai) {
        super(ai);
        this.sprintResetMethod = WTAP;
        this.comboMethod = STRAIGHTLINE;
        this.canSprint = true;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.lastTickCombo = 0;
    }

    @Override
    public void update() {
        this.handleMovement();
        //this.handleSprintResetting();
        sprintReset.tick();
    }

    protected void handleMovement() {

        if (bot.getMoveForward() != FORWARD && !isSprintResetting) {
            bot.setMoveForward(FORWARD);
        }

        // Combo movement
        if (this.ai.botCombo > 1) {
            switch (comboMethod) {
                case STRAIGHTLINE:
                    if (bot.getMoveForward() != FORWARD && !isSprintResetting) {
                        bot.setMoveForward(FORWARD);
                    }
                    break;
                case SWITCH:
                    if (this.lastTickCombo < this.ai.botCombo) {
                        bot.setMoveStrafe(bot.getMoveStrafe() == RIGHT ? LEFT : RIGHT);
                    }
                    break;
            }
        } else {
            bot.setMoveStrafe(0);
        }

        this.lastTickCombo = this.ai.botCombo;
    }

    public void canSprint(boolean canSprint) {
        this.canSprint = canSprint;
    }

    public boolean canSprint() {
        return this.canSprint;
    }

    public void setFreshSprint(boolean freshSprint) {
        if (!freshSprint) {
            sprintReset.start();
        }
        this.freshSprint = freshSprint;
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
