package com.thirstwastaken2.api;

import com.thirstwastaken2.config.ThirstConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Public, loader-neutral item hydration API.
 *
 * <p>Resolution is per {@link Item} and is memoised, because the tooltip renderer asks for it once
 * per frame per hovered stack. The cache is dropped whenever the config generation changes.
 */
public final class ThirstApi {
    private static final int[] NONE = new int[0];
    private static final Map<Item, int[]> CACHE = new ConcurrentHashMap<>();
    private static volatile int cachedGeneration = -1;

    private ThirstApi() { }

    /** @return {hydration, quenched}, or {@code null} when the item restores no thirst. */
    public static int[] hydration(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return hydration(stack.getItem());
    }

    public static int[] hydration(Item item) {
        int generation = ThirstConfig.generation();
        if (generation != cachedGeneration) {
            CACHE.clear();
            cachedGeneration = generation;
        }
        int[] cached = CACHE.computeIfAbsent(item, ThirstApi::resolve);
        return cached == NONE ? null : cached;
    }

    public static boolean restoresThirst(ItemStack stack) {
        return hydration(stack) != null;
    }

    private static int[] resolve(Item item) {
        Identifier identifier = item.builtInRegistryHolder().key().identifier();
        String id = identifier.toString();
        ThirstConfig config = ThirstConfig.get();
        if (config.itemBlacklist.contains(id)) return NONE;

        int[] value = config.drinks.get(id);
        if (value == null) value = config.foods.get(id);
        if (value != null) return value;
        if (!config.enableKeywordMatching) return NONE;

        String path = identifier.getPath();
        if (matches(config.keywordBlacklistPattern(), path)) return NONE;
        if (matches(config.drinkKeywordPattern(), path)) return config.keywordDrinkValue;
        if (matches(config.soupKeywordPattern(), path)) return config.keywordSoupValue;
        if (matches(config.fruitKeywordPattern(), path)) return config.keywordFruitValue;
        return NONE;
    }

    private static boolean matches(Pattern pattern, String path) {
        return pattern != null && pattern.matcher(path).find();
    }
}
