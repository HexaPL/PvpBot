package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.PacketUtils;
import com.github.hexa.pvpbot.util.VectorUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class EntityPlayerBot extends EntityPlayer {

    public String name;
    public EntityPlayer owner;
    public float forward;
    public float strafe;
    public float reach;
    public boolean shouldAttack;

    public EntityPlayerBot(String name, EntityPlayer owner, MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.name = name;
        this.owner = owner;
        this.reach = 3.0F;
        this.forward = 0F;
        this.strafe = 0F;
        this.shouldAttack = false;
    }

    @Override
    public void tick() {
        this.rotateToTarget(this.owner);
        this.updateMovement();
        if (this.shouldAttack) {
            this.tryAttack(this.owner);
        }
        super.tick();
        super.playerTick();
    }

    @Override
    public void playerTick() {
        super.playerTick();
    }

    public void rotateToTarget(EntityPlayer target) {
        Location location = target.getBukkitEntity().getEyeLocation();
        this.rotateToLocation(location);
    }

    public void rotateToLocation(Location location) {
        Location from = this.getBukkitEntity().getEyeLocation();
        Vector direction = VectorUtils.getVectorFromTo(from, location);
        float vecYaw = VectorUtils.getVectorYaw(direction);
        float vecPitch = VectorUtils.getVectorPitch(direction);
        this.setRotation(vecYaw, vecPitch);
    }

    public void setRotation(float yaw, float pitch) {
        this.setHeadRotation(yaw);
        this.setYawPitch(yaw, pitch);
    }

    public void updateMovement() {
        this.aT = this.forward;
        this.aR = this.strafe;
    }

    public void tryAttack(EntityPlayer target) {
        if (this.shouldSwing(target)) {
            this.swingArm();
        }
        if (this.canAttack(target)) {
            this.attack(target);
        }
    }

    public boolean canAttack(EntityPlayer target) {
        double distance = BoundingBoxUtils.getDistanceToAABB(this.getBukkitEntity().getEyeLocation(), target.getBoundingBox());
        if (distance > this.reach) {
            return false;
        }
        return true;
        // TODO - raytracing
    }

    public boolean shouldSwing(EntityPlayer target) {
        double distance = BoundingBoxUtils.getDistanceToAABB(this.getBukkitEntity().getEyeLocation(), target.getBoundingBox());
        if (distance > this.reach + 2) {
            return false;
        }
        return true;
    }

    public void swingArm() {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(this, 0);
        PacketUtils.sendPacketNearby(this, packetPlayOutAnimation);
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        Vec3D vec = this.getMot();
        this.setMot(0, this.getMot().y, 0);
        boolean damaged = super.damageEntity(damagesource, f);
        if (!damaged) {
            this.setMot(vec);
        }
        if (damaged && velocityChanged) {
            velocityChanged = false;
            Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    EntityPlayerBot.this.velocityChanged = true;
                }
            });
        }
        return damaged;
    }

    @Override
    public void die(DamageSource damagesource) {
        if (dead) {
            return;
        }
        super.die(damagesource);
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                ((WorldServer) world).removeEntity(EntityPlayerBot.this);
            }
        }, 15);
    }

    public void setMoveForward(float forward) {
        this.forward = forward;
    }

    public void setMoveStrafe(float strafe) {
        this.strafe = strafe;
    }

    public String getBotName() {
        return this.name;
    }

}
