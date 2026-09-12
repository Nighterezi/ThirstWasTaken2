package com.thirstwastaken2.mixin;

import com.thirstwastaken2.purity.WaterInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The empty cauldron. Rain and dripstone turn it into a water cauldron, which is a different block
 * from the one {@code LayeredCauldronBlockMixin} covers, so both filling routes are hooked twice.
 */
@Mixin(CauldronBlock.class)
abstract class CauldronBlockMixin {
    @Inject(method = "handlePrecipitation", at = @At("RETURN"))
    private void thirst$rainFilled(BlockState state, Level level, BlockPos pos,
                                   Biome.Precipitation precipitation, CallbackInfo ci) {
        WaterInteractions.filledByRain(state, level, pos);
    }

    @Inject(method = "receiveStalactiteDrip", at = @At("RETURN"))
    private void thirst$dripstoneFilled(BlockState state, Level level, BlockPos pos, Fluid fluid,
                                        CallbackInfo ci) {
        WaterInteractions.filledByDripstone(state, level, pos, fluid);
    }
}
