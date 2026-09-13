package com.thirstwastaken2.client.config;

import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/**
 * One {@link ConfigCategory} as a vanilla options list. Leaving it keeps the edits in the live config;
 * {@link ThirstConfigScreen} decides whether they are saved or thrown away.
 */
final class ThirstCategoryScreen extends OptionsSubScreen {
    private static final int FOOTER_BUTTON_WIDTH = 150;
    /** Title, gap and preview, with the same margin above and below as vanilla's plain title header. */
    private static final int PREVIEW_HEADER_HEIGHT = 12 + 9 + 6 + ConfigPreview.HEIGHT + 8;

    private final ConfigCategory category;

    ThirstCategoryScreen(Screen parent, ConfigCategory category) {
        super(parent, Minecraft.getInstance().options,
                Component.translatable("thirstwastaken2.config.category." + category.key()));
        this.category = category;
    }

    @Override
    protected void addTitle() {
        if (!category.hasPreview()) {
            super.addTitle();
            return;
        }
        LinearLayout header = layout.addToHeader(LinearLayout.vertical().spacing(6));
        header.addChild(new StringWidget(title, font), LayoutSettings::alignHorizontallyCenter);
        header.addChild(ConfigPreview.widget(), LayoutSettings::alignHorizontallyCenter);
        layout.setHeaderHeight(PREVIEW_HEADER_HEIGHT);
    }

    @Override
    protected void addOptions() {
        category.addOptions(list, ThirstConfig.get());
    }

    @Override
    protected void addFooter() {
        LinearLayout footer = layout.addToFooter(LinearLayout.horizontal().spacing(8));
        footer.addChild(Button.builder(Component.translatable("thirstwastaken2.config.reset"), button -> reset())
                .width(FOOTER_BUTTON_WIDTH).build());
        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .width(FOOTER_BUTTON_WIDTH).build());
    }

    private void reset() {
        category.reset(ThirstConfig.get(), new ThirstConfig());
        // Widgets hold the values they were built with, so the page is rebuilt to show the defaults.
        ClientVanilla.setScreen(minecraft, new ThirstCategoryScreen(lastScreen, category));
    }
}
