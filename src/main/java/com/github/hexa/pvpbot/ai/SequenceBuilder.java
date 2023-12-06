package com.github.hexa.pvpbot.ai;

public class SequenceBuilder {

    SequenceBlock start;
    SequenceBlock tick;
    SequenceBlock stop;
    int totalSteps;

    public SequenceBuilder() {
        this.start = SequenceBlock.empty();
        this.tick = SequenceBlock.empty();
        this.stop = SequenceBlock.empty();
        this.totalSteps = 1;
    }

    public static SequenceBuilder create() {
        return new SequenceBuilder();
    }

    public SequenceBuilder onStart(SequenceBlock start) {
        this.start = start;
        return this;
    }

    public SequenceBuilder onTick(int totalSteps, SequenceBlock main) {
        this.totalSteps = totalSteps;
        this.tick = main;
        return this;
    }

    public SequenceBuilder onStop(SequenceBlock stop) {
        this.stop = stop;
        return this;
    }

    public Sequence save() {
        return new Sequence(this.totalSteps) {
            @Override
            public void start() {
                start.steps();
                super.start();
            }

            @Override
            public void tick() {
                if (finished) return;
                tick.steps();
                super.tick();
            }

            @Override
            public void stop() {
                stop.steps();
                super.stop();
            }
        };
    }

    public static Sequence emptySequence() {
        return SequenceBuilder.create().save();
    }

}
