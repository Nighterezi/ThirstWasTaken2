package com.thirstwastaken2.purity;

import com.mojang.serialization.Codec;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.item.WaterskinItem;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;

public final class ThirstComponents {
    /** Servings in a waterskin, canteen or flask. Widening the range keeps every saved stack valid. */
    public static final DataComponentType<Integer> WATER_SERVINGS = DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(0, WaterskinItem.MAX_CAPACITY))
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build();

    /** The grade of the fresh water inside. Salt water carries {@link #WATER_SALTY} instead. */
    public static final DataComponentType<Integer> WATER_PURITY = DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(WaterPurity.MIN, WaterPurity.MAX))
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build();

    /**
     * Sea water. It is not a grade: a salty container carries no {@link #WATER_PURITY} at all, so
     * nothing can read a grade off water that has none, and no purification recipe can match it.
     */
    public static final DataComponentType<Boolean> WATER_SALTY = DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL)
            .build();

    private ThirstComponents() { }

    /**
     * Registers the component types. They are built with the class, but registered only here, so touching
     * a field early never writes into a registry a loader may still have frozen.
     */
    public static void register() {
        register("water_servings", WATER_SERVINGS);
        register("water_purity", WATER_PURITY);
        register("water_salty", WATER_SALTY);
    }

    private static void register(String name, DataComponentType<?> type) {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ThirstWasTaken2.id(name), type);
    }
}
