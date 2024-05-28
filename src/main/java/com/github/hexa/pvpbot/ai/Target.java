package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Target {

    private Player player;
    private Bot bot;
    private HashMap<Integer, BoundingBox> hitboxPingCache;
    private HashMap<Integer, Vector> aimPingCache;
    public int pingCacheSize;
    protected int delay;

    private BoundingBox boundingBox;
    private Location headLocation;
    private HashMap<Integer, Location> locationCache;
    private HashMap<Integer, Vector> lookDirectionCache;

    public Vector motion;
    public Vector lookDirection;
    public double blockSpeed;
    public Vector motionVectorTowardsBot;
    public double motionTowardsBot;
    public double blockSpeedTowardsBot;
    public int strafeDirection;

    public Vector realMot = new Vector();
    public Vector realLastLoc = new Vector();

    public Target(Player player, Bot bot) {
        this.player = player;
        this.bot = bot;
        this.pingCacheSize = 0;
        this.flushPingCache();
        this.delay = 0;
        this.motion = new Vector(0, 0, 0);
        this.lookDirection = new Vector(0, 0, 0);
        this.blockSpeed = 0;
        this.boundingBox = BoundingBoxUtils.getBoundingBox(player);
        this.headLocation = this.player.getEyeLocation();
        this.initCaches();
    }

    public void update() {
        this.updateDelays();
        this.updatePing();
        this.boundingBox = this.calculateDelayedBoundingBox();
        this.headLocation = this.calculateDelayedHeadLocation();
        this.lookDirection = this.calculateDelayedLookDirection();
        this.updateMotion();
        this.updateCache();
    }

    private void updateDelays() {
        int delay = 150 + (bot.getControllable().getAI().getPing() / 2);
        if (delay != this.delay) {
            this.delay = delay;
            this.pingCacheSize = MathHelper.ceil(this.delay / 50F) + 1;
            this.flushPingCache();
        }
    }

    private void updatePing() {
        if (this.pingCacheSize == 1) {
            return;
        }
        for (int i = this.pingCacheSize - 1; i > 0; i--) {
            this.hitboxPingCache.put(i, this.hitboxPingCache.get(i - 1));
            this.aimPingCache.put(i, this.aimPingCache.get(i - 1));
        }
        this.hitboxPingCache.put(0, player.getBoundingBox());
        this.aimPingCache.put(0, player.getEyeLocation().getDirection());
    }

    private void updateCache() {
        for (int i = 40; i > 0; i--) {
            locationCache.put(i, locationCache.get(i - 1));
            lookDirectionCache.put(i, lookDirectionCache.get(i - 1));
        }
        locationCache.put(0, this.getHeadLocation());
        lookDirectionCache.put(0, this.getLookDirection());
    }

    private void updateMotion() {
        Location currentLocation = locationCache.get(0);
        Location previousLocation = locationCache.get(1);
        Vector delta = currentLocation.clone().subtract(previousLocation).toVector();
        this.blockSpeed = delta.length();
        this.motion = VectorUtils.blockSpeedToMotion(delta);
        Location botLocation = bot.getControllable().getEyeLocation();
        double distanceDelta = botLocation.distance(previousLocation) - botLocation.distance(currentLocation);
        this.motionVectorTowardsBot = VectorUtils.blockSpeedToMotion(VectorUtils.getVectorFromTo(previousLocation, botLocation).normalize().multiply(distanceDelta));
        this.motionTowardsBot = this.motionVectorTowardsBot.length() * Math.signum(distanceDelta);
        this.blockSpeedTowardsBot = VectorUtils.motionToBlockSpeed(motionVectorTowardsBot).length() * Math.signum(distanceDelta);

        double cross = motion.clone().setY(0).normalize().crossProduct(lookDirection.clone().setY(0).normalize()).getY();
        this.strafeDirection = (int) (Math.abs(cross) < 0.3 ? 0 : 1 * Math.signum(-cross));

        realMot = getPlayer().getLocation().toVector().subtract(realLastLoc);
        realLastLoc = getPlayer().getLocation().toVector();

    }

    private BoundingBox calculateDelayedBoundingBox() {

        if (this.delay == 0) {
            return player.getBoundingBox();
        }

        if (this.delay % 50 == 0) {
            return this.hitboxPingCache.get(delay / 50);
        }

        int initialTick = MathHelper.floor(this.delay / 50F);
        float partialTicks = (this.delay % 50) / 50F;

        BoundingBox box1 = this.hitboxPingCache.get(initialTick);
        BoundingBox box2 = this.hitboxPingCache.get(initialTick + 1);

        return BoundingBoxUtils.interpolate(box1, box2, partialTicks);

    }

    private Location calculateDelayedHeadLocation() {
        if (this.delay == 0) {
            return this.player.getEyeLocation();
        }
        Vector center = this.boundingBox.getCenter();
        return center.clone().setY(this.boundingBox.getMinY() + this.player.getEyeHeight()).toLocation(this.player.getWorld());
    }

    private Vector calculateDelayedLookDirection() {
        if (this.delay == 0) {
            return player.getEyeLocation().getDirection();
        }
        if (this.delay % 50 == 0) {
            return this.aimPingCache.get(delay / 50);
        }

        int initialTick = MathHelper.floor(this.delay / 50F);
        float partialTicks = (this.delay % 50) / 50F;

        Vector vector1 = this.aimPingCache.get(initialTick);
        Vector vector2 = this.aimPingCache.get(initialTick + 1);

        return VectorUtils.interpolate(vector1, vector2, partialTicks);
    }

    public void flushPingCache() {
        if (this.hitboxPingCache == null) {
            this.hitboxPingCache = new HashMap<>();
        }
        if (this.aimPingCache == null) {
            this.aimPingCache = new HashMap<>();
        }
        this.hitboxPingCache.clear();
        this.aimPingCache.clear();
        if (this.pingCacheSize <= 1) {
            return;
        }
        for (int i = 0; i < this.pingCacheSize; i++) {
            this.hitboxPingCache.put(i, player.getBoundingBox());
            this.aimPingCache.put(i, player.getEyeLocation().getDirection());
        }
    }

    private void initCaches() {
        this.locationCache = new HashMap<>();
        this.lookDirectionCache = new HashMap<>();
        for (int i = 0; i <= 40; i++) {
            locationCache.put(i, this.getHeadLocation());
            lookDirectionCache.put(i, this.getLookDirection());
        }
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public Location getHeadLocation() {
        return this.headLocation;
    }

    public Vector getLookDirection() {
        return this.lookDirection;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<Integer, BoundingBox> getHitboxPingCache() {
        return new HashMap<>(this.hitboxPingCache);
    }

    public boolean isInvulnerable() {
        return player.getNoDamageTicks() > player.getMaximumNoDamageTicks() / 2;
    }

    public Vector getMotion() {
        return this.motion;
    }

    public double getBlockSpeed() {
        return this.blockSpeed;
    }

}
