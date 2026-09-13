package com.thirstwastaken2.fabric.mixin;

import com.thirstwastaken2.client.platform.ClientLoader;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The status bar registry Fabric API gained in 1.21.6, for the versions before it: the rows added
 * through {@code ClientLoader.addRightStatusBar} are drawn right after the food bar, and the air
 * bubbles vanilla draws next are moved up past them. Empty on 1.21.6 and later, where the registry
 * does both.
 */
@Mixin(Gui.class)
abstract class GuiMixin {
    //? if <1.21.6 {
    /*@Unique private boolean thirst$stacked;

    @Inject(method = "renderPlayerHealth", at = @At(value = "INVOKE_STRING",
            target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=air"))
    private void thirst$drawRightStatusBars(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        thirst$stacked = ClientLoader.renderRightStatusBars(graphics);
    }

    @Inject(method = "renderPlayerHealth", at = @At("TAIL"))
    private void thirst$restoreAirBubbles(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (thirst$stacked) graphics.pose().popPose();
        thirst$stacked = false;
    }
    *///?}
}
