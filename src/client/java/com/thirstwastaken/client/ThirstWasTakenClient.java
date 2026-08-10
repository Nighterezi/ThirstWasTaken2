package com.thirstwastaken.client;

import com.thirstwastaken.ThirstWasTaken;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public final class ThirstWasTakenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, ThirstWasTaken.id("thirst_bar"), ThirstHud::render);
        HudStatusBarHeightRegistry.addRight(ThirstWasTaken.id("thirst_bar"), player -> ThirstHud.shouldRender(player) ? 10 : 0);
    }
}
