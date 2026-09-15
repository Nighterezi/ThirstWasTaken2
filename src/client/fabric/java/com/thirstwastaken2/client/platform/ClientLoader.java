package com.thirstwastaken2.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import squeek.appleskin.ModConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Every client call into the mod loader, for Fabric. The client half of
 * {@link com.thirstwastaken2.platform.Loader}, under the same rules.
 */
public final class ClientLoader {
    /** Rows added before 1.21.6, drawn by {@code GuiMixin}. Stays empty on later versions. */
    private static final List<RightStatusBar> RIGHT_STATUS_BARS = new ArrayList<>();
    /** How far above the bottom of the screen vanilla's food bar sits, the base of the right-hand stack. */
    private static final int FOOD_BAR_TOP = 39;

    private record RightStatusBar(int height, Predicate<Player> visible, StatusBarRenderer renderer) { }

    private ClientLoader() { }

    /**
     * Adds a row to the right-hand status bar stack, drawn after the food bar. While {@code visible}
     * holds it takes {@code height} pixels of the stack, so vanilla's air bubbles and other mods' rows
     * move up past it.
     */
    public static void addRightStatusBar(Identifier id, int height, Predicate<Player> visible, StatusBarRenderer renderer) {
        //? if >=1.21.6 {
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.attachElementAfter(
                net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements.FOOD_BAR, id,
                (graphics, deltaTracker) -> renderer.render(graphics, graphics.guiHeight()
                        - net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry.getHeight(id)));
        net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry.addRight(
                id, player -> visible.test(player) ? height : 0);
        //?} else {
        /*// Fabric API has no HUD element or status bar registry before 1.21.6. GuiMixin draws these
        // rows at the point vanilla is about to draw the air bubbles, and moves the bubbles up.
        RIGHT_STATUS_BARS.add(new RightStatusBar(height, visible, renderer));
        *///?}
    }

    /**
     * Whether AppleSkin's own exhaustion underlay setting is on. Only call once AppleSkin is known to be
     * loaded: it names AppleSkin's classes.
     */
    public static boolean appleSkinShowsExhaustionUnderlay() {
        return AppleSkinConfig.showsExhaustionUnderlay();
    }

    /** Loaded only when asked, so {@link ClientLoader} itself never names AppleSkin's classes. */
    private static final class AppleSkinConfig {
        private static boolean showsExhaustionUnderlay() {
            ModConfig config = ModConfig.INSTANCE;
            return config != null && config.showFoodExhaustionHudUnderlay;
        }
    }

    /**
     * Draws every visible row added before 1.21.6, stacked up from the food bar, then moves whatever
     * vanilla draws next up past them. Called by {@code GuiMixin}.
     *
     * @return whether a pose was pushed, which the caller has to pop once vanilla is done
     */
    public static boolean renderRightStatusBars(GuiGraphicsExtractor graphics) {
        Player player = Minecraft.getInstance().player;
        int stacked = 0;
        for (RightStatusBar bar : RIGHT_STATUS_BARS) {
            if (player == null || !bar.visible().test(player)) continue;
            stacked += bar.height();
            bar.renderer().render(graphics, graphics.guiHeight() - FOOD_BAR_TOP - stacked);
        }
        if (stacked == 0) return false;
        //? if <1.21.6 {
        /*graphics.pose().pushPose();
        graphics.pose().translate(0.0F, -stacked, 0.0F);
        *///?}
        return true;
    }
}
