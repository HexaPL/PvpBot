package com.github.hexa.pvpbot.util;

import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VectorUtils {

    public static Vec3D bukkitToVec3d(Vector bukkitVector) {
        return new Vec3D(bukkitVector.getX(), bukkitVector.getY(), bukkitVector.getZ());
    }

    public static Vector vec3dToBukkit(Vec3D vec3d) {
        return new Vector(vec3d.getX(), vec3d.getY(), vec3d.getZ());
    }

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

    public static double getVec3dXZLength(Vec3D vector) {
        return Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
    }

    public static Location getLocationZero() {
        return new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
    }

}
