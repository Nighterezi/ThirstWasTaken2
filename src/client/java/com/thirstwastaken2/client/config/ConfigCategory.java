package com.thirstwastaken2.client.config;

import com.thirstwastaken2.client.platform.ClientVanilla;
import com.thirstwastaken2.compat.AppleSkin;
import com.thirstwastaken2.config.QuenchedOverlay;
import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.util.Util;

import static com.thirstwastaken2.client.config.ConfigOptions.chanceSlider;
import static com.thirstwastaken2.client.config.ConfigOptions.cycle;
import static com.thirstwastaken2.client.config.ConfigOptions.percentSlider;
import static com.thirstwastaken2.client.config.ConfigOptions.slider;
import static com.thirstwastaken2.client.config.ConfigOptions.text;
import static com.thirstwastaken2.client.config.ConfigOptions.toggle;

/**
 * One page of the config screen: its options, and what "Reset to Defaults" puts back. Every scalar in
 * {@link ThirstConfig} belongs to exactly one page; the per-item maps and keyword patterns stay in the
 * file, which the Item Values page opens.
 */
enum ConfigCategory {
    DEPLETION("depletion", false) {
        @Override
        void addOptions(OptionsList list, ThirstConfig config) {
            list.addSmall(
                    percentSlider("thirst_depletion_modifier", config.thirstDepletionModifier, 0, 1000,
                            value -> config.thirstDepletionModifier = value),
                    percentSlider("nether_thirst_depletion_modifier", config.netherThirstDepletionModifier, 0, 1000,
                            value -> config.netherThirstDepletionModifier = value),
                    slider("fire_resistance_dehydration_percent", config.fireResistanceDehydrationPercent, 0, 100,
                            value -> config.fireResistanceDehydrationPercent = value),
                    toggle("thirst_depletion_in_peaceful", config.thirstDepletionInPeaceful,
                            value -> config.thirstDepletionInPeaceful = value),
                    toggle("depletes_when_nauseous", config.depletesWhenNauseous,
                            value -> config.depletesWhenNauseous = value),
                    toggle("dehydration_halts_health_regen", config.dehydrationHaltsHealthRegen,
                            value -> config.dehydrationHaltsHealthRegen = value),
                    toggle("prevent_sprinting_when_thirsty", config.preventSprintingWhenThirsty,
                            value -> config.preventSprintingWhenThirsty = value));
        }

        @Override
        void reset(ThirstConfig config, ThirstConfig defaults) {
            config.thirstDepletionModifier = defaults.thirstDepletionModifier;
            config.netherThirstDepletionModifier = defaults.netherThirstDepletionModifier;
            config.fireResistanceDehydrationPercent = defaults.fireResistanceDehydrationPercent;
            config.thirstDepletionInPeaceful = defaults.thirstDepletionInPeaceful;
            config.depletesWhenNauseous = defaults.depletesWhenNauseous;
            config.dehydrationHaltsHealthRegen = defaults.dehydrationHaltsHealthRegen;
            config.preventSprintingWhenThirsty = defaults.preventSprintingWhenThirsty;
        }
    },

    DRINKING("drinking", false) {
        @Override
        void addOptions(OptionsList list, ThirstConfig config) {
            list.addSmall(
                    toggle("can_drink_by_hand", config.canDrinkByHand, value -> config.canDrinkByHand = value),
                    toggle("drink_by_hand_needs_both_hands_empty", config.drinkByHandNeedsBothHandsEmpty,
                            value -> config.drinkByHandNeedsBothHandsEmpty = value),
                    toggle("extra_thirst_converts_to_quenched", config.extraThirstConvertsToQuenched,
                            value -> config.extraThirstConvertsToQuenched = value),
                    slider("hand_drinking_thirst", config.handDrinkingThirst, 0, 20,
                            value -> config.handDrinkingThirst = value),
                    slider("hand_drinking_quenched", config.handDrinkingQuenched, 0, 20,
                            value -> config.handDrinkingQuenched = value));
        }

        @Override
        void reset(ThirstConfig config, ThirstConfig defaults) {
            config.canDrinkByHand = defaults.canDrinkByHand;
            config.drinkByHandNeedsBothHandsEmpty = defaults.drinkByHandNeedsBothHandsEmpty;
            config.extraThirstConvertsToQuenched = defaults.extraThirstConvertsToQuenched;
            config.handDrinkingThirst = defaults.handDrinkingThirst;
            config.handDrinkingQuenched = defaults.handDrinkingQuenched;
        }
    },

