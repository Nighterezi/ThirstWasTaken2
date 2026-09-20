package com.thirstwastaken2.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * The client-side half of {@code com.thirstwastaken2.platform.Vanilla}: every client vanilla call
 * whose shape differs between the supported Minecraft versions. Same rules apply — plumbing only,
 * one signature for every version.
 */
public final class ClientVanilla {
    private ClientVanilla() { }

    /** Whether the player has hidden the HUD (F1). */
    public static boolean isHudHidden(Minecraft minecraft) {
        // 26.2 split the HUD out of Gui into its own object; before that the flag was an option.
        //? if >=26.2 {
        return minecraft.gui.hud.isHidden();
        //?} else {
        /*return minecraft.options.hideGui;
        *///?}
    }

    /** Opens {@code screen}, or closes the current one when it is null. 26.2 moved this onto the GUI. */
    public static void setScreen(Minecraft minecraft, net.minecraft.client.gui.screens.Screen screen) {
        //? if >=26.2 {
        minecraft.gui.setScreen(screen);
        //?} else {
        /*minecraft.setScreen(screen);
        *///?}
    }

    /** Adds a centred line of text to an options list, as a section heading. */
    public static void addHeader(OptionsList list, net.minecraft.network.chat.Component text) {
        //? if >1.21.1 {
        list.addHeader(text);
        //?} else {
        /*// No headings before a later release: a centred text widget as its own row stands in for one.
        addFullWidthRow(list, new net.minecraft.client.gui.components.StringWidget(
                list.getRowWidth(), 20, text, Minecraft.getInstance().font).alignCenter());
        *///?}
    }

    /** Adds a widget as its own full-width row of an options list. */
    public static void addFullWidthRow(OptionsList list, AbstractWidget widget) {
        //? if >=26.2 {
        list.addBig(widget);
        //?} else {
        /*list.addSmall(java.util.List.of(widget));
        *///?}
    }

    /** What a {@link #canvas} draws every frame, given its own bounds. */
    @FunctionalInterface
    public interface Painter {
        void paint(GuiGraphicsExtractor graphics, int x, int y, int width, int height);
    }

    /**
     * A widget that only draws, for placing custom drawing in a layout. It takes no input and no
     * focus, and a screen reader reads {@code narration}. 26.1 renamed the method a widget draws in.
     */
    public static AbstractWidget canvas(int width, int height, Component narration, Painter painter) {
        AbstractWidget canvas = new AbstractWidget(0, 0, width, height, narration) {
            //? if >=26.1 {
            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, getX(), getY(), getWidth(), getHeight());
            }
            //?} else {
            /*@Override
            protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, getX(), getY(), getWidth(), getHeight());
            }
            *///?}

            @Override
            protected void updateWidgetNarration(NarrationElementOutput output) {
                output.add(NarratedElementType.TITLE, getMessage());
            }
        };
        canvas.active = false;
        return canvas;
    }

    /** Draws a line of text with a shadow. {@code argb} needs its alpha, which later releases honour. */
    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int argb) {
        //? if >=26.1 {
        graphics.text(font, text, x, y, argb, true);
        //?} else {
        /*graphics.drawString(font, text, x, y, argb, true);
        *///?}
    }

    /** Draws a sprite from the GUI atlas, such as vanilla's {@code hud/food_full}. */
    public static void blitSprite(GuiGraphicsExtractor graphics, Identifier sprite, int x, int y, int width, int height) {
        //? if >1.21.1 {
        graphics.blitSprite(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        //?} else {
        /*com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        graphics.blitSprite(sprite, x, y, width, height);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        *///?}
    }

    /**
     * Draws a {@code width} by {@code height} region of a texture sheet at {@code u, v}, tinted with
     * {@code argb}. Later releases pass the render pipeline and the tint with the draw call; on
     * 1.21.1 both are global render state, set before the draw and reset after it.
     */
    public static void blit(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight, int argb) {
        //? if >1.21.1 {
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v,
                width, height, textureWidth, textureHeight, argb);
        //?} else {
        /*com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        graphics.setColor(((argb >> 16) & 0xFF) / 255.0F, ((argb >> 8) & 0xFF) / 255.0F,
                (argb & 0xFF) / 255.0F, ((argb >>> 24) & 0xFF) / 255.0F);
        graphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        *///?}
    }

    /** Opens a folder or file in the player's file manager. 26.3 moved this off {@code Util.OS}. */
    public static void openPath(java.nio.file.Path path) {
        //? if >=26.3 {
        com.mojang.blaze3d.Blaze3D.openPath(path);
        //?} else {
        /*net.minecraft.util.Util.getPlatform().openPath(path);
        *///?}
    }
}
