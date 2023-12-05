package com.github.hexa.pvpbot.ai;

import java.util.HashMap;

public class Timers {

    private static HashMap<Sequence, Timer> timers = new HashMap<>();

    public static void updateAll() {
        for (Timer timer : timers.values()) {
            timer.update();
        }
    }

    public static HashMap<Sequence, Timer> getTimers() {
        return timers;
    }

    public static void wait(Sequence sequence, int ticks) {
        if (!timers.containsKey(sequence)) {
            timers.put(sequence, new Timer(ticks));
            sequence.isOnTimer = true;
        }
        Timer timer = timers.get(sequence);
        timer.update();
        if (timer.finished) {
            sequence.isOnTimer = false;
            timers.remove(sequence);
        }
    }

    public static void waitUntil(Sequence sequence, Condition condition) {
        sequence.isOnTimer = true;
        if (condition.isTrue()) {
            sequence.isOnTimer = false;
        }
    }

}
