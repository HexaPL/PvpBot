package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.ai.gamemode.GameMode;
import com.github.hexa.pvpbot.PvpBotPlugin;
import com.github.hexa.pvpbot.ai.Ai;
import com.github.hexa.pvpbot.ControllableBot;
import com.github.hexa.pvpbot.ai.gamemode.sword.SwordAi;
import com.github.hexa.pvpbot.skins.Skin;
import com.github.hexa.pvpbot.util.*;
import com.github.hexa.pvpbot.util.MathHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityPlayerBot extends EntityPlayer implements ControllableBot {

    public String name;
    private GameMode gameMode;
    public EntityPlayer owner;
    private Ai ai;
    private Skin skin;
    private PropertyMap properties;

    private float forward;
    private float strafe;

    private float prevYaw;
    private float prevPitch;

    public boolean hasPendingKnockback;
    public List<Vec3D> pendingKnockbacks = new ArrayList<>();

    public static HashMap<Player, Location> packetLocations = new HashMap<>();

    public EntityPlayerBot(String name, EntityPlayer owner, MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.name = name;
        this.owner = owner;
        this.properties = new PropertyMap(this);
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
        if (this.ai != null && this.ai.isEnabled()) {
            this.ai.tick();
        }

        // Update move speed/direction
        this.aT = this.forward;
        this.aR = this.strafe;

        // Tick entity
        super.tick();
        super.playerTick();

        if (PvpBotPlugin.debug) pingDelayDebug();
        //tickEndMS = SystemUtils.getTimeMillis();
    }

    private void pingDelayDebug() {
        long time = System.currentTimeMillis();
        if (MathHelper.roundToNumber(time, 50) % 500 != 0) {
            return;
        }
        if (owner.getBukkitEntity().getInventory().getItemInOffHand().getType() != Material.GOLD_NUGGET) {
            return;
        }
        Vector mot = this.getMotion();
        mot.setY(0);
        String velocity = "" + MathHelper.roundTo((float) mot.length(), 4);
        float reach = MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.getBukkitEntity().getEyeLocation(), BoundingBoxUtils.getBoundingBox(owner.getBukkitEntity())), 3);
        String timeS = String.valueOf(time);
        timeS = timeS.substring(timeS.length() - 5);
        if (this.getAI().getTarget() == null) {
            ((SwordAi)this.getAI()).selectTarget();
        }
        float reach2 = MathHelper.roundTo((float) BoundingBoxUtils.distanceTo(this.getBukkitEntity().getEyeLocation(), this.getAI().getTarget().getBoundingBox()), 3);
        Bukkit.broadcastMessage("SERVER " + velocity + ", reach " + reach + ", pingReach " + reach2 + ", ms " + timeS);
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
            this.setMot(0, this.getMotion().getY(), 0);
        }

        boolean damaged = super.damageEntity(damagesource, f);

        if (damaged && velocityChanged) {
            this.velocityChanged = false;
            //Bukkit.getScheduler().runTask(PvpBotPlugin.getInstance(), () -> velocityChanged = true);
            //this.hasPendingKnockback = true;
            this.getAI().onDamageEntity(new NmsDamageSource(damagesource));
        }

        return damaged;

    }

    public Vec3D tempKnockback;

    @Override
    public void setMot(Vec3D vec3d) {
        /*
        if (this.hasPendingKnockback) {
            this.hasPendingKnockback = false;
            super.setMot(tempKnockback);
            return;
        }*/
        super.setMot(vec3d);
    }

    public Vector getMotion() {
        Vec3D mot = this.getMot();
        return new Vector(mot.x, mot.y, mot.z);
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

    @Override
    public void setAI(Ai ai) {
        this.ai = ai;
    }

    @Override
    public Ai getAI() {
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
    public boolean canJump() {
        return this.onGround;
    }

    @Override
    public void jump() {
        if (this.canJump()) {
            super.jump();
        }
    }

    @Override
    public boolean canCrit() {
        return /*!this.isSprinting() && */!this.onGround && this.getMotion().getY() < 0;
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public GameMode getGameMode() {
        return this.gameMode;
    }

    @Override
    public void setFallDistance(float f) {
        this.fallDistance = f;
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
        // Toggle sprint
        this.setSprinting(forward == 1.0F);
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
    public float getAttackCooldown() {
        return super.getAttackCooldown(0.5F);
    }

    public void setAttackCooldown(int attackCooldown) {
        this.at = attackCooldown;
    }

    @Override
    public String getBotName() {
        return this.name;
    }

    @Override
    public PropertyMap getProperties() {
        return this.properties;
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

}
