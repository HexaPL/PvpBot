package com.github.hexa.pvpbot.ai;

public class Sequences {

    public static Sequence empty() {
        return new Sequence(1) {
            @Override
            public void tick() {
                if (finished) return;
                super.tick();
            }
        };
    }

    public static Sequence createSimpleSequence(int steps, SimpleSequence simpleSequence) {
        return new Sequence(steps) {
            @Override
            public void tick() {
                if (finished) return;
                simpleSequence.steps();
                super.tick();
            }
        };
    }

}
