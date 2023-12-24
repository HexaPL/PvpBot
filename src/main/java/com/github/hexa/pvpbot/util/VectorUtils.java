package com.github.hexa.pvpbot.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {

    public static float getVectorYaw(Vector vector) {
        Location location = getLocationZero();
        location.setDirection(vector);
        return location.getYaw();
    }


    public static float getVectorPitch(Vector vector) {
        Location location = getLocationZero();
        location.setDirection(vector);
        return location.getPitch();
    }

    public static Vector getVectorFromTo(Location from, Location to) {
        return getVectorFromTo(from.toVector(), to.toVector());
    }

    public static Vector getVectorFromTo(Vector from, Vector to) {
        return to.subtract(from);
    }

    public static Location getLocationZero() {
        return new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
    }

    public static Vector motionToBlockSpeed(Vector motion) {
        return motion.multiply(1.835);
    }

    public static Vector blockSpeedToMotion(Vector blockSpeed) {
        return blockSpeed.multiply(1 / 1.835);
    }

}
