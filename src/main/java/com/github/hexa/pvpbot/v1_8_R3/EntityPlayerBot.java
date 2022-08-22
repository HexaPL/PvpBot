package com.github.hexa.pvpbot.v1_8_R3;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.AttackResult;
import com.github.hexa.pvpbot.ai.BotAI;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.ControllableBot;
import com.github.hexa.pvpbot.util.RotationUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class EntityPlayerBot extends EntityPlayer implements ControllableBot {

    public String name;
    public EntityPlayer owner;
    private EntityPlayer target;
    private BotAI ai;

    private float forward;
    private float strafe;

    private float prevYaw;
    private float prevPitch;

    private boolean canSprint;

    public EntityPlayerBot(String name, EntityPlayer owner, MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.name = name;
        this.owner = owner;
        // Initialize AI
        this.ai = new BotAIBase(this);
        // Initialize variables
        this.init();
    }

    @Override
    public void t_() {
        //tickStartMS = SystemUtils.getTimeMillis();

        // Cache last rotation
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;

        // Tick AI
        this.ai.tick();

        // Update move speed/direction
        this.ba = this.forward;
        this.aZ = this.strafe;

        // Tick entity
        super.t_(); // tick()
        super.l(); // playerTick()

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
            Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> EntityPlayerBot.this.velocityChanged = true);
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
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> world.removeEntity(EntityPlayerBot.this), 15);

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

    public void sendBlockHitAnimation(BlockHitState state) {}

    @Override
    public void setAI(BotAI ai) {
        this.ai = ai;
    }

    @Override
    public BotAI getAI() {
        return this.ai;
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        this.f(yaw); // setHeadRotation
        this.setYawPitch(yaw, pitch);
    }

    @Override
    public void swingArm() {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(this, 0);
        this.sendPacketNearby(packetPlayOutAnimation);
    }

    @Override
    public AttackResult attack(org.bukkit.entity.LivingEntity entity, boolean freshSprint) {

        EntityLiving nmsEntity = ((CraftLivingEntity) entity).getHandle();

        AttackResult result = AttackResult.INVULNERABLE;

        // Cache initial sprint state to restore it later
        boolean wasSprinting = this.isSprinting();

        // Damage & invulnerability predictions
        boolean canDamage = ((float)this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue()) + (EnchantmentManager.a(this.bA(), EnumMonsterType.UNDEFINED)) > 0.0F;
        boolean invulnerable = (float) nmsEntity.noDamageTicks > (float) nmsEntity.maxNoDamageTicks / 2.0F;
        boolean knockback = canDamage && !invulnerable;

        // Correct sprint state if knockback will be applied to target
        if (knockback && isSprinting() && !freshSprint) {
            this.setSprinting(false);
        }

        // Attack entity
        super.attack(nmsEntity);

        if (knockback) {
            result = AttackResult.KNOCKBACK;
        }

        // Restore sprint state to not affect later movement
        this.setSprinting(wasSprinting);

        return result;
    }

    @Override
    public Location getEyeLocation() {
        return this.getBukkitEntity().getEyeLocation();
    }

    @Override
    public BoundingBox getBukkitBoundingBox() {
        return this.getBukkitEntity().getBoundingBox();
    }

    @Override
    public Player getTarget() {
        return target.getBukkitEntity();
    }

    @Override
    public void setTarget(Player target) {
        this.target = ((CraftPlayer) target).getHandle();
    }

    @Override
    public void clearTarget() {
        this.target = null;
    }

    @Override
    public void setMoveForward(float forward) {
        this.forward = forward;
    }

    @Override
    public float getMoveForward() {
        return this.forward;
    }

    @Override
    public void setMoveStrafe(float strafe) {
        this.strafe = strafe;
    }

    @Override
    public float getMoveStrafe() {
        return this.strafe;
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
    public boolean isSprinting() {
        return super.isSprinting();
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
    }

    @Override
    public String getBotName() {
        return this.name;
    }

    private void init() {
        this.forward = 0F;
        this.strafe = 0F;
        this.canSprint = false;
        this.prevYaw = 0F;
        this.prevPitch = 0F;
    }

    public enum BlockHitState {
        START, STOP
    }

}
