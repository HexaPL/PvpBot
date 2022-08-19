package com.github.hexa.pvpbot.util;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;

public class BoundingBoxUtils {

    public static double distanceTo(Location origin, BoundingBox box) {
        if (box.contains(origin.toVector())) {
            return 0D;
        }
        double dx = Math.max(Math.max(box.getMinX() - origin.getX(), origin.getX() - box.getMaxX()), 0);
        double dz = Math.max(Math.max(box.getMinZ() - origin.getZ(), origin.getZ() - box.getMaxZ()), 0);
        double dy = Math.max(Math.max(box.getMinY() - origin.getY(), origin.getY() - box.getMaxY()), 0);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
