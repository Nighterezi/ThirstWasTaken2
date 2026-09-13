package com.thirstwastaken2.client.platform;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Draws one row of the status bar stack above the hotbar. */
@FunctionalInterface
public interface StatusBarRenderer {
    /** {@code top} is the row's y, already moved up past every row stacked below it. */
    void render(GuiGraphicsExtractor graphics, int top);
}
