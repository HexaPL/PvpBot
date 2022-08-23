package com.github.hexa.pvpbot.ai;

import com.github.hexa.pvpbot.util.BoundingBoxUtils;
import com.github.hexa.pvpbot.util.MathHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class Target {

    private Player player;
    private HashMap<Integer, BoundingBox> locationCache;
    public int locationCacheSize;
    protected int delay;
    private BoundingBox delayedBoundingBox;
    private Location delayedHeadLocation;

    public Target(Player player) {
        this.player = player;
        this.locationCacheSize = 0;
        this.flushLocationCache();
        this.delay = 0;
        this.delayedBoundingBox = this.player.getBoundingBox();
        this.delayedHeadLocation = this.player.getEyeLocation();
    }

    public void update() {
        this.delayedBoundingBox = calculateDelayedBoundingBox();
        this.delayedHeadLocation = calculateDelayedHeadLocation();
    }

    private BoundingBox calculateDelayedBoundingBox() {

        if (this.delay  == 0) {
            return this.player.getBoundingBox();
        }

        if (this.delay % 50 == 0) {
            return this.locationCache.get(delay / 50);
        }

        int initialTick = MathHelper.floor((float) this.delay / 50);
        float partialTicks = (this.delay % 50) / 50F;
        BoundingBox box1;
        BoundingBox box2;

        if (initialTick == 0) {
            box1 = this.player.getBoundingBox();
            box2 = this.locationCache.get(1);
        } else {
            box1 = this.locationCache.get(initialTick);
            box2 = this.locationCache.get(initialTick + 1);
        }

        return BoundingBoxUtils.interpolate(box1, box2, partialTicks);

    }

    public void flushLocationCache() {
        if (this.locationCache == null) {
            this.locationCache = new HashMap<>();
        } else {
            this.locationCache.clear();
        }
        if (this.locationCacheSize == 0) {
            return;
        }
        for (int i = 1; i <= this.locationCacheSize; i++) {
            this.locationCache.put(i, this.player.getBoundingBox());
        }
    }

    public void updateLocationCache() {
        if (this.locationCacheSize == 0) {
            return;
        }
        for (int i = this.locationCacheSize; i > 1; i--) {
            this.locationCache.put(i, this.locationCache.get(i - 1));
        }
        this.locationCache.put(1, this.player.getBoundingBox());
    }

    public BoundingBox getDelayedBoundingBox() {
        return this.delayedBoundingBox;
    }

    private Location calculateDelayedHeadLocation() {
        if (this.delay == 0) {
            return this.player.getEyeLocation();
        }
        Vector center = this.delayedBoundingBox.getCenter();
        Vector head = center.clone().setY(this.delayedBoundingBox.getMinY() + this.player.getEyeHeight());
        return head.toLocation(this.player.getWorld());
    }

    public Location getDelayedHeadLocation() {
        return this.delayedHeadLocation;
    }

    public Player getPlayer() {
        return this.player;
    }

    public HashMap<Integer, BoundingBox> getLocationCache() {
        return this.locationCache;
    }

}
