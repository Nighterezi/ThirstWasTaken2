package com.thirstwastaken2.dev.mixin;

import com.thirstwastaken2.client.ThirstHud;
import com.thirstwastaken2.config.QuenchedOverlay;
import com.thirstwastaken2.dev.agent.thirst.HudRecord;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records where the thirst bar was drawn, for the agent's {@code client.hud} probe.
 *
 * <p>It lives in the dev source set and targets the mod's own class rather than the mod recording its
 * own rectangles: a published jar carries no tooling, and the rule for the agent is that the mod is
 * never changed for its sake. The bodies stay one line each, like every other mixin in this repository;
 * what they call is a normal class.
 *
 * <p>{@code render} is bracketed as well as {@code drawBar} because the config screen's preview draws
 * the bar through the same method, and the two have to be told apart. Its {@code RETURN} injection
 * catches the early return too, which is what "the bar is hidden" looks like from here.
 */
@Mixin(ThirstHud.class)
abstract class ThirstHudMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private static void thirst$beginHud(GuiGraphicsExtractor graphics, int stackTop, CallbackInfo info) {
        HudRecord.beginHud();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private static void thirst$endHud(GuiGraphicsExtractor graphics, int stackTop, CallbackInfo info) {
        HudRecord.endHud();
    }

    @Inject(method = "drawBar", at = @At("HEAD"))
    private static void thirst$recordBar(GuiGraphicsExtractor graphics, int right, int top, int thirst,
                                         int quenched, float exhaustion, QuenchedOverlay overlay,
                                         boolean exhaustionStrip, boolean shake, boolean parched, boolean upsetStomach,
                                         CallbackInfo info) {
        HudRecord.bar(right, top, thirst, quenched, exhaustion, overlay.name(), exhaustionStrip, shake, parched,
                upsetStomach);
    }
}
