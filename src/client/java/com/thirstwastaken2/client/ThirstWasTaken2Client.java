package com.thirstwastaken2.client;

import com.thirstwastaken2.ThirstWasTaken2;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class ThirstWasTaken2Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, ThirstWasTaken2.id("thirst_bar"), ThirstHud::render);
        HudStatusBarHeightRegistry.addRight(ThirstWasTaken2.id("thirst_bar"), player -> ThirstHud.shouldRender(player) ? 10 : 0);
    }
}
