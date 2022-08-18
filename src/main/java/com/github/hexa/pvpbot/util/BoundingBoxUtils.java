package com.github.hexa.pvpbot.util;

import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftVector;

public class BoundingBoxUtils {

    public static double getDistanceToAABB(Location origin, AxisAlignedBB aabb) {
        if (aabb.contains(CraftVector.toNMS(origin.toVector()))) {
            return 0D;
        }
        double dx = Math.max(Math.max(aabb.minX - origin.getX(), origin.getX() - aabb.maxX), 0);
        double dz = Math.max(Math.max(aabb.minZ - origin.getZ(), origin.getZ() - aabb.maxZ), 0);
        double dy = Math.max(Math.max(aabb.minY - origin.getY(), origin.getY() - aabb.maxY), 0);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
