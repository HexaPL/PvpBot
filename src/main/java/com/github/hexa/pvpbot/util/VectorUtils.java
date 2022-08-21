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
        return to.toVector().subtract(from.toVector());
    }

    public static Location getLocationZero() {
        return new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
    }

}
