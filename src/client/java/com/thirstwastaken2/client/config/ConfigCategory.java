package com.thirstwastaken2.client.config;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.QuenchedOverlay;
import com.thirstwastaken2.config.SicknessPreset;
import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * One page of the config screen: its icon in the sidebar, its settings, and any rows that are not a
 * setting. Every scalar in {@link ThirstConfig} belongs to exactly one page; the per-item maps and the
 * keyword patterns stay in the file, which Item Values opens. Reset puts back exactly the page's
 * entries, so the maps are never reset from a button.
 */
enum ConfigCategory {
    THIRST("thirst", ThirstWasTaken2.id("textures/item/waterskin_3.png"), List.of(
            ConfigEntry.percent("thirst_depletion_modifier", 0, 1000,
                    config -> config.thirstDepletionModifier, (config, value) -> config.thirstDepletionModifier = value),
            ConfigEntry.toggle("thirst_depletion_in_peaceful",
                    config -> config.thirstDepletionInPeaceful, (config, value) -> config.thirstDepletionInPeaceful = value),
            ConfigEntry.toggle("prevent_sprinting_when_thirsty",
                    config -> config.preventSprintingWhenThirsty, (config, value) -> config.preventSprintingWhenThirsty = value),
            ConfigEntry.toggle("dehydration_halts_health_regen",
                    config -> config.dehydrationHaltsHealthRegen, (config, value) -> config.dehydrationHaltsHealthRegen = value))),

    WATER("water", ThirstWasTaken2.id("textures/item/terracotta_water_bowl_purity_3.png"), List.of(
            ConfigEntry.choice("sickness_preset", SicknessPreset.values(),
                    config -> config.sicknessPreset, (config, value) -> config.sicknessPreset = value),
            ConfigEntry.grade("default_purity",
                    config -> config.defaultPurity, (config, value) -> config.defaultPurity = value),
            ConfigEntry.toggle("can_drink_by_hand",
                    config -> config.canDrinkByHand, (config, value) -> config.canDrinkByHand = value))),

    APPLESKIN("appleskin", Identifier.withDefaultNamespace("textures/item/apple.png"), List.of(
            ConfigEntry.choice("appleskin_quenched_overlay", QuenchedOverlay.values(),
                    config -> config.appleskinQuenchedOverlay, (config, value) -> config.appleskinQuenchedOverlay = value),
            ConfigEntry.toggle("appleskin_tooltip_droplets",
                    config -> config.appleskinTooltipDroplets, (config, value) -> config.appleskinTooltipDroplets = value))) {
        @Override
        void addLeadingRows(List<ConfigRow> rows) {
            rows.add(ConfigRow.preview());
            // Both settings only show anything alongside AppleSkin, so the page says so when it is
            // missing rather than offering switches that appear to do nothing.
            if (!AppleSkin.isLoaded()) rows.add(ConfigRow.note(Component.translatable("thirstwastaken2.config.appleskin_missing")));
        }
    },

    ITEMS("items", Identifier.withDefaultNamespace("textures/item/honey_bottle.png"), List.of(
            ConfigEntry.toggle("enable_drink_tag_matching",
                    config -> config.enableDrinkTagMatching, (config, value) -> config.enableDrinkTagMatching = value),
            ConfigEntry.toggle("enable_keyword_matching",
                    config -> config.enableKeywordMatching, (config, value) -> config.enableKeywordMatching = value))) {
        @Override
        void addTrailingRows(List<ConfigRow> rows) {
            rows.add(ConfigRow.action(Component.translatable("thirstwastaken2.config.open_file"),
                    Component.translatable("thirstwastaken2.config.open_file.tooltip"),
                    Component.translatable("thirstwastaken2.config.open_file.button"),
                    () -> ClientVanilla.openPath(ThirstConfig.path())));
        }
    };

    private final String key;
    private final Identifier icon;
    private final List<ConfigEntry<?>> entries;

    ConfigCategory(String key, Identifier icon, List<ConfigEntry<?>> entries) {
        this.key = key;
        this.icon = icon;
        this.entries = entries;
    }

    Component title() {
        return Component.translatable("thirstwastaken2.config.section." + key);
    }

    Component description() {
        return Component.translatable("thirstwastaken2.config.section." + key + ".tooltip");
    }

    /** A 16x16 texture drawn whole, beside the page's name in the sidebar. */
    Identifier icon() {
        return icon;
    }

    List<ConfigEntry<?>> entries() {
        return entries;
    }

    /** Rows placed between the page heading and its settings. */
    void addLeadingRows(List<ConfigRow> rows) { }

    /** Rows placed after the page's settings. */
    void addTrailingRows(List<ConfigRow> rows) { }
}
