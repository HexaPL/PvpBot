package com.github.hexa.pvpbot.ai;

public class Timer {

    public int ticks;
    public int length;
    public boolean finished;

    public Timer(int length) {
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

}
