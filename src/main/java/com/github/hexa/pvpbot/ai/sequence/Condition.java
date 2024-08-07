package com.github.hexa.pvpbot.ai.sequence;

public interface Condition {

    public boolean check();

    default public boolean isTrue() {
        return check();
    }

    default public boolean isFalse() {
        return !isTrue();
    }

}
