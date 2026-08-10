package com.thirstwastaken.api;

import com.thirstwastaken.config.ThirstConfig;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.regex.Pattern;

/** Public, loader-neutral item hydration API. */
public final class ThirstApi {
    private ThirstApi() { }

    public static int[] hydration(ItemStack stack) {
        String id = stack.getItem().builtInRegistryHolder().key().identifier().toString().toLowerCase(Locale.ROOT);
        ThirstConfig config = ThirstConfig.get();
        if (config.itemBlacklist.contains(id)) return null;
        int[] value = config.drinks.get(id);
        if (value == null) value = config.foods.get(id);
        if (value != null) return value;
        if (!config.enableKeywordMatching) return null;
        String path = id.substring(id.indexOf(':') + 1);
        if (Pattern.compile(config.keywordBlacklist, Pattern.CASE_INSENSITIVE).matcher(path).find()) return null;
        if (Pattern.compile(config.drinkKeywords, Pattern.CASE_INSENSITIVE).matcher(path).find()) return new int[]{10, 14};
        if (Pattern.compile(config.soupKeywords, Pattern.CASE_INSENSITIVE).matcher(path).find()) return new int[]{4, 5};
        if (Pattern.compile(config.fruitKeywords, Pattern.CASE_INSENSITIVE).matcher(path).find()) return new int[]{2, 3};
        return null;
    }
}
