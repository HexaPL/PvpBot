package com.github.hexa.pvpbot.ai;

public interface SequenceBlock {

    public void execute();

    public static SequenceBlock empty() {
        return () -> {};
    }

}
