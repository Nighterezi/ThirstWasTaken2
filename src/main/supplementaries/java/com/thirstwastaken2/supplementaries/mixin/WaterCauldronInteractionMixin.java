package com.thirstwastaken2.supplementaries.mixin;

import com.thirstwastaken2.supplementaries.CauldronQuality;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Supplementaries' faucet behaviour for vanilla cauldrons. It offers plain water when it drains one and
 * places a default water cauldron when it fills one, so a faucet between two cauldrons turned dirty
 * water clean in one pass. The class is package-private, so it is named rather than imported.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.WaterCauldronInteraction",
        remap = false)
abstract class WaterCauldronInteractionMixin {
    @Inject(method = "getProvidedFluid", at = @At("RETURN"))
    private void thirst$offerStoredQuality(Level level, BlockPos pos, Direction dir, BlockState source,
                                           CallbackInfoReturnable<FluidOffer> cir) {
        CauldronQuality.offer(cir.getReturnValue(), source);
    }

    @Inject(method = "fill", at = @At("RETURN"))
    private void thirst$keepPouredQuality(Level level, BlockPos pos, BlockState state, FluidOffer offer,
                                          CallbackInfoReturnable<Integer> cir) {
        CauldronQuality.filled(level, pos, state, offer, cir.getReturnValue());
    }
}
