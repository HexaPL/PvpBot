package com.github.hexa.pvpbot.v1_8_R3;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.RotationUtils;
import com.github.hexa.pvpbot.util.VectorUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import static com.github.hexa.pvpbot.v1_8_R3.EntityPlayerBot.SprintResetMethod.*;

public class EntityPlayerBot extends EntityPlayer implements Bot {

    public String name;
    public EntityPlayer owner;
    public EntityPlayer target;
    public float forward;
    public float strafe;
    public float reach;
    private float prevYaw;
    private float prevPitch;
    public boolean shouldAttack;
    public int clicksPerSecond;
    public long tickMsTimer;
    public int clickDelay;
    public boolean canSprint;
    public boolean freshSprint;
    public boolean isSprintResetting;
    private boolean sTapSlowdown;
    public int sprintResetDelay = 3;
    public int sprintResetLength = 4;
    public int sprintTicks;
    public SprintResetMethod sprintResetMethod;

    // Walk direction constants
    public static final int FORWARD = 1;
    public static final int BACKWARD = -1;
    public static final int RIGHT = 1;
    public static final int LEFT = 1;

    public EntityPlayerBot(String name, EntityPlayer owner, MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.name = name;
        this.owner = owner;
        // Initialize variables
        this.init();
    }

    @Override
    public void t_() {
        //tickStartMS = SystemUtils.getTimeMillis();

        // Update rotation
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        if (this.target != null) {
            this.rotateToTarget(this.target);
        }

        // Update move speed/direction
        this.aT = this.forward;
        this.aR = this.strafe;
        this.setSprinting(this.canSprint && !this.isSprintResetting);

        // Attack current target if possible
        this.doHitLogic();

        // Do sprint reset if needed
        this.handleSprintResetting();

        // Tick entity
        super.t_(); // tick()
        super.l(); // playerTick

        // Send rotation packets again to make head movement smooth
        this.sendRotationPackets(owner);

        //tickEndMS = SystemUtils.getTimeMillis();
    }

