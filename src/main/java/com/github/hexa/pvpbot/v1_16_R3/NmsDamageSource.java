package com.github.hexa.pvpbot.v1_16_R3;

import com.github.hexa.pvpbot.util.IDamageSource;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.EntityDamageSource;
import org.bukkit.entity.Entity;

public class NmsDamageSource implements IDamageSource {

    private String source;
    private Entity entity;

    public NmsDamageSource(DamageSource damageSource) {
        source = damageSource.translationIndex;
        if (damageSource instanceof EntityDamageSource && damageSource.getEntity() != null) {
            entity = damageSource.getEntity().getBukkitEntity();
        }
    }

    @Override
    public String getSource() {
        return this.source;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }
}
