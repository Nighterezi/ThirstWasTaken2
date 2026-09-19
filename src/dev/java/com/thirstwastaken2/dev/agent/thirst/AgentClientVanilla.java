package com.thirstwastaken2.dev.agent.thirst;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.util.function.Consumer;

/**
 * The client vanilla calls the agent makes whose shape moved between Minecraft versions, and that the
 * mod itself has no reason to have a seam for.
 *
 * <p>It is the same idea as {@code client/platform/ClientVanilla} and deliberately not part of it: the
 * mod's seam carries what the mod needs, and widening it so a development tool can take a screenshot
 * would put API in the published jar that no player ever reaches. Whatever the mod already has a seam
 * for — whether the HUD is hidden, opening a screen — is called through {@code ClientVanilla} rather
 * than copied here.
 */
final class AgentClientVanilla {
    private AgentClientVanilla() { }

    /** The screen currently open, or null for none. 26.2 moved the field onto the GUI. */
    static Screen screen(Minecraft minecraft) {
        //? if >=26.2 {
        return minecraft.gui.screen();
        //?} else {
        /*return minecraft.screen;
        *///?}
    }

    /**
     * How many framebuffer pixels one GUI pixel is. The HUD rectangles the agent reads are in GUI
     * pixels and framebuffer samples are in physical ones, so every conversion between the two goes
     * through this. 1.21.11 turned the scale from a fraction into a whole number, which widens to the
     * same answer and needs no branch.
     */
    static double guiScale(Minecraft minecraft) {
        return minecraft.getWindow().getGuiScale();
    }

    /**
     * The GLFW handle of the game window, for the few things no Minecraft call covers: maximising a
     * driven client's window. 1.21.11 renamed the accessor from {@code getWindow} to {@code handle}.
     */
    static long windowHandle(Minecraft minecraft) {
        //? if >1.21.1 {
        return minecraft.getWindow().handle();
        //?} else {
        /*return minecraft.getWindow().getWindow();
        *///?}
    }

    /**
     * Presses and releases a mouse button over {@code screen} at GUI coordinates, the two calls the
     * mouse handler makes, and answers whether the press was taken. 1.21.9 wrapped the position and
     * the button into one event.
     */
    static boolean click(Screen screen, double x, double y, int button) {
        //? if >1.21.1 {
        net.minecraft.client.input.MouseButtonEvent event = new net.minecraft.client.input.MouseButtonEvent(x, y,
                new net.minecraft.client.input.MouseButtonInfo(button, 0));
        boolean taken = screen.mouseClicked(event, false);
        screen.mouseReleased(event);
        //?} else {
        /*boolean taken = screen.mouseClicked(x, y, button);
        screen.mouseReleased(x, y, button);
        *///?}
        return taken;
    }

    /**
     * Clicks a slot of the open container the way a screen does, through the game mode, which sends the
     * click to the server. {@code action} is the constant's name, {@code PICKUP} or {@code QUICK_MOVE}
     * for instance; 26.1 renamed {@code ClickType} to {@code ContainerInput} and kept the names.
     */
    static void clickSlot(Minecraft minecraft, Player player, int slot, int button, String action) {
        int containerId = player.containerMenu.containerId;
        //? if >=26.1 {
        minecraft.gameMode.handleContainerInput(containerId, slot, button,
                net.minecraft.world.inventory.ContainerInput.valueOf(action), player);
        //?} else {
        /*minecraft.gameMode.handleInventoryMouseClick(containerId, slot, button,
                net.minecraft.world.inventory.ClickType.valueOf(action), player);
        *///?}
    }

    /** Toggles the same HUD-hidden state as F1 without depending on a physical keyboard event. */
    static void toggleHud(Minecraft minecraft) {
        // 26.2 moved the flag into Hud and made the toggle its public operation.
        //? if >=26.2 {
        minecraft.gui.hud.toggle();
        //?} else {
        /*minecraft.options.hideGui = !minecraft.options.hideGui;
        *///?}
    }

    /**
     * Writes the framebuffer to {@code <directory>/screenshots/<name>} and calls {@code done} with
     * vanilla's own message once the file is on disk — which is not in this tick: from 1.21.11 the
     * pixels are read back from the GPU asynchronously.
     *
     * <p>A PNG rather than pixels held in memory, on purpose. It is one call whose shape barely moved
     * across four Minecraft versions, the samples are then taken with {@code javax.imageio} on any of
     * them, and the same file is the evidence a person looks at afterwards.
     */
    static void screenshot(Minecraft minecraft, File directory, String name, Consumer<Component> done) {
        // 26.2 moved the main render target from the client onto its game renderer.
        //? if >=26.2 {
        RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
        //?} else {
        /*RenderTarget target = minecraft.getMainRenderTarget();
        *///?}
        // 1.21.11 added the downscale factor; 1 is the framebuffer's own size on every version.
        //? if >1.21.1 {
        Screenshot.grab(directory, name, target, 1, done);
        //?} else {
        /*Screenshot.grab(directory, name, target, done);
        *///?}
    }
}
