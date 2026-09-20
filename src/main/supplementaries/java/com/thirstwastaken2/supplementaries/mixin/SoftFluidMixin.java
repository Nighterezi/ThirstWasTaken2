package com.thirstwastaken2.supplementaries.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thirstwastaken2.supplementaries.SoftFluidQuality;
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
 * <p>The two hooks on the stack itself cover pouring by hand; this covers every conversion Moonlight
 * makes of its own accord, which is how the grade survives becoming a NeoForge fluid stack and back.
 * The answer never changes for one registry entry, so it is worked out once per entry.
 */
@Mixin(value = SoftFluid.class, remap = false)
abstract class SoftFluidMixin {
    @Unique
    private HolderSet<DataComponentType<?>> thirst$preserved;

    @ModifyReturnValue(method = "getPreservedComponents", at = @At("RETURN"))
    private HolderSet<DataComponentType<?>> thirst$keepQuality(HolderSet<DataComponentType<?>> original) {
        if (thirst$preserved == null) {
            thirst$preserved = SoftFluidQuality.preserving((SoftFluid) (Object) this, original);
        }
        return thirst$preserved;
    }
}
