package com.thirstwastaken2.dev.mixin;

import com.thirstwastaken2.dev.agent.thirst.Recorder;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Takes a recording's frame at the start of a pass of the game loop, before its ticks and its render:
 * the main target then holds the frame the last pass drew, and the virtual pointer has not moved since
 * that frame was drawn. See {@link Recorder}.
 */
@Mixin(Minecraft.class)
abstract class FrameStartMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    private void thirst$recordFrame(boolean advanceGameTime, CallbackInfo info) {
        Recorder.frameStart((Minecraft) (Object) this);
    }
}
