package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.*;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.SprintResetMethod.*;

public class MovementController extends Controller {

    private boolean canSprint;
    private int sprintResetDelay;
    private int sprintResetLength;
    private SprintResetMethod sprintResetMethod;
    private boolean freshSprint;
    private boolean isSprintResetting;

    private int sprintTicks;

    public MovementController(BotAIBase ai) {
        super(ai);
        this.canSprint = true;
        this.sprintResetMethod = WTAP;
        this.sprintTicks = -1;
        this.freshSprint = true;
        this.isSprintResetting = false;
    }

    @Override
    public void update() {
        this.handleMovement();
        this.handleSprintResetting();
    }

    protected void handleMovement() {
        if (isSprintResetting) {
            return;
        }
        if (bot.getMoveForward() != FORWARD) {
            bot.setMoveForward(FORWARD);
        }
    }

    protected void handleSprintResetting() {

        // Check if any action is required
        if (!this.canSprint() || this.sprintTicks == -1) {
            return;
        }

        // Check for offensive/defensive sprint reset
        this.sprintResetDelay = 1;
        this.sprintResetLength = (this.ai.botCombo > 1 ? 9 : 1); // 9 = optimal length for straightline combo with sword

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
        WTAP, STAP, BLOCKHIT
    }

}
