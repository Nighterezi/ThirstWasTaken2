package com.thirstwastaken2.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

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

    /** What a {@link #canvas} or {@link #button} draws every frame, given the widget for its bounds and state. */
    @FunctionalInterface
    public interface Painter {
        void paint(GuiGraphicsExtractor graphics, AbstractWidget widget, int mouseX, int mouseY);
    }

    /**
     * A widget that only draws, for placing custom drawing in a layout. It takes no input and no
     * focus, and a screen reader reads {@code narration}. It still shows a tooltip on hover. 26.1
     * renamed the method a widget draws in.
     */
    public static AbstractWidget canvas(int width, int height, Component narration, Painter painter) {
        AbstractWidget canvas = new AbstractWidget(0, 0, width, height, narration) {
            //? if >=26.1 {
            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, this, mouseX, mouseY);
            }
            //?} else {
            /*@Override
            protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, this, mouseX, mouseY);
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

    /**
     * A button drawn entirely by {@code painter}: it clicks, focuses, narrates and plays the click sound
     * like a vanilla button. 1.21.11 made the button draw its contents through a method of its own, and
     * 26.1 renamed it.
     */
    public static AbstractWidget button(int width, int height, Component message, Runnable onPress, Painter painter) {
        return new Button(0, 0, width, height, message, button -> onPress.run(), narration -> narration.get()) {
            //? if >=26.1 {
            @Override
            protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, this, mouseX, mouseY);
            }
            //?}
            //? if >1.21.1 <26.1 {
            /*@Override
            protected void renderContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, this, mouseX, mouseY);
            }
            *///?}
            //? if <=1.21.1 {
            /*@Override
            protected void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
                painter.paint(graphics, this, mouseX, mouseY);
            }
            *///?}
        };
    }

    /** Draws a line of text with a shadow. {@code argb} needs its alpha, which later releases honour. */
    public static void text(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int argb) {
        //? if >=26.1 {
        graphics.text(font, text, x, y, argb, true);
        //?} else {
        /*graphics.drawString(font, text, x, y, argb, true);
        *///?}
    }

    /** Draws one wrapped line of text, as {@code Font.split} returns them, with a shadow. */
    public static void text(GuiGraphicsExtractor graphics, Font font, FormattedCharSequence text, int x, int y, int argb) {
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
