package com.thirstwastaken2.client.config;

import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * The editor for {@code config/thirstwastaken2.json}: a live preview of the display settings, a
 * button per {@link ConfigCategory}, and Cancel or Done.
 *
 * <p>Pages write straight into the live config, so the HUD and tooltips follow every change while the
 * screen is open. Done (or Escape) saves; Cancel puts back the copy taken when the screen opened.
 */
public final class ThirstConfigScreen extends Screen {
    private static final int BUTTON_WIDTH = 150;
    private static final int SPACING = 8;

    private final Screen parent;
    private final ThirstConfig snapshot = ThirstConfig.snapshot();
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public ThirstConfigScreen(Screen parent) {
        super(Component.translatable("thirstwastaken2.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        layout.addTitleHeader(title, font);

        LinearLayout contents = layout.addToContents(LinearLayout.vertical().spacing(SPACING));
        contents.addChild(ConfigPreview.widget(), LayoutSettings::alignHorizontallyCenter);

        GridLayout grid = new GridLayout().columnSpacing(SPACING).rowSpacing(4);
        GridLayout.RowHelper rows = grid.createRowHelper(2);
        for (ConfigCategory category : ConfigCategory.values()) {
            String key = "thirstwastaken2.config.category." + category.key();
            rows.addChild(Button.builder(Component.translatable(key),
                            button -> ClientVanilla.setScreen(minecraft, new ThirstCategoryScreen(this, category)))
                    .tooltip(Tooltip.create(Component.translatable(key + ".tooltip")))
                    .width(BUTTON_WIDTH).build());
        }
        contents.addChild(grid, LayoutSettings::alignHorizontallyCenter);
        contents.addChild(new StringWidget(Component.translatable("thirstwastaken2.config.note")
                .withStyle(ChatFormatting.GRAY), font), LayoutSettings::alignHorizontallyCenter);

        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(SPACING));
        footer.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> cancel()).width(BUTTON_WIDTH).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).width(BUTTON_WIDTH).build());

        layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @Override
    protected void repositionElements() {
        layout.arrangeElements();
    }

    /** Done and Escape both keep the edits. */
    @Override
    public void onClose() {
        ThirstConfig.commit();
        ClientVanilla.setScreen(minecraft, parent);
    }

    private void cancel() {
        ThirstConfig.restore(snapshot);
        ClientVanilla.setScreen(minecraft, parent);
    }
}
