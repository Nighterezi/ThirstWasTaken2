package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thirstwastaken2.createfly.SampledWater;
import com.thirstwastaken2.createfly.WaterFluids;
import com.zurrtum.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/** A Hose Pulley drawing from a body of water, graded where the hose ends as the original mod did. */
@Mixin(FluidDrainingBehaviour.class)
abstract class FluidDrainingBehaviourMixin {
    @Unique
    private final SampledWater thirst$source = new SampledWater();

    @ModifyReturnValue(method = "getDrainableFluid", at = @At("RETURN"))
    private FluidStack thirst$stampDrawn(FluidStack drawn, BlockPos rootPos) {
        Level level = ((FluidDrainingBehaviour) (Object) this).getLevel();
        return WaterFluids.isWater(drawn) && level != null ? WaterFluids.stampIfKnown(drawn, thirst$source.at(level, rootPos)) : drawn;
    }
}
