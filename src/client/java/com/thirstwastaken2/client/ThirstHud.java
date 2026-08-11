package com.thirstwastaken2.client;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ThirstData;
import com.thirstwastaken2.data.ThirstManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class ThirstHud {
    private static final Identifier ICONS = ThirstWasTaken2.id("textures/gui/thirst_icons.png");
    private static final Identifier OVERLAY_ICONS = ThirstWasTaken2.id("textures/gui/appleskin_icons.png");
    private static final Identifier BAR_ID = ThirstWasTaken2.id("thirst_bar");
    private static final RandomSource RANDOM = RandomSource.create();

    private static final int ICON_SIZE = 9;
    private static final int ICONS_TEXTURE_WIDTH = 41;
    private static final int ICONS_TEXTURE_HEIGHT = 9;
    /**
     * Fill frames on the sheet, driest first. Frames share their transparent edge columns, so the
     * stride is 8 rather than {@link #ICON_SIZE}.
     */
    private static final int U_EMPTY = 0;
    private static final int[] FILL_FRAMES = {8, 16, 24, 32};
    /** Units of thirst each entry of {@link #FILL_FRAMES} needs; one droplet holds two. */
    private static final float[] FILL_THRESHOLDS = {0.5F, 1.0F, 1.5F, 2.0F};
    private static final int OVERLAY_TEXTURE_SIZE = 256;
    private static final int BAR_WIDTH = 81;
    /** The exhaustion strip is a hard-edged dither pattern, so it needs the original's 75% alpha. */
    private static final int EXHAUSTION_TINT = 0xBFFFFFFF;
    private static final int OPAQUE = 0xFFFFFFFF;
    private static final float MAX_EXHAUSTION = 4.0F;

    private ThirstHud() { }

    public static boolean shouldRender(Player player) {
        if (player == null || !player.isAlive() || player.getVehicle() instanceof LivingEntity) return false;
        Minecraft minecraft = Minecraft.getInstance();
        return !minecraft.gui.hud.isHidden() && ThirstManager.get(player).enabled();
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Player player = Minecraft.getInstance().player;
        if (!shouldRender(player)) return;

        ThirstConfig config = ThirstConfig.get();
        ThirstData data = ThirstManager.get(player);
        int thirst = data.thirst();
        int quenched = data.quenched();

        int right = graphics.guiWidth() / 2 + 91 + config.thirstBarXOffset;
        int top = graphics.guiHeight() - HudStatusBarHeightRegistry.getHeight(BAR_ID) + config.thirstBarYOffset;

        if (config.showExhaustionUnderlay) {
            renderExhaustion(graphics, data.exhaustion(), right, top);
        }

        // Vanilla shakes the hunger bar once saturation runs out; the thirst bar mirrors that.
        boolean shake = quenched <= 0;
        int shakePeriod = thirst * 3 + 1;

        float level = thirst - drainedFraction(config, data);

        for (int i = 0; i < 10; i++) {
            int x = right - i * 8 - ICON_SIZE;
            int y = top;
            if (shake && player.tickCount % shakePeriod == 0) y += RANDOM.nextInt(3) - 1;

            icon(graphics, x, y, U_EMPTY);
            int fill = fillFrame(level - i * 2);
            if (fill >= 0) {
                icon(graphics, x, y, fill);
            }

            if (config.showQuenchedOverlay) {
                renderQuenched(graphics, x, y, quenched / 2.0F - i);
            }
        }
    }

    /**
     * How much of the next thirst point has already been eaten by exhaustion, as a 0..1 fraction.
     * Exhaustion only reaches thirst once quenched is gone, so a quenched player never drains.
     */
    private static float drainedFraction(ThirstConfig config, ThirstData data) {
        if (!config.smoothThirstDrain || data.quenched() > 0) return 0.0F;
        return Math.min(Math.max(data.exhaustion(), 0.0F), MAX_EXHAUSTION) / MAX_EXHAUSTION;
    }

    /** Texture u of the wettest frame this droplet has earned, or -1 when it is dry. */
    private static int fillFrame(float units) {
        for (int i = FILL_FRAMES.length - 1; i >= 0; i--) {
            if (units >= FILL_THRESHOLDS[i]) return FILL_FRAMES[i];
        }
        return -1;
    }

    private static void icon(GuiGraphicsExtractor graphics, int x, int y, int u) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, x, y, u, 0, ICON_SIZE, ICON_SIZE,
                ICONS_TEXTURE_WIDTH, ICONS_TEXTURE_HEIGHT);
    }

    private static void renderExhaustion(GuiGraphicsExtractor graphics, float exhaustion, int right, int top) {
        int width = Math.min(BAR_WIDTH, Math.max(0, (int) (exhaustion / MAX_EXHAUSTION * BAR_WIDTH)));
        if (width <= 0) return;
        graphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_ICONS, right - width, top,
                BAR_WIDTH - width, 18, width, ICON_SIZE,
                OVERLAY_TEXTURE_SIZE, OVERLAY_TEXTURE_SIZE, EXHAUSTION_TINT);
    }

    private static void renderQuenched(GuiGraphicsExtractor graphics, int x, int y, float effective) {
        if (effective <= 0.0F) return;
        int u = effective >= 1.0F ? 27 : effective > 0.5F ? 18 : effective > 0.25F ? 9 : 0;
        graphics.blit(RenderPipelines.GUI_TEXTURED, OVERLAY_ICONS, x, y, u, 0, ICON_SIZE, ICON_SIZE,
                OVERLAY_TEXTURE_SIZE, OVERLAY_TEXTURE_SIZE, OPAQUE);
    }
}
