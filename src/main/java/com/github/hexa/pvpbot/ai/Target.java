package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.Bot;
import com.github.hexa.pvpbot.util.MathHelper;
import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Target {

    private Player player;
    private Bot bot;
    private HashMap<Integer, BoundingBox> locationCache;
    public int locationCacheSize;
    protected int delay;
    private BoundingBox delayedBoundingBox;
    private Location delayedHeadLocation;

    public Target(Player player, Bot bot) {
        this.player = player;
        this.bot = bot;
        this.locationCacheSize = 0;
        this.flushLocationCache();
        this.delay = 0;
        this.delayedBoundingBox = BoundingBoxUtils.getBoundingBox(player);
        this.delayedHeadLocation = this.player.getEyeLocation();
    }

    public void update() {
        this.updateDelays();
        this.updateLocationCache();
        this.delayedBoundingBox = calculateDelayedBoundingBox();
        this.delayedHeadLocation = calculateDelayedHeadLocation();
    }

    private void updateDelays() {
        int delay = 150 + (bot.getControllable().getAI().getPing() / 2);
        if (delay != this.delay) {
            this.delay = delay;
            this.locationCacheSize = MathHelper.ceil(this.delay / 50F) + 1;
            this.flushLocationCache();
        }
    }

    private void updateLocationCache() {
        if (this.locationCacheSize == 1) {
            return;
        }
        for (int i = this.locationCacheSize - 1; i > 0; i--) {
            this.locationCache.put(i, this.locationCache.get(i - 1));
        }
        this.locationCache.put(0, player.getBoundingBox());
    }

    private BoundingBox calculateDelayedBoundingBox() {

        if (this.delay == 0) {
            return player.getBoundingBox();
        }

        if (this.delay % 50 == 0) {
            return this.locationCache.get(delay / 50);
        }

        int initialTick = MathHelper.floor(this.delay / 50F);
        float partialTicks = (this.delay % 50) / 50F;

        BoundingBox box1 = this.locationCache.get(initialTick);
        BoundingBox box2 = this.locationCache.get(initialTick + 1);

        return BoundingBoxUtils.interpolate(box1, box2, partialTicks);

    }

    public void flushLocationCache() {
        if (this.locationCache == null) {
            this.locationCache = new HashMap<>();
        }
        this.locationCache.clear();
        if (this.locationCacheSize <= 1) {
            return;
        }
        for (int i = 0; i < this.locationCacheSize; i++) {
            this.locationCache.put(i, player.getBoundingBox());
        }
    }

    public BoundingBox getDelayedBoundingBox() {
        return this.delayedBoundingBox;
    }

    private Location calculateDelayedHeadLocation() {
        if (this.delay == 0) {
            return this.player.getEyeLocation();
        }
        Vector center = this.delayedBoundingBox.getCenter();
        return center.clone().setY(this.delayedBoundingBox.getMinY() + this.player.getEyeHeight()).toLocation(this.player.getWorld());
    }

    public Location getDelayedHeadLocation() {
        return this.delayedHeadLocation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<Integer, BoundingBox> getLocationCache() {
        return new HashMap<>(this.locationCache);
    }

    public boolean isInvulnerable() {
        return player.getNoDamageTicks() > player.getMaximumNoDamageTicks() / 2;
    }

}
