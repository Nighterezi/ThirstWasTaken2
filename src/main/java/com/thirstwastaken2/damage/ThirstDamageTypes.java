package com.thirstwastaken2.damage;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

/** Restores the original mod's dedicated "died from dehydration" damage source. */
public final class ThirstDamageTypes {
    public static final ResourceKey<DamageType> DEHYDRATE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ThirstWasTaken2.id("dehydrate"));

    private ThirstDamageTypes() { }

    /**
     * Falls back to vanilla starvation if the datapack entry is missing, so a stripped resource pack
     * can never crash the dehydration tick.
     */
    public static DamageSource dehydration(ServerLevel level) {
        var damageSources = level.damageSources();
        return damageSources.source(
                damageSources.damageTypes.containsKey(DEHYDRATE) ? DEHYDRATE : DamageTypes.STARVE);
    }
}