    PURITY("purity", false) {
        @Override
        void addOptions(OptionsList list, ThirstConfig config) {
            list.addSmall(
                    slider("default_purity", config.defaultPurity, 0, 3, value -> config.defaultPurity = value),
                    slider("rainwater_purity", config.rainwaterPurity, 0, 3,
                            value -> config.rainwaterPurity = value),
                    slider("dripstone_purity", config.dripstonePurity, 0, 3,
                            value -> config.dripstonePurity = value),
                    toggle("quench_when_debuffed", config.quenchWhenDebuffed,
                            value -> config.quenchWhenDebuffed = value));

            ClientVanilla.addHeader(list, text("category.purity_chances"));
            for (int purity = 0; purity < 4; purity++) {
                int index = purity;
                list.addSmall(
                        chanceSlider("nausea_chance", index, config.nauseaChance[index],
                                value -> config.nauseaChance[index] = value),
                        chanceSlider("poison_chance", index, config.poisonChance[index],
                                value -> config.poisonChance[index] = value));
            }
        }

        @Override
        void reset(ThirstConfig config, ThirstConfig defaults) {
            config.defaultPurity = defaults.defaultPurity;
            config.rainwaterPurity = defaults.rainwaterPurity;
            config.dripstonePurity = defaults.dripstonePurity;
            config.quenchWhenDebuffed = defaults.quenchWhenDebuffed;
            config.nauseaChance = defaults.nauseaChance.clone();
            config.poisonChance = defaults.poisonChance.clone();
        }
    },

    HUD("hud", true) {
        @Override
        void addOptions(OptionsList list, ThirstConfig config) {
            list.addSmall(
                    slider("thirst_bar_x_offset", config.thirstBarXOffset, -200, 200,
                            value -> config.thirstBarXOffset = value),
                    slider("thirst_bar_y_offset", config.thirstBarYOffset, -200, 200,
                            value -> config.thirstBarYOffset = value));

            // Both settings only show anything alongside AppleSkin, so the section says so when it is
            // missing rather than offering switches that appear to do nothing.
            ClientVanilla.addHeader(list, text("category.appleskin"));
            if (!AppleSkin.isLoaded()) ClientVanilla.addHeader(list, text("appleskin_missing"));
            list.addSmall(
                    cycle("appleskin_quenched_overlay", QuenchedOverlay.values(), config.appleskinQuenchedOverlay,
                            value -> config.appleskinQuenchedOverlay = value),
                    toggle("appleskin_tooltip_droplets", config.appleskinTooltipDroplets,
                            value -> config.appleskinTooltipDroplets = value));
        }

        @Override
        void reset(ThirstConfig config, ThirstConfig defaults) {
            config.thirstBarXOffset = defaults.thirstBarXOffset;
            config.thirstBarYOffset = defaults.thirstBarYOffset;
            config.appleskinQuenchedOverlay = defaults.appleskinQuenchedOverlay;
            config.appleskinTooltipDroplets = defaults.appleskinTooltipDroplets;
        }
    },

    ITEMS("items", false) {
        @Override
        void addOptions(OptionsList list, ThirstConfig config) {
            list.addSmall(
                    toggle("enable_keyword_matching", config.enableKeywordMatching,
                            value -> config.enableKeywordMatching = value));
            ClientVanilla.addFullWidthRow(list, Button.builder(text("open_file"),
                            button -> Util.getPlatform().openPath(ThirstConfig.path()))
                    .build());
        }

        @Override
        void reset(ThirstConfig config, ThirstConfig defaults) {
            // The item maps are edited in the file; resetting them from a button would lose a whole
            // modpack's values in one click.
            config.enableKeywordMatching = defaults.enableKeywordMatching;
        }
    };

    private final String key;
    private final boolean preview;

    ConfigCategory(String key, boolean preview) {
        this.key = key;
        this.preview = preview;
    }

    /** The lang key suffix: {@code category.<key>} names it and {@code category.<key>.tooltip} describes it. */
    String key() {
        return key;
    }

    boolean hasPreview() {
        return preview;
    }

    abstract void addOptions(OptionsList list, ThirstConfig config);

    /** Copies this page's values from {@code defaults}, a freshly constructed config, into {@code config}. */
    abstract void reset(ThirstConfig config, ThirstConfig defaults);
}
