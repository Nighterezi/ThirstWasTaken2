package com.thirstwastaken2.client.config;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.client.ThirstHud;
import com.thirstwastaken2.client.compat.AppleSkinIntegration;
import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.tooltip.ThirstTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * A live picture of what the display settings do: a drink's tooltip next to the thirst bar above the
 * food bar, as the game would draw them right now. It reads the live config every frame, so a changed
 * option shows up at once, and the quenched and saturation levels sweep up and down so every outline
 * frame is seen.
 *
 * <p>Mod Menu opens the screen from the title screen too, where 26.1 and later cannot build an
 * {@code ItemStack} yet: item components are bound when a world loads. So the sample drink is drawn
 * from its texture and the same tooltip lines {@link ThirstTooltip} would give it, never from a stack.
 */
final class ConfigPreview {
    static final int WIDTH = 300;
    static final int HEIGHT = 58;

    private static final Identifier FOOD_EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
    private static final Identifier FOOD_FULL = Identifier.withDefaultNamespace("hud/food_full");
    /** AppleSkin's saturation outline sits on its 256x256 sheet at v = 0, a quarter per 9 px of u. */
    private static final Identifier APPLESKIN_ICONS = Identifier.fromNamespaceAndPath("appleskin", "textures/icons.png");

    /** A clean bowl of water: the drink most players will hover first. */
    private static final String SAMPLE_ID = "thirstwastaken2:terracotta_water_bowl";
    private static final int SAMPLE_PURITY = 2;
    private static final Identifier SAMPLE_TEXTURE =
            ThirstWasTaken2.id("textures/item/terracotta_water_bowl_purity_" + SAMPLE_PURITY + ".png");
    private static final Component SAMPLE_NAME = Component.translatable("item.thirstwastaken2.terracotta_water_bowl");
    /** What the bowl restores when the config file has lost its entry. */
    private static final int[] SAMPLE_FALLBACK = {4, 5};

    private static final int PADDING = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int ICON_SIZE = 16;
    private static final int BAR_WIDTH = 81;
    private static final int WHITE = 0xFFFFFFFF;
    /** One full sweep of the reserve, empty to full and back. */
    private static final long SWEEP_MILLIS = 8000L;
    private static final long EXHAUSTION_MILLIS = 2500L;

    private ConfigPreview() { }

    static AbstractWidget widget() {
        return ClientVanilla.canvas(WIDTH, HEIGHT, Component.translatable("thirstwastaken2.config.preview"),
                ConfigPreview::paint);
    }

    private static void paint(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        Font font = Minecraft.getInstance().font;
        graphics.fill(x, y, x + width, y + height, 0x90000000);
        border(graphics, x, y, width, height, 0x40FFFFFF);

        paintTooltip(graphics, font, x + PADDING, y + PADDING);

        long now = Util.getMillis();
        int level = sweep(now);
        float exhaustion = 4.0F * (now % EXHAUSTION_MILLIS) / EXHAUSTION_MILLIS;
        int right = x + width - PADDING;
        int foodTop = y + height - PADDING - 9;
        int thirstTop = foodTop - 10;

        ThirstHud.drawBar(graphics, right, thirstTop, 20, level, exhaustion, AppleSkin.quenchedOverlay(),
                AppleSkinIntegration.shouldShowExhaustion(), false, false);
        paintFood(graphics, right, foodTop, level);

        Component caption = Component.translatable("thirstwastaken2.config.preview");
        ClientVanilla.text(graphics, font, caption, right - BAR_WIDTH, y + PADDING, 0xFFA0A0A0);
    }

    /** The bowl's name and the lines the mod gives it, in a box shaped like a tooltip. */
    private static void paintTooltip(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        List<Component> lines = new ArrayList<>();
        lines.add(SAMPLE_NAME);
        lines.add(WaterPurity.tooltip(SAMPLE_PURITY));
        if (AppleSkin.showsTooltipDroplets()) {
            int[] values = ThirstConfig.get().drinks.getOrDefault(SAMPLE_ID, SAMPLE_FALLBACK);
            Component thirst = ThirstTooltip.thirst(values[0]);
            Component quenched = ThirstTooltip.quenched(values[1], AppleSkin.quenchedOverlay());
            if (thirst != null) lines.add(thirst);
            if (quenched != null) lines.add(quenched);
        }

        int textWidth = 0;
        for (Component line : lines) textWidth = Math.max(textWidth, font.width(line));
        int boxX = x + ICON_SIZE + 4;
        int boxWidth = textWidth + 8;
        int boxHeight = lines.size() * LINE_HEIGHT + 4;

        ClientVanilla.blit(graphics, SAMPLE_TEXTURE, x, y + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE, WHITE);
        graphics.fill(boxX, y, boxX + boxWidth, y + boxHeight, 0xF0100010);
        border(graphics, boxX, y, boxWidth, boxHeight, 0xFF3A1B6B);
        for (int i = 0; i < lines.size(); i++) {
            ClientVanilla.text(graphics, font, lines.get(i), boxX + 4, y + 3 + i * LINE_HEIGHT, WHITE);
        }
    }

    /** The vanilla food bar, full, with AppleSkin's own saturation outline on it when AppleSkin is there. */
    private static void paintFood(GuiGraphicsExtractor graphics, int right, int top, int saturation) {
        for (int i = 0; i < 10; i++) {
            int x = right - i * 8 - 9;
            ClientVanilla.blitSprite(graphics, FOOD_EMPTY, x, top, 9, 9);
            ClientVanilla.blitSprite(graphics, FOOD_FULL, x, top, 9, 9);
            float effective = saturation / 2.0F - i;
            if (!AppleSkin.isLoaded() || effective <= 0.0F) continue;
            int u = effective >= 1.0F ? 27 : effective > 0.5F ? 18 : effective > 0.25F ? 9 : 0;
            ClientVanilla.blit(graphics, APPLESKIN_ICONS, x, top, u, 0, 9, 9, 256, 256, WHITE);
        }
    }

    /** 0 to 20 and back over {@link #SWEEP_MILLIS}, holding a moment at each end. */
    private static int sweep(long now) {
        float phase = (now % SWEEP_MILLIS) / (float) SWEEP_MILLIS;
        float triangle = phase < 0.5F ? phase * 2.0F : 2.0F - phase * 2.0F;
        return Math.round(Math.clamp(triangle * 1.2F - 0.1F, 0.0F, 1.0F) * 20.0F);
    }

    private static void border(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int argb) {
        graphics.fill(x, y, x + width, y + 1, argb);
        graphics.fill(x, y + height - 1, x + width, y + height, argb);
        graphics.fill(x, y + 1, x + 1, y + height - 1, argb);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }
}
