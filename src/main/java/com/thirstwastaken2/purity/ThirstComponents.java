package com.thirstwastaken2.purity;

import com.mojang.serialization.Codec;
import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ThirstComponents {
    public static final DataComponentType<Integer> WATER_SERVINGS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_servings"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, 3))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final DataComponentType<Integer> WATER_PURITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_purity"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(WaterPurity.MIN, WaterPurity.MAX))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    /** Exact sampled contamination. Purity remains the compact, player-facing four-tier value. */
    public static final DataComponentType<Integer> WATER_CONTAMINATION = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_contamination"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(WaterQuality.MIN_CONTAMINATION, WaterQuality.MAX_CONTAMINATION))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    /** Salinity is independent of cleanliness: boiling unsafe fresh water must not desalinate it. */
    public static final DataComponentType<Boolean> WATER_SALTY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_salty"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build());

    private ThirstComponents() { }
    public static void register() { }
}
