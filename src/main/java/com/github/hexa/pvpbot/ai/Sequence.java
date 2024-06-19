package com.github.hexa.pvpbot.ai;

public abstract class Sequence {

    public Timer timer;
    public Sequence subSequence;

    public int totalSteps;
    public int step;
    public boolean finished;
    public boolean keepStep;
    public boolean nextStep;

    public Sequence(int totalSteps) {
        this.timer = null;
        this.subSequence = null;
        this.totalSteps = totalSteps;
        this.finished = true;
    }

    public static Sequence empty() {
        return SequenceBuilder.emptySequence();
    }

    public void start() {
        if (!this.finished) {
            this.stop();
        }
        this.finished = false;
        this.step = 1;
        this.keepStep = false;
        this.nextStep = false;
        this.onStart();
    }

    public void tick() {
        if (this.finished) {
            return;
        }
        this.onTick();

        if (keepStep) {
            this.keepStep = false;
        } else {
            this.step++;
            if (this.timer != null) {
                this.timer.stop();
            }
        }
        if (this.step > this.totalSteps) {
            this.finished = true;
            return;
        }
        if (this.nextStep) {
            this.nextStep = false;
            this.tick();
        }
    }

    public void stop() {
        if (this.subSequence != null && !this.subSequence.finished) {
            this.subSequence.stop();
        }
        if (this.hasTimer()) {
            this.stopTimer();
        }
        this.onStop();
        this.finished = true;
    }

    public void onStart() {}

    public void onTick() {}

    public void onStop() {}

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

    public void nextStep() {
        this.nextStep = true;
    }

    public boolean wait(int ticks) {
        if (ticks == 0) {
            this.nextStep();
            return true;
        }

        if (ticks == 1) {
            return true;
        }

        if (this.timer == null) {
            this.timer = new Timer(this, ticks);
        }

        this.timer.update();
        if (this.timer.finished) {
            this.timer = null;
            return true;
        }

        this.keepStep();
        return false;
    }

    public boolean waitUntil(Condition condition) {
        if (condition.isTrue()) {
            this.nextStep();
            return true;
        }
        this.keepStep = true;
        return false;
    }

    public boolean waitUntil(Condition condition, int expireIn) {
        return waitUntil(condition, expireIn, SequenceBlock.empty());
    }

    public boolean waitUntil(Condition condition, int expireIn, SequenceBlock onExpire) {
        if (waitUntil(condition)) {
            this.stopTimer();
            return true;
        }
        if (this.wait(expireIn)) {
            this.keepStep = false;
            return true;
        }
        return false;
    }

    public void tickSubsequence(Sequence subSequence) {
        if (this.subSequence != subSequence) {
            if (this.subSequence != null && !this.subSequence.finished) {
                this.subSequence.stop();
            }
            this.subSequence = subSequence;
            this.subSequence.start();
        }

        this.subSequence.tick();
        if (this.subSequence.finished) {
            this.subSequence = null;
            return;
        }

        this.keepStep = true;
    }

    public void tickSubsequence(Sequence subSequence, boolean breakOnStop) {
        this.tickSubsequence(subSequence);
        if (breakOnStop && this.subSequence.finished) {
            this.stop();
        }
    }

    public void stopSubsequence() {
        if (this.subSequence != null) {
            this.subSequence.stop();
            this.subSequence = null;
        }
    }

    public void keepStep() {
        this.keepStep = true;
    }

    public boolean hasTimer() {
        return this.timer != null;
    }

    public void stopTimer() {
        if (this.timer != null) {
            this.timer.stop();
        }
    }



}
