package com.thirstwastaken2.client.platform;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * Every client call into the mod loader, for Fabric. The client half of
 * {@link com.thirstwastaken2.platform.Loader}, under the same rules.
 */
public final class ClientLoader {
    private ClientLoader() { }

    /**
     * Adds a row to the right-hand status bar stack, drawn after the food bar. While {@code visible}
     * holds it takes {@code height} pixels of the stack, so vanilla's air bubbles and other mods' rows
     * move up past it.
     */
    public static void addRightStatusBar(Identifier id, int height, Predicate<Player> visible, StatusBarRenderer renderer) {
        HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR, id, (graphics, deltaTracker) ->
                renderer.render(graphics, graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(id)));
        HudStatusBarHeightRegistry.addRight(id, player -> visible.test(player) ? height : 0);
    }
}
