package com.thirstwastaken2.supplementaries.mixin;

import net.mehvahdjukaar.moonlight.api.fluids.FluidContainerList;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Moonlight adds a container to a soft fluid's list through one protected method, which is how it adds
 * the buckets of a fluid's equivalents to it. The mod's terracotta bowl goes in the same way, so it is
 * one entry in one list rather than a copy of {@code moonlight:water} shipped as a data pack, which
 * would silently drop every other mod's containers the day Moonlight adds one.
 */
@Mixin(value = FluidContainerList.class, remap = false)
public interface FluidContainerListAccessor {
    @Invoker("add")
    void thirst$add(Item empty, Item filled, int servings);
}
