package com.github.hexa.pvpbot.util;

public class MathHelper {

    public static int ceil(float f) {
        return (int) Math.ceil(f);
    }

    public static int floor(float f) {
        return (int) Math.floor(f);
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

}
