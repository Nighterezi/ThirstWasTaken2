package com.thirstwastaken2.client.config;

import com.thirstwastaken2.client.platform.ClientVanilla;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The colours and small drawing helpers every part of the config screen shares. The panels are
 * translucent, so the blurred world or title panorama vanilla draws behind every screen still shows.
 */
final class ConfigTheme {
    /** Water blue, for the selected page, focus and the header rule. */
    static final int ACCENT = 0xFF4FB8F0;
    /** Amber, marking a setting that differs from its default. */
    static final int CHANGED = 0xFFF2B84B;
    static final int TEXT = 0xFFFFFFFF;
    static final int MUTED = 0xFFA3ADB8;
    static final int FAINT = 0xFF6E7781;

    static final int BAR = 0xD0101418;
    static final int SIDEBAR = 0xB00C0F12;
    static final int PANEL = 0x90000000;
    static final int ROW = 0x18FFFFFF;
    static final int ROW_HOVER = 0x30FFFFFF;
    static final int SELECTED = 0x38FFFFFF;
    static final int LINE = 0x30FFFFFF;
    static final int NOTE = 0x40F2B84B;

    private static final String ELLIPSIS = "...";
    private static final int WHITE = 0xFFFFFFFF;

    private ConfigTheme() { }

    static void border(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int argb) {
        graphics.fill(x, y, x + width, y + 1, argb);
        graphics.fill(x, y + height - 1, x + width, y + height, argb);
        graphics.fill(x, y + 1, x + 1, y + height - 1, argb);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, argb);
    }

    /** {@code text} in one line of at most {@code width} pixels, cut with an ellipsis when it is longer. */
    static void clippedText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int width, int argb) {
        if (font.width(text) <= width) {
            ClientVanilla.text(graphics, font, text, x, y, argb);
            return;
        }
        String cut = font.plainSubstrByWidth(text.getString(), Math.max(0, width - font.width(ELLIPSIS)));
        ClientVanilla.text(graphics, font, Component.literal(cut + ELLIPSIS).withStyle(text.getStyle()), x, y, argb);
    }

    /** A whole 16x16 texture, such as an item's, drawn at its own size. */
    static void icon(GuiGraphicsExtractor graphics, Identifier texture, int x, int y) {
        ClientVanilla.blit(graphics, texture, x, y, 0, 0, 16, 16, 16, 16, WHITE);
    }
}
