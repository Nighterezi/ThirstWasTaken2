package com.thirstwastaken2.client.config;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.QuenchedOverlay;
import com.thirstwastaken2.config.SicknessPreset;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.WaterskinItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * One page of the config screen: its icon in the sidebar, its settings, and any rows that are not a
 * setting. Every scalar in {@link ThirstConfig} belongs to exactly one page. The per-item values are
 * edited item by item on Item Values ({@link ItemValueRows}); the keyword patterns stay in the file,
 * which Item Values opens. Reset puts back exactly the page's entries, so the item values are never
 * reset from the footer, only one row at a time.
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
                    config -> config.dehydrationHaltsHealthRegen, (config, value) -> config.dehydrationHaltsHealthRegen = value),
            ConfigEntry.toggle("cold_sweat_climate",
                    config -> config.coldSweatClimate, (config, value) -> config.coldSweatClimate = value))),

    WATER("water", ThirstWasTaken2.id("textures/item/terracotta_water_bowl_purity_3.png"), List.of(
            ConfigEntry.choice("sickness_preset", SicknessPreset.values(),
                    config -> config.sicknessPreset, (config, value) -> config.sicknessPreset = value),
            ConfigEntry.grade("default_purity",
                    config -> config.defaultPurity, (config, value) -> config.defaultPurity = value),
            ConfigEntry.toggle("can_drink_by_hand",
                    config -> config.canDrinkByHand, (config, value) -> config.canDrinkByHand = value),
            quenchedPercent("quenched_percent_dirty", 0),
            quenchedPercent("quenched_percent_murky", 1),
            quenchedPercent("quenched_percent_clean", 2),
            quenchedPercent("quenched_percent_pure", 3),
            ConfigEntry.toggle("enable_sea_water",
                    config -> config.enableSeaWater, (config, value) -> config.enableSeaWater = value),
            ConfigEntry.number("sea_water_nausea_seconds", 0, ThirstConfig.MAX_EFFECT_SECONDS, ConfigEntry::seconds,
                    config -> config.seaWaterNauseaSeconds, (config, value) -> config.seaWaterNauseaSeconds = value),
            ConfigEntry.number("sea_water_parched_seconds", 0, ThirstConfig.MAX_EFFECT_SECONDS, ConfigEntry::seconds,
                    config -> config.seaWaterParchedSeconds, (config, value) -> config.seaWaterParchedSeconds = value),
            ConfigEntry.toggle("enable_rain_collection",
                    config -> config.enableRainCollection, (config, value) -> config.enableRainCollection = value),
            ConfigEntry.grade("rainwater_purity",
                    config -> config.rainwaterPurity, (config, value) -> config.rainwaterPurity = value),
            ConfigEntry.grade("dripstone_purity",
                    config -> config.dripstonePurity, (config, value) -> config.dripstonePurity = value))),

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
        void addTrailingRows(List<ConfigRow> rows, Runnable refresh) {
            rows.add(ConfigRow.action(Component.translatable("thirstwastaken2.config.open_file"),
                    Component.translatable("thirstwastaken2.config.open_file.tooltip"),
                    Component.translatable("thirstwastaken2.config.open_file.button"),
                    () -> ClientVanilla.openPath(ThirstConfig.path())));
            ItemValueRows.addPage(rows, refresh);
        }
    },

    MOD_ITEMS("mod_items", ThirstWasTaken2.id("textures/item/copper_canteen.png"), List.of(
            ConfigEntry.toggle("enable_bowls",
                    config -> config.enableBowls, (config, value) -> config.enableBowls = value),
            ConfigEntry.toggle("enable_waterskin",
                    config -> config.enableWaterskin, (config, value) -> config.enableWaterskin = value),
            ConfigEntry.toggle("enable_copper_canteen",
                    config -> config.enableCopperCanteen, (config, value) -> config.enableCopperCanteen = value),
            ConfigEntry.toggle("enable_iron_flask",
                    config -> config.enableIronFlask, (config, value) -> config.enableIronFlask = value),
            ConfigEntry.toggle("enable_copper_hanging_pot",
                    config -> config.enableCopperHangingPot, (config, value) -> config.enableCopperHangingPot = value),
            ConfigEntry.toggle("enable_iron_hanging_pot",
                    config -> config.enableIronHangingPot, (config, value) -> config.enableIronHangingPot = value))) {
        @Override
        void addLeadingRows(List<ConfigRow> rows) {
            // Recipes are only read as data loads, so a switch here does nothing until the next load.
            rows.add(ConfigRow.note(Component.translatable("thirstwastaken2.config.mod_items_reload")));
        }
    },

    CONTAINERS("containers", ThirstWasTaken2.id("textures/item/iron_flask.png"), List.of(
            ConfigEntry.number("copper_canteen_capacity", 1, WaterskinItem.MAX_CAPACITY, ConfigEntry::servings,
                    config -> config.copperCanteenCapacity, (config, value) -> config.copperCanteenCapacity = value),
            ConfigEntry.number("iron_flask_capacity", 1, WaterskinItem.MAX_CAPACITY, ConfigEntry::servings,
                    config -> config.ironFlaskCapacity, (config, value) -> config.ironFlaskCapacity = value),
            ConfigEntry.toggle("enable_boiling_in_hand",
                    config -> config.enableBoilingInHand, (config, value) -> config.enableBoilingInHand = value),
            ConfigEntry.number("copper_canteen_boil_seconds", 1, ThirstConfig.MAX_BOIL_SECONDS, ConfigEntry::seconds,
                    config -> config.copperCanteenBoilSeconds, (config, value) -> config.copperCanteenBoilSeconds = value),
            ConfigEntry.number("iron_flask_boil_seconds", 1, ThirstConfig.MAX_BOIL_SECONDS, ConfigEntry::seconds,
                    config -> config.ironFlaskBoilSeconds, (config, value) -> config.ironFlaskBoilSeconds = value),
            ConfigEntry.number("copper_hanging_pot_boil_seconds", 1, ThirstConfig.MAX_BOIL_SECONDS, ConfigEntry::seconds,
                    config -> config.copperHangingPotBoilSeconds,
                    (config, value) -> config.copperHangingPotBoilSeconds = value),
            ConfigEntry.number("iron_hanging_pot_boil_seconds", 1, ThirstConfig.MAX_BOIL_SECONDS, ConfigEntry::seconds,
                    config -> config.ironHangingPotBoilSeconds,
                    (config, value) -> config.ironHangingPotBoilSeconds = value)));

    private final String key;
    private final Identifier icon;
    private final List<ConfigEntry<?>> entries;

    ConfigCategory(String key, Identifier icon, List<ConfigEntry<?>> entries) {
        this.key = key;
        this.icon = icon;
        this.entries = entries;
    }

    /** One grade's share of a drink's quenched. The value is an element of an array, so it is set in place. */
    private static ConfigEntry<Integer> quenchedPercent(String key, int grade) {
        return ConfigEntry.number(key, 0, 100, ConfigEntry::wholePercent,
                config -> config.quenchedPercent[grade], (config, value) -> config.quenchedPercent[grade] = value);
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

    /** Rows placed after the page's settings. {@code refresh} rebuilds the page, for a row that adds or removes rows. */
    void addTrailingRows(List<ConfigRow> rows, Runnable refresh) { }
}
