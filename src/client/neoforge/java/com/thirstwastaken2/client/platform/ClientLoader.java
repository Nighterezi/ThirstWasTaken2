package com.thirstwastaken2.client.platform;

import com.thirstwastaken2.ThirstWasTaken2;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import squeek.appleskin.ModConfig;

import java.util.function.Predicate;

/**
 * Every client call into the mod loader, for NeoForge. The client half of
 * {@link com.thirstwastaken2.platform.Loader}, under the same rules.
 */
public final class ClientLoader {
    private ClientLoader() { }

    /**
     * Adds a row to the right-hand status bar stack, drawn after the food bar. While {@code visible}
     * holds it takes {@code height} pixels of the stack, so vanilla's air bubbles and other mods' rows
     * move up past it.
     *
     * <p>NeoForge has no height registry: each layer reads {@code rightHeight} (on {@code Hud} from
     * 26.2, on {@code Gui} before), draws there and
     * advances it, and the air layer above does the same. So the row only advances it when it is
     * visible. Fabric draws rows attached to the food bar only where vanilla draws the food bar, that is
     * when the player can be hurt; a NeoForge layer has no such condition of its own, so it is checked
     * here.
     */
    public static void addRightStatusBar(Identifier id, int height, Predicate<Player> visible, StatusBarRenderer renderer) {
        ModList.get().getModContainerById(ThirstWasTaken2.MOD_ID).orElseThrow().getEventBus()
                .addListener((RegisterGuiLayersEvent event) -> event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, id,
                        (graphics, deltaTracker) -> {
                            Minecraft minecraft = Minecraft.getInstance();
                            Player player = minecraft.player;
                            if (player == null || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()
                                    || !visible.test(player)) {
                                return;
                            }
                            // 26.2 moved the status bar stack heights from Gui into its Hud.
                            //? if >=26.2 {
                            net.minecraft.client.gui.Hud hud = minecraft.gui.hud;
                            //?} else {
                            /*net.minecraft.client.gui.Gui hud = minecraft.gui;
                            *///?}
                            renderer.render(graphics, graphics.guiHeight() - hud.rightHeight);
                            hud.rightHeight += height;
                        }));
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
            // FML loads AppleSkin's config, not AppleSkin, and reading a value before then throws.
            return ModConfig.SPEC.isLoaded() && ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY.get();
        }
    }
}
