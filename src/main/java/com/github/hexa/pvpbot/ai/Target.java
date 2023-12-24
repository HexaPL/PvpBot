package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.VectorUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Target {

    private Player player;
    private Bot bot;
    private HashMap<Integer, BoundingBox> pingCache;
    public int pingCacheSize;
    protected int delay;

    private BoundingBox boundingBox;
    private Location headLocation;
    private HashMap<Integer, Location> locationCache;

    public Vector motion;
    public double blockSpeed;
    public Vector motionVectorTowardsBot;
    public double motionTowardsBot;

    public Target(Player player, Bot bot) {
        this.player = player;
        this.bot = bot;
        this.pingCacheSize = 0;
        this.flushPingCache();
        this.delay = 0;
        this.motion = new Vector(0, 0, 0);
        this.blockSpeed = 0;
        this.boundingBox = BoundingBoxUtils.getBoundingBox(player);
        this.headLocation = this.player.getEyeLocation();
        this.initLocationCache();
    }

    public void update() {
        this.updateDelays();
        this.updatePingCache();
        this.boundingBox = this.calculateDelayedBoundingBox();
        this.headLocation = this.calculateDelayedHeadLocation();
        this.updateLocationCache();
        this.updateMotion();
    }

    private void updateDelays() {
        int delay = 150 + (bot.getControllable().getAI().getPing() / 2);
        if (delay != this.delay) {
            this.delay = delay;
            this.pingCacheSize = MathHelper.ceil(this.delay / 50F) + 1;
            this.flushPingCache();
        }
    }

    private void updatePingCache() {
        if (this.pingCacheSize == 1) {
            return;
        }
        for (int i = this.pingCacheSize - 1; i > 0; i--) {
            this.pingCache.put(i, this.pingCache.get(i - 1));
        }
        this.pingCache.put(0, player.getBoundingBox());
    }

    private void updateLocationCache() {
        for (int i = 40; i > 0; i--) {
            locationCache.put(i, this.locationCache.get(i - 1));
        }
        locationCache.put(0, this.getHeadLocation());
    }

    private void updateMotion() {
        Location currentLocation = locationCache.get(0);
        Location previousLocation = locationCache.get(1);
        Vector delta = previousLocation.clone().subtract(currentLocation).toVector();
        this.blockSpeed = delta.length();
        this.motion = VectorUtils.blockSpeedToMotion(delta);
        Location botLocation = bot.getControllable().getEyeLocation();
        double distanceDelta = botLocation.distance(previousLocation) - botLocation.distance(currentLocation);
        this.motionVectorTowardsBot = VectorUtils.blockSpeedToMotion(VectorUtils.getVectorFromTo(previousLocation, botLocation).normalize().multiply(distanceDelta));
        this.motionTowardsBot = this.motionVectorTowardsBot.length() * Math.signum(distanceDelta);
    }

    private BoundingBox calculateDelayedBoundingBox() {

        if (this.delay == 0) {
            return player.getBoundingBox();
        }

        if (this.delay % 50 == 0) {
            return this.pingCache.get(delay / 50);
        }

        int initialTick = MathHelper.floor(this.delay / 50F);
        float partialTicks = (this.delay % 50) / 50F;

        BoundingBox box1 = this.pingCache.get(initialTick);
        BoundingBox box2 = this.pingCache.get(initialTick + 1);

        return BoundingBoxUtils.interpolate(box1, box2, partialTicks);

    }

    private Location calculateDelayedHeadLocation() {
        if (this.delay == 0) {
            return this.player.getEyeLocation();
        }
        Vector center = this.boundingBox.getCenter();
        return center.clone().setY(this.boundingBox.getMinY() + this.player.getEyeHeight()).toLocation(this.player.getWorld());
    }

    public void flushPingCache() {
        if (this.pingCache == null) {
            this.pingCache = new HashMap<>();
        }
        this.pingCache.clear();
        if (this.pingCacheSize <= 1) {
            return;
        }
        for (int i = 0; i < this.pingCacheSize; i++) {
            this.pingCache.put(i, player.getBoundingBox());
        }
    }

    private void initLocationCache() {
        this.locationCache = new HashMap<>();
        for (int i = 0; i <= 40; i++) {
            locationCache.put(i, this.getHeadLocation());
        }
    }

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public Location getHeadLocation() {
        return this.headLocation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<Integer, BoundingBox> getPingCache() {
        return new HashMap<>(this.pingCache);
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
