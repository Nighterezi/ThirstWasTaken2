package com.thirstwastaken2.supplementaries.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import com.thirstwastaken2.supplementaries.WaterSoftFluid;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Moonlight copies a component from a container to the fluid it becomes, and back, only when the soft
 * fluid lists it as preserved. {@code moonlight:water} lists another thirst mod's grade component, not
 * this one's, so water poured into a jar came back out of it plain.
 *
 * <p>The two hooks on the stack itself cover the mod's own rules; this is what carries the grade across
 * the boundary at all, in both directions, and it is also what carries it onto a NeoForge fluid stack
 * and back. The answer never changes for one registry entry, so it is worked out once per entry, and
 * only for the one that has been marked as water: before a world has loaded nothing is known to be
 * water yet, and an answer taken then would be wrong to keep.
 *
 * <p>This mixin also carries the mark itself; see {@link WaterSoftFluid}.
 */
@Mixin(value = SoftFluid.class, remap = false)
abstract class SoftFluidMixin implements WaterSoftFluid {
    @Unique
    private boolean thirst$water;

    @Unique
    private HolderSet<DataComponentType<?>> thirst$preserved;

    @Override
    public void thirst$markWater() {
        thirst$water = true;
    }

    @Override
    public boolean thirst$isWater() {
        return thirst$water;
    }

    @ModifyReturnValue(method = "getPreservedComponents", at = @At("RETURN"))
    private HolderSet<DataComponentType<?>> thirst$keepQuality(HolderSet<DataComponentType<?>> original) {
        if (!thirst$water) return original;
        if (thirst$preserved == null) thirst$preserved = SoftFluidQuality.preserving(original);
        return thirst$preserved;
    }
}
