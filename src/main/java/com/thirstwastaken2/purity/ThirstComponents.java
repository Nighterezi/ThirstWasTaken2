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

    /** The grade of the fresh water inside. Salt water carries {@link #WATER_SALTY} instead. */
    public static final DataComponentType<Integer> WATER_PURITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_purity"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(WaterPurity.MIN, WaterPurity.MAX))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    /**
     * Sea water. It is not a grade: a salty container carries no {@link #WATER_PURITY} at all, so
     * nothing can read a grade off water that has none, and no purification recipe can match it.
     */
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
