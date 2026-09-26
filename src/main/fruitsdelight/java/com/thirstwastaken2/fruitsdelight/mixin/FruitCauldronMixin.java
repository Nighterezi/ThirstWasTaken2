package com.thirstwastaken2.fruitsdelight.mixin;

import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A full cauldron of sea water takes no lemon slice and no jam. Fruits Delight turns a full water
 * cauldron into its lemonade or fruit cauldron by checking the level alone, and the jello at the end of
 * that chain restores thirst, so sea water would become a safe drink by way of it, as it would through
 * the keg or the vat.
 *
 * <p>Every one of the mod's cauldron interactions, by hand or by dispenser, goes through {@code perform},
 * and its own cauldrons carry no purity value, so only a vanilla water cauldron holding sea water is
 * refused. Named by string, like the ingredient mixin, so the mod is not on the compile classpath.
 */
@Mixin(targets = "dev.xkmc.fruitsdelight.content.cauldrons.FDCauldronInteraction")
abstract class FruitCauldronMixin {
    @Inject(method = "perform", at = @At("HEAD"), cancellable = true)
    private void thirst$noFruitInSeaWater(BlockState state, Level level, BlockPos pos, ItemStack stack,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (WaterPurity.storedQuality(state) == WaterQuality.SALT) cir.setReturnValue(false);
    }
}
