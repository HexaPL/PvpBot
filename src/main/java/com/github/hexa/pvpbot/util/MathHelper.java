package com.github.hexa.pvpbot.util;

public class MathHelper {

    public static int random(int min, int max) {
        return min + (int)(Math.random() * ((max - min) + 1));
    }

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

    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static long roundToNumber(long x, int to) {
        return (x + (to - 1)) / to * to;
    }

    public static float roundTo(float var, int count) {
        double decimal = Math.pow(10, count);
        return (float) (Math.round(var * decimal) / decimal);
    }

}
