package com.thirstwastaken2.createfly.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.thirstwastaken2.createfly.SampledWater;
import com.thirstwastaken2.createfly.WaterFluids;
import com.thirstwastaken2.purity.WaterQuality;
import com.zurrtum.create.content.fluids.OpenEndedPipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A pipe drawing water from the world or from a cauldron. It samples before draining, because a
 * drained cauldron no longer holds the quality it had.
 */
@Mixin(OpenEndedPipe.class)
abstract class OpenEndedPipeMixin {
    @Shadow
    private @Nullable Level world;

    @Shadow
    @Final
    private BlockPos outputPos;

    @Unique
    private final SampledWater thirst$source = new SampledWater();

    /** What the current call draws, read before it drains anything. */
    @Unique
    private @Nullable WaterQuality thirst$drawing;

    // Two plain injectors rather than one wrapper: Create calls this on every flow check, and a wrapper
    // allocates its operation object on every call.
    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"))
    private void thirst$sampleSource(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        thirst$drawing = world == null || !world.isLoaded(outputPos) ? null : thirst$source.at(world, outputPos);
    }

    @ModifyReturnValue(method = "removeFluidFromSpace", at = @At("RETURN"))
    private FluidStack thirst$stampDrawn(FluidStack drawn) {
        return WaterFluids.stampIfKnown(drawn, thirst$drawing);
    }
}
