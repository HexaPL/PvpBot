package com.github.hexa.pvpbot.util;

public class RotationUtils {

    public static byte toByte(float rotation) {
        return (byte) Math.floor(rotation * 256 / 360);
    }

    public static float fromByte(byte rotation) {
        return (rotation * 360F / 256F);
    }

}
