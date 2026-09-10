package com.thirstwastaken2.client.platform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;

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

    /** Adds a widget as its own full-width row of an options list. */
    public static void addFullWidthRow(OptionsList list, AbstractWidget widget) {
        //? if >=26.2 {
        list.addBig(widget);
        //?} else {
        /*list.addSmall(java.util.List.of(widget));
        *///?}
    }
}
