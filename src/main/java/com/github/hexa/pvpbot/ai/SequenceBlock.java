package com.github.hexa.pvpbot.ai;

public interface SequenceBlock {

    public void steps();

    public static SequenceBlock empty() {
        return () -> {};
    }

}
