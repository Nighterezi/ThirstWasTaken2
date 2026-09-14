package com.thirstwastaken2.api;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.platform.Vanilla;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Public, loader-neutral item thirst API.
 *
 * <p>Resolution is per {@link Item} and is memoised, because the tooltip renderer asks for it once
 * per frame per hovered stack. The cache is dropped whenever the config generation changes.
 */
public final class ThirstApi {
    private static final int[] NONE = new int[0];
    private static final TagKey<Item> DRINKS = conventionTag("drinks");
    private static final TagKey<Item> MAGIC_DRINKS = conventionTag("drinks/magic");
    private static final TagKey<Item> OMINOUS_DRINKS = conventionTag("drinks/ominous");
    private static final Map<Item, int[]> CACHE = new ConcurrentHashMap<>();
    private static volatile int cachedGeneration = -1;

    private ThirstApi() { }

    /**
     * Forgets every resolved item. Tags are bound empty at startup and rebound on every data pack
     * reload and every server join, so a value resolved from a tag is only good until the next load.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /** @return {thirst, quenched}, or {@code null} when the item restores no thirst. */
    public static int[] thirstValues(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(ThirstItems.WATERSKIN) && WaterskinItem.servings(stack) == 0) return null;
        return thirstValues(stack.getItem());
    }

    public static int[] thirstValues(Item item) {
        int generation = ThirstConfig.generation();
        if (generation != cachedGeneration) {
            CACHE.clear();
            cachedGeneration = generation;
        }
        int[] cached = CACHE.get(item);
        if (cached == null) cached = CACHE.computeIfAbsent(item, ThirstApi::resolve);
        return cached == NONE ? null : cached;
    }

    public static boolean restoresThirst(ItemStack stack) {
        return thirstValues(stack) != null;
    }

    private static int[] resolve(Item item) {
        Identifier identifier = Vanilla.itemId(item);
        String id = identifier.toString();
        ThirstConfig config = ThirstConfig.get();
        if (config.itemBlacklist.contains(id)) return NONE;

        int[] value = config.drinks.get(id);
        if (value == null) value = config.foods.get(id);
        if (value != null) return value;
        if (config.enableDrinkTagMatching && isTaggedDrink(item)) return config.drinkTagValue;
        if (!config.enableKeywordMatching) return NONE;

        String path = identifier.getPath();
        if (matches(config.keywordBlacklistPattern(), path)) return NONE;
        if (matches(config.drinkKeywordPattern(), path)) return config.keywordDrinkValue;
        if (matches(config.soupKeywordPattern(), path)) return config.keywordSoupValue;
        if (matches(config.fruitKeywordPattern(), path)) return config.keywordFruitValue;
        return NONE;
    }

    /**
     * Whether the item's own mod calls it a drink, through the {@code c:drinks} convention tag. Magic
     * drinks are left out: the tag counts every potion and the ominous bottle among them, and neither
     * is water.
     */
    private static boolean isTaggedDrink(Item item) {
        ItemStack stack = new ItemStack(item);
        return stack.is(DRINKS) && !stack.is(MAGIC_DRINKS) && !stack.is(OMINOUS_DRINKS);
    }

    private static boolean matches(Pattern pattern, String path) {
        return pattern != null && pattern.matcher(path).find();
    }

    private static TagKey<Item> conventionTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