    public void sendRotationPackets(EntityPlayer target) {
        int entityId = this.getId();
        byte yaw = RotationUtils.toByte(this.yaw);
        byte headYaw = RotationUtils.toByte(this.getHeadRotation());
        byte pitch = RotationUtils.toByte(this.pitch);
        PacketPlayOutEntity.PacketPlayOutEntityLook packetPlayOutEntityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, yaw, pitch, this.onGround);
        PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotation(this, headYaw);
        owner.playerConnection.sendPacket(packetPlayOutEntityLook);
        owner.playerConnection.sendPacket(packetPlayOutEntityHeadRotation);
    }

    private void doHitLogic() {

        // Check for target and CPS
        if (this.target == null || !this.shouldAttack || this.clicksPerSecond == 0) {
            this.tickMsTimer = 0;
            return;
        }

        // Return if on click cooldown
        if (this.tickMsTimer < this.clickDelay) {
            this.tickMsTimer += 50;
            return;
        }

        // Calculate distance to closest point of target's hitbox
        Location eyeLocation = this.getBukkitEntity().getEyeLocation();
        BoundingBox targetBoundingBox = target.getBukkitEntity().getBoundingBox();
        double distance = BoundingBoxUtils.distanceTo(eyeLocation, targetBoundingBox);

        // Check if target is close enough to swing or attack
        if (distance > this.reach + 2) {
            return;
        }

        // Perform raytrace to target's hitbox
        RayTraceResult result = targetBoundingBox.rayTrace(eyeLocation.toVector(), eyeLocation.getDirection(), this.reach);

        // Swing hand and/or attack target, based on current click rate
        while (this.tickMsTimer >= this.clickDelay) {
            this.tickMsTimer -= this.clickDelay;
            this.swingArm();
            if (result != null) {
                this.attack(target);
            }
        }

    }

    public void swingArm() {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(this, 0);
        this.sendPacketNearby(packetPlayOutAnimation);
    }

    public void handleSprintResetting() {

        // Check if any action is required
        if (!this.canSprint || this.sprintTicks == -1) {
            return;
        }

        // Reset s-tap slowdown to not move backwards
        if (this.sTapSlowdown) {
            this.sTapSlowdown = false;
            this.setMoveForward(FORWARD);
        }

        // Bukkit.broadcastMessage("handleSprintResetting - sprintTicks" + )

        // Start sprint reset if needed
        if (this.isSprinting() && this.forward > 0 && !this.freshSprint && !this.isSprintResetting && this.sprintTicks >= this.sprintResetDelay) {
            this.setSprinting(false);
            this.isSprintResetting = true;
            this.startSprintReset(this.sprintResetMethod);
            this.sprintTicks = 0;
        }

        // End sprint reset if needed
        if (isSprintResetting && sprintTicks >= sprintResetLength) {
            this.setSprinting(true);
            this.freshSprint = true;
            this.isSprintResetting = false;
            this.endSprintReset(this.sprintResetMethod);
            this.sprintTicks = -1;
        }

        if (this.sprintTicks != -1) this.sprintTicks++;

    }

    public void startSprintReset(SprintResetMethod method) {
        switch (method) {
            case WTAP:
                // Simply simulate releasing W key
                this.setMoveForward(0);
                break;
            case STAP:
                // Do some opposite force to slow down faster
                this.setMoveForward(-0.5F);
                this.sTapSlowdown = true;
                break;
            case BLOCKHIT:
                // Block sword
                // TODO - blockhit
                break;
        }
    }

    public void endSprintReset(SprintResetMethod method) {
        switch (method) {
            case WTAP:
                this.setMoveForward(FORWARD);
        }
    }

    public void sendBlockHitAnimation(BlockHitState state) {

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
        this.f(yaw); // setHeadRotation
        this.setYawPitch(yaw, pitch);
    }

    @Override
    public void attack(Entity entity) {

        // Filter non-living entities
        if (!(entity instanceof EntityLiving)) {
            return;
        }

        // Cache initial sprint state to restore it later
        boolean wasSprinting = this.isSprinting();

        // Damage & invulnerability predictions
        boolean canDamage = ((float)this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue()) + (EnchantmentManager.a(this.bA(), EnumMonsterType.UNDEFINED)) > 0.0F;
        boolean invulnerable = (float) entity.noDamageTicks > (float) ((EntityLiving)entity).maxNoDamageTicks / 2.0F;
        boolean knockback = canDamage && !invulnerable;

        // Correct sprint state if knockback will be applied to target
        if (knockback && isSprinting() && !freshSprint) {
            this.setSprinting(false);
        }
        super.attack(entity);

        // Simulate client-server desync and make bot sprint-reset soon
        if (knockback && this.freshSprint) {
            this.freshSprint = false;
            this.sprintTicks = 0;
        }

        // Restore sprint state to not affect later movement
        this.setSprinting(wasSprinting);

    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {

        // Check for invulnerability ticks
        boolean invulnerable = (float) this.noDamageTicks > (float) this.maxNoDamageTicks / 2.0F;

        // Set motion to 0 to simulate client-side knockback calculation
        if (!invulnerable && f > 0.0F) {
            this.setMot(0, this.getMot().b, 0);
        }

        boolean damaged = super.damageEntity(damagesource, f);

        // Make server apply velocity instead of sending packet to non-existing client
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

        // Check if bot is already dead
        if (dead) {
            return;
        }

        super.die(damagesource);

        // Wait for death animation to end and remove bot from world
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                ((WorldServer) world).removeEntity(EntityPlayerBot.this);
            }
        }, 15);

    }

    public void setMot(double x, double y, double z) {
        this.motX = x;
        this.motY = y;
        this.motZ = z;
    }

    public Vec3D getMot() {
        return new Vec3D(this.motX, this.motY, this.motZ);
    }

    public void sendPacketNearby(Packet<?> packet) {
        ((WorldServer) this.world).tracker.a(this, packet);
    }

    @Override
    public Player getTarget() {
        return target.getBukkitEntity();
    }

    @Override
    public void setTarget(Player target) {
        this.target = ((CraftPlayer) target).getHandle();
        this.shouldAttack = true;
    }

    @Override
    public void clearTarget() {
        this.shouldAttack = false;
        this.target = null;
    }

    @Override
    public void setCPS(int cps) {
        this.clicksPerSecond = cps;
        this.clickDelay = cps == 0 ? 0 : 1000 / cps;
    }

    @Override
    public int getCPS() {
        return this.clicksPerSecond;
    }

    @Override
    public void setMoveForward(float forward) {
        this.forward = forward;
    }

    @Override
    public void setMoveStrafe(float strafe) {
        this.strafe = strafe;
    }

    @Override
    public void canSprint(boolean canSprint) {
        this.canSprint = canSprint;
    }

    @Override
    public boolean canSprint() {
        return this.canSprint;
    }

    @Override
    public String getBotName() {
        return this.name;
    }

    public void init() {
        this.reach = 3.0F;
        this.forward = 0F;
        this.strafe = 0F;
        this.clicksPerSecond = 0;
        this.tickMsTimer = 0;
        this.clickDelay = 0;
        this.shouldAttack = false;
        this.canSprint = false;
        this.freshSprint = true;
        this.isSprintResetting = false;
        this.sprintTicks = -1;
        this.sprintResetMethod = WTAP;
        this.prevYaw = 0F;
        this.prevPitch = 0F;
    }

    public enum SprintResetMethod {
        WTAP, STAP, BLOCKHIT
    }

    public enum BlockHitState {
        START, STOP
    }

}
