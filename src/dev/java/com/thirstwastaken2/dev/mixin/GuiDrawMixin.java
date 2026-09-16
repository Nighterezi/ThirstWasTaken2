package com.thirstwastaken2.dev.mixin;

//? if >=1.21.2 {
import com.mojang.blaze3d.pipeline.RenderPipeline;
//?}
import com.thirstwastaken2.dev.agent.thirst.HudRecord;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records the rectangles of vanilla's food and air sprites for numeric HUD layout checks. */
@Mixin(GuiGraphicsExtractor.class)
abstract class GuiDrawMixin {
    // The render pipeline parameter was added with the rendering changes after 1.21.1.
    //? if >=1.21.2 {
    @Inject(method = "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
            at = @At("HEAD"))
    private void thirst$recordHudSprite(RenderPipeline pipeline, Identifier sprite, int x, int y,
                                        int width, int height, CallbackInfo info) {
        HudRecord.sprite(sprite.toString(), x, y, width, height);
    }
    //?} else {
    /*@Inject(method = "blitSprite(Lnet/minecraft/resources/Identifier;IIII)V", at = @At("HEAD"))
    private void thirst$recordHudSprite(Identifier sprite, int x, int y, int width, int height,
                                        CallbackInfo info) {
        HudRecord.sprite(sprite.toString(), x, y, width, height);
    }
    *///?}
}
