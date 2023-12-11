package com.github.hexa.pvpbot.ai;

public class Timer {

    public Sequence sequence;
    public int ticks;
    public int length;
    public boolean finished;

    public Timer(Sequence sequence, int length) {
        this.sequence = sequence;
        this.ticks = 0;
        this.length = length;
        this.finished = false;
    }

    public void update() {
        ticks++;
        if (ticks >= length) {
            this.finished = true;
        }
    }

    public void stop() {
        this.finished = true;
        sequence.timer = null;
    }

}
