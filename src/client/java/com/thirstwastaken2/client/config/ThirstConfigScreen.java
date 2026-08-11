package com.thirstwastaken2.client.config;

import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Vanilla-styled editor for {@code config/thirstwastaken2.json}.
 *
 * <p>Only scalar settings are exposed here. The per-item hydration maps and keyword patterns stay in
 * the JSON file, which the footer button opens directly.
 */
public final class ThirstConfigScreen extends OptionsSubScreen {
    /** Doubles are edited as integer percentages so they can use the vanilla slider widget. */
    private static final int PERCENT = 100;

    public ThirstConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.translatable("thirstwastaken2.config.title"));
    }

    @Override
    protected void addOptions() {
        ThirstConfig config = ThirstConfig.get();

        // Everything except the HUD section is server-authoritative, so editing it on a client that
        // is connected to a remote server has no effect there.
        list.addHeader(Component.translatable("thirstwastaken2.config.note"));
        list.addHeader(Component.translatable("thirstwastaken2.config.category.depletion"));
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

        list.addHeader(Component.translatable("thirstwastaken2.config.category.drinking"));
        list.addSmall(
                toggle("can_drink_rain", config.canDrinkRain, value -> config.canDrinkRain = value),
                toggle("can_drink_by_hand", config.canDrinkByHand, value -> config.canDrinkByHand = value),
                toggle("drink_by_hand_needs_both_hands_empty", config.drinkByHandNeedsBothHandsEmpty,
                        value -> config.drinkByHandNeedsBothHandsEmpty = value),
                toggle("extra_hydration_converts_to_quenched", config.extraHydrationConvertsToQuenched,
                        value -> config.extraHydrationConvertsToQuenched = value),
                slider("hand_drinking_hydration", config.handDrinkingHydration, 0, 20,
                        value -> config.handDrinkingHydration = value),
                slider("hand_drinking_quenched", config.handDrinkingQuenched, 0, 20,
                        value -> config.handDrinkingQuenched = value));

        list.addHeader(Component.translatable("thirstwastaken2.config.category.purity"));
        list.addSmall(
                slider("default_purity", config.defaultPurity, 0, 3, value -> config.defaultPurity = value),
                slider("running_water_purification", config.runningWaterPurification, 0, 3,
                        value -> config.runningWaterPurification = value),
                slider("mountains_y", config.mountainsY, -64, 320, value -> config.mountainsY = value),
                slider("caves_y", config.cavesY, -64, 320, value -> config.cavesY = value),
                toggle("quench_when_debuffed", config.quenchWhenDebuffed,
                        value -> config.quenchWhenDebuffed = value));

        list.addHeader(Component.translatable("thirstwastaken2.config.category.purity_chances"));
        for (int purity = 0; purity < 4; purity++) {
            int index = purity;
            list.addSmall(
                    chanceSlider("nausea_chance", index, config.nauseaChance[index],
                            value -> config.nauseaChance[index] = value),
                    chanceSlider("poison_chance", index, config.poisonChance[index],
                            value -> config.poisonChance[index] = value));
        }

        list.addHeader(Component.translatable("thirstwastaken2.config.category.hud"));
        list.addSmall(
                slider("thirst_bar_x_offset", config.thirstBarXOffset, -200, 200,
                        value -> config.thirstBarXOffset = value),
                slider("thirst_bar_y_offset", config.thirstBarYOffset, -200, 200,
                        value -> config.thirstBarYOffset = value));

        list.addHeader(Component.translatable("thirstwastaken2.config.category.items"));
        list.addSmall(
                toggle("enable_keyword_matching", config.enableKeywordMatching,
                        value -> config.enableKeywordMatching = value));
        list.addBig(Button.builder(Component.translatable("thirstwastaken2.config.open_file"),
                        button -> Util.getPlatform().openPath(
                                FabricLoader.getInstance().getConfigDir().resolve("thirstwastaken2.json")))
                .build());
    }

    @Override
    public void onClose() {
        // Values are written straight into the live config, so this only re-validates and persists.
        ThirstConfig.commit();
        super.onClose();
    }

    private static OptionInstance<Boolean> toggle(String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(translationKey(key),
                OptionInstance.cachedConstantTooltip(Component.translatable(translationKey(key) + ".tooltip")),
                initial, setter::accept);
    }

    private static OptionInstance<Integer> slider(String key, int initial, int min, int max, IntConsumer setter) {
        return new OptionInstance<>(translationKey(key),
                OptionInstance.cachedConstantTooltip(Component.translatable(translationKey(key) + ".tooltip")),
                (caption, value) -> Options.genericValueLabel(caption, value),
                new OptionInstance.IntRange(min, max), initial, setter::accept);
    }

    private static OptionInstance<Integer> chanceSlider(String key, int purity, int initial, IntConsumer setter) {
        Component label = Component.translatable(translationKey(key),
                Component.translatable("thirst.purity." + purityName(purity)));
        return new OptionInstance<>(translationKey(key),
                OptionInstance.cachedConstantTooltip(Component.translatable(translationKey(key) + ".tooltip")),
                (caption, value) -> Component.translatable("options.generic_value", label, value + "%"),
                new OptionInstance.IntRange(0, 100), initial, setter::accept);
    }

    private static OptionInstance<Integer> percentSlider(String key, double initial, int min, int max,
                                                         DoubleConsumer setter) {
        return new OptionInstance<>(translationKey(key),
                OptionInstance.cachedConstantTooltip(Component.translatable(translationKey(key) + ".tooltip")),
                (caption, value) -> Component.translatable("options.generic_value", caption, value + "%"),
                new OptionInstance.IntRange(min, max), (int) Math.round(initial * PERCENT),
                value -> setter.accept(value / (double) PERCENT));
    }

    private static String purityName(int purity) {
        return switch (purity) {
            case 0 -> "dirty";
            case 1 -> "slightly_dirty";
            case 2 -> "acceptable";
            default -> "purified";
        };
    }

    private static String translationKey(String key) {
        return "thirstwastaken2.config." + key;
    }
}
