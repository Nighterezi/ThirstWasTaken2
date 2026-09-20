package com.thirstwastaken2.supplementaries.mixin;

import com.thirstwastaken2.supplementaries.SoftFluidQuality;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluid;
import net.mehvahdjukaar.moonlight.core.fluid.SoftFluidInternal;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Where Moonlight builds the map from item to soft fluid, which is what decides whether an item can be
 * poured out at all. It is the last thing a load does with soft fluids and it reads every container
 * list, so the mod's terracotta bowl is written into water's list here, one moment before.
 *
 * <p>This is also where the water entry is taken for {@link SoftFluidQuality#isWater(SoftFluid)}. Water
 * has to be asked for by registry key, because the entry's own account of itself is the {@code c:water}
 * fluid tag, which is not bound yet while a world is loading. Asking here and not earlier matters:
 * Moonlight remembers the holder it resolves for a key, so a key looked up before the registry is
 * finished is remembered wrong for the rest of the load, and with water wrong nothing that pours
 * works.
 */
@Mixin(value = SoftFluidInternal.class, remap = false)
abstract class SoftFluidInternalMixin {
    @Inject(method = "populateItemSlaveMap", at = @At("HEAD"))
    private static void thirst$adoptWater(HolderLookup.Provider registries, Map<Item, Holder<SoftFluid>> itemMap,
                                          CallbackInfo ci) {
        SoftFluidQuality.adoptWater(registries);
    }
}
