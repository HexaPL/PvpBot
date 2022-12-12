package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.BotAI;
import com.github.hexa.pvpbot.ai.BotAIBase;
import com.github.hexa.pvpbot.ai.ControllableBot;
import com.github.hexa.pvpbot.skins.Skin;
import com.github.hexa.pvpbot.util.RotationUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.net.URL;

public class EntityPlayerBot extends EntityPlayer implements ControllableBot {

    public String name;
    public EntityPlayer owner;
    private BotAI ai;
    private Skin skin;

    private float forward;
    private float strafe;

    private float prevYaw;
    private float prevPitch;

    private boolean hasPendingKnockback;

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
    public void tick() {
        //tickStartMS = SystemUtils.getTimeMillis();

        // Update rotation
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;

        // Tick AI
        if (this.ai.isEnabled()) {
            this.ai.tick();
        }

        // Update move speed/direction
        this.aT = this.forward;
        this.aR = this.strafe;

        // Tick entity
        super.tick();
        super.playerTick();

        // Send rotation packets again to make head movement smooth
        this.sendRotationPackets(owner);

        //tickEndMS = SystemUtils.getTimeMillis();
    }

    public void sendRotationPackets(EntityPlayer target) {
        int entityId = this.getId();
        byte yaw = RotationUtils.toByte(this.yaw);
        byte headYaw = RotationUtils.toByte(this.getHeadRotation());
        byte pitch = RotationUtils.toByte(this.pitch);
        PacketPlayOutEntity.PacketPlayOutEntityLook packetPlayOutEntityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, yaw, pitch, this.isOnGround());
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
            this.setMot(0, this.getMot().y, 0);
        }

        boolean damaged = super.damageEntity(damagesource, f);

        if (damaged && velocityChanged) {
            this.hasPendingKnockback = true;
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
        Bukkit.getScheduler().runTaskLater(PvpBotPlugin.getInstance(), () -> ((WorldServer) world).removeEntity(EntityPlayerBot.this), 15);

    }

    public void sendPacketNearby(Packet<?> packet) {
        ChunkProviderServer chunkproviderserver = ((WorldServer)this.world).getChunkProvider();
        chunkproviderserver.broadcast(this, packet);
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

    public void setRotation(float yaw, float pitch) {
        this.setHeadRotation(yaw);
        this.setYawPitch(yaw, pitch);
    }

    public void swingArm() {
        PacketPlayOutAnimation packetPlayOutAnimation = new PacketPlayOutAnimation(this, 0);
        this.sendPacketNearby(packetPlayOutAnimation);
    }

    @Override
    public void attack(LivingEntity entity) {
        EntityLiving nmsEntity = ((CraftLivingEntity) entity).getHandle();
        super.attack(nmsEntity);
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

    @Override
    public Player getOwner() {
        return this.owner.getBukkitEntity();
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = ((CraftPlayer) owner).getHandle();
    }

    @Override
    public Skin getSkin() {
        return this.skin;
    }

    @Override
    public void setSkin(Player player) {

    }

    @Override
    public void setSkin(Skin skin) {

    }

    @Override
    public void setSkin(URL mineSkinUrl) {

    }

    public boolean hasPendingKnockback() {
        return this.hasPendingKnockback;
    }

    public void clearPendingKnockback() {
        this.hasPendingKnockback = false;
    }

    private void init() {
        this.forward = 0F;
        this.strafe = 0F;
        this.prevYaw = 0F;
        this.prevPitch = 0F;
        this.hasPendingKnockback = false;
    }

    public enum BlockHitState {
        START, STOP
    }

}
