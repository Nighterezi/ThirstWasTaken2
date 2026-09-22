package com.thirstwastaken2.client.config;

import com.mojang.serialization.Codec;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

/**
 * Builds the vanilla option widgets the config screens are made of. Every widget writes straight into
 * the live config, and every label and hover text comes from {@code thirstwastaken2.config.<key>} and
 * {@code thirstwastaken2.config.<key>.tooltip}.
 */
final class ConfigOptions {
    /** Doubles are edited as integer percentages so they can use the vanilla slider widget. */
    private static final int PERCENT = 100;

    private ConfigOptions() { }

    static OptionInstance<Boolean> toggle(String key, boolean initial, Consumer<Boolean> setter) {
        return OptionInstance.createBoolean(translationKey(key), tooltip(key), initial, setter::accept);
    }

    static OptionInstance<Integer> slider(String key, int initial, int min, int max, IntConsumer setter) {
        return new OptionInstance<>(translationKey(key), tooltip(key),
                (caption, value) -> Options.genericValueLabel(caption, value),
                new OptionInstance.IntRange(min, max), initial, setter::accept);
    }

    /**
     * A button that steps through {@code values}, labelled by {@code <key>.<value in lower case>}. The
     * button puts the option's name in front of the value itself, so the label is the value alone.
     */
    static <T extends Enum<T>> OptionInstance<T> cycle(String key, T[] values, T initial, Consumer<T> setter) {
        return new OptionInstance<>(translationKey(key), tooltip(key),
                (caption, value) -> Component.translatable(
                        translationKey(key) + "." + value.name().toLowerCase(Locale.ROOT)),
                // The codec is only used by vanilla's options.txt, which this option is never saved to.
                new OptionInstance.Enum<>(List.of(values), Codec.INT.xmap(i -> values[i], Enum::ordinal)),
                initial, setter::accept);
    }

    /** A percentage from 0 to 100, labelled {@code <name>: <value>%}. */
    static OptionInstance<Integer> chanceSlider(String key, int initial, IntConsumer setter) {
        return new OptionInstance<>(translationKey(key), tooltip(key),
                (caption, value) -> Component.translatable("options.generic_value", caption, value + "%"),
                new OptionInstance.IntRange(0, 100), initial, setter::accept);
    }

    /** A percentage from 0 to 100 for water of one grade, labelled with the grade's name. */
    static OptionInstance<Integer> gradePercentSlider(String key, int purity, int initial, IntConsumer setter) {
        Component label = Component.translatable(translationKey(key), gradeName(purity));
        return new OptionInstance<>(translationKey(key), tooltip(key),
                (caption, value) -> Component.translatable("options.generic_value", label, value + "%"),
                new OptionInstance.IntRange(0, 100), initial, setter::accept);
    }

    /** The name the game gives water of {@code purity}, as its tooltip shows it. */
    static Component gradeName(int purity) {
        return Component.translatable("thirst.purity." + purityName(purity));
    }

    static OptionInstance<Integer> percentSlider(String key, double initial, int min, int max, DoubleConsumer setter) {
        return new OptionInstance<>(translationKey(key), tooltip(key),
                (caption, value) -> Component.translatable("options.generic_value", caption, value + "%"),
                new OptionInstance.IntRange(min, max), (int) Math.round(initial * PERCENT),
                value -> setter.accept(value / (double) PERCENT));
    }

    static Component text(String key) {
        return Component.translatable(translationKey(key));
    }

    private static <T> OptionInstance.TooltipSupplier<T> tooltip(String key) {
        return OptionInstance.cachedConstantTooltip(Component.translatable(translationKey(key) + ".tooltip"));
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
