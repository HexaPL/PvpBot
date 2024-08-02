package com.github.hexa.pvpbot.ai.sequence;

public interface SequenceBlock {

    public void execute();

    public static SequenceBlock empty() {
        return () -> {};
    }

}
