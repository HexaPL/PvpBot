package com.github.hexa.pvpbot.ai.controllers;

import com.github.hexa.pvpbot.ai.BotAIBase;

import static com.github.hexa.pvpbot.ai.BotAIBase.Direction.FORWARD;
import static com.github.hexa.pvpbot.ai.controllers.MovementController.SprintResetMethod.*;

public class MovementController extends Controller {

    private boolean canSprint;
    private boolean sTapSlowdown;
    private int sprintResetDelay = 2;
    private int sprintResetLength = 5;
    private SprintResetMethod sprintResetMethod;
    private boolean freshSprint;
    private boolean isSprintResetting;

    private int sprintTicks;

    public MovementController(BotAIBase ai) {
        super(ai);
        this.canSprint = true;
        this.sTapSlowdown = false;
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
        if (isSprintResetting || sTapSlowdown) {
            return;
        }
        if (bot.getMoveForward() == 0) {
            bot.setMoveForward(FORWARD);
        }
    }

    protected void handleSprintResetting() {

        // Check if any action is required
        if (!this.canSprint() || this.sprintTicks == -1) {
            return;
        }

        // Reset s-tap slowdown to not move backwards
        if (this.sTapSlowdown) {
            this.sTapSlowdown = false;
            bot.setMoveForward(FORWARD);
        }

        // Bukkit.broadcastMessage("handleSprintResetting - sprintTicks" + )

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
                // Simply simulate releasing W key
                bot.setMoveForward(0);
                break;
            case STAP:
                // Do some opposite force to slow down faster
                bot.setMoveForward(-0.2F);
                this.sTapSlowdown = true;
                break;
            case BLOCKHIT:
                // Block sword
                // TODO - blockhit
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
