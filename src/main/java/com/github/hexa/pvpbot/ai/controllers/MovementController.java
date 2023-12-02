package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.SprintResetMethod.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.ComboMethod.*;

public class MovementController extends Controller {

    private SprintResetMethod sprintResetMethod;
    private ComboMethod comboMethod;

    private boolean canSprint;
    private int sprintResetDelay;
    private int sprintResetLength;
    private boolean freshSprint;
    private boolean isSprintResetting;

    private int sprintTicks;
    private int lastTickCombo;

    public int test = 6;

    public MovementController(BotAIBase ai) {
        super(ai);
        this.sprintResetMethod = WTAP;
        this.comboMethod = STRAIGHTLINE;
        this.canSprint = true;
        this.sprintTicks = -1;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.lastTickCombo = 0;
    }

    @Override
    public void update() {
        this.handleMovement();
        this.handleSprintResetting();
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

    protected void handleSprintResetting() {

        // Check if any action is required
        if (!this.canSprint() || this.sprintTicks == -1) {
            return;
        }

        // Calculate sprint reset time values
        // Hardcoded values for 'infinite' combo
        // TODO - delay calculation based on distance and velocity
        this.sprintResetDelay = 1;
        if (this.ai.botCombo > 1) { // In combo
            if (sprintResetMethod == WTAP) {
                if (this.comboMethod == STRAIGHTLINE) {
                    this.sprintResetLength = 9;
                } else if (this.comboMethod == SWITCH) {
                    this.sprintResetLength = test;
                }
            } else if (sprintResetMethod == STAP) {
                this.sprintResetLength = 6;
            }
        } else { // In trade
            this.sprintResetLength = 6;
        }

        // Start sprint reset if needed
        if (bot.isSprinting() && bot.getMoveForward() > 0 && !this.freshSprint && !this.isSprintResetting && this.sprintTicks >= this.sprintResetDelay) {
            bot.setSprinting(false);
            this.isSprintResetting = true;
            this.startSprintReset(this.sprintResetMethod);
            this.sprintTicks = 0;
        }

        // End sprint reset if needed
        if (isSprintResetting && this.sprintTicks >= sprintResetLength) {
            bot.setSprinting(true);
            this.freshSprint = true;
            this.isSprintResetting = false;
            this.endSprintReset(this.sprintResetMethod);
            this.sprintTicks = -1;
        }

        if (this.sprintTicks != -1) this.sprintTicks++;

    }

    protected void startSprintReset(SprintResetMethod method) {

        switch (method) {
            case WTAP:
                bot.setMoveForward(0);
                break;
            case STAP:
                bot.setMoveForward(BACKWARD);
                break;
        }

    }

    protected void endSprintReset(SprintResetMethod method) {
        switch (method) {
            case WTAP:
            case STAP:
                bot.setMoveForward(FORWARD);
        }
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

    public boolean isFreshSprint() {
        return this.freshSprint;
    }

    public boolean isSprintResetting() {
        return this.isSprintResetting;
    }

    public int getSprintTicks() {
        return this.sprintTicks;
    }

    public void setSprintTicks(int sprintTicks) {
        this.sprintTicks = sprintTicks;
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
