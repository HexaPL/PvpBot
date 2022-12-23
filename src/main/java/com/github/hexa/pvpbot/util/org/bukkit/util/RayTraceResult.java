package com.github.hexa.pvpbot.util.org.bukkit.util;

import org.apache.commons.lang.Validate;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RayTraceResult {
    private final Vector hitPosition;
    private final Block hitBlock;
    private final BlockFace hitBlockFace;
    private final Entity hitEntity;

    private RayTraceResult(@NotNull Vector hitPosition, @Nullable Block hitBlock, @Nullable BlockFace hitBlockFace, @Nullable Entity hitEntity) {
        Validate.notNull(hitPosition, "Hit position is null!");
        this.hitPosition = hitPosition.clone();
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
        this.hitEntity = hitEntity;
    }

    public RayTraceResult(@NotNull Vector hitPosition) {
        this(hitPosition, (Block)null, (BlockFace)null, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, (Block)null, hitBlockFace, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Block hitBlock, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, hitBlock, hitBlockFace, (Entity)null);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Entity hitEntity) {
        this(hitPosition, (Block)null, (BlockFace)null, hitEntity);
    }

    public RayTraceResult(@NotNull Vector hitPosition, @Nullable Entity hitEntity, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, (Block)null, hitBlockFace, hitEntity);
    }

    @NotNull
    public Vector getHitPosition() {
        return this.hitPosition.clone();
    }

    @Nullable
    public Block getHitBlock() {
        return this.hitBlock;
    }

    @Nullable
    public BlockFace getHitBlockFace() {
        return this.hitBlockFace;
    }

    @Nullable
    public Entity getHitEntity() {
        return this.hitEntity;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof RayTraceResult)) {
            return false;
        } else {
            RayTraceResult other = (RayTraceResult)obj;
            if (!this.hitPosition.equals(other.hitPosition)) {
                return false;
            } else if (!Objects.equals(this.hitBlock, other.hitBlock)) {
                return false;
            } else if (!Objects.equals(this.hitBlockFace, other.hitBlockFace)) {
                return false;
            } else {
                return Objects.equals(this.hitEntity, other.hitEntity);
            }
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RayTraceResult [hitPosition=");
        builder.append(this.hitPosition);
        builder.append(", hitBlock=");
        builder.append(this.hitBlock);
        builder.append(", hitBlockFace=");
        builder.append(this.hitBlockFace);
        builder.append(", hitEntity=");
        builder.append(this.hitEntity);
        builder.append("]");
        return builder.toString();
    }
}
