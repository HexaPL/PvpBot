package com.github.hexa.pvpbot.ai;

public abstract class Sequence {

    public int totalSteps;
    public int step;
    public boolean finished;
    public boolean keepStep;
    public boolean isOnTimer;

    public Sequence(int totalSteps) {
        this.totalSteps = totalSteps;
        this.finished = true;
    }

    public void start() {
        if (!this.finished) {
            this.stop();
        }
        this.finished = false;
        this.step = 1;
        this.keepStep = false;
        this.isOnTimer = false;
    }

    public void tick() {
        if (this.finished) {
            return;
        }
        if (this.step == this.totalSteps) {
            this.finished = true;
            return;
        }
        if (keepStep) {
            this.keepStep = false;
        }
        else if (!isOnTimer) {
            this.step++;
        }
    }

    public void tickStep(int step) {
        int backup = this.step;
        this.step = step;
        if (!this.finished) {
            this.tick();
        } else {
            this.finished = false;
            this.tick();
            this.finished = true;
        }
        this.step = backup;
    }

    public void stop() {
        this.finished = true;
    }

}
