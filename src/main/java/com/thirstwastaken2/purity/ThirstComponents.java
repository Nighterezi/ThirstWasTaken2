package com.thirstwastaken2.purity;

import com.mojang.serialization.Codec;
import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ThirstComponents {
    public static final DataComponentType<Integer> WATER_PURITY = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            ThirstWasTaken2.id("water_purity"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(WaterPurity.MIN, WaterPurity.MAX))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ThirstComponents() { }
    public static void register() { }
}
