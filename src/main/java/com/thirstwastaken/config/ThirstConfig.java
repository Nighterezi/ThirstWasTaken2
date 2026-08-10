package com.thirstwastaken.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thirstwastaken.ThirstWasTaken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Loader-independent replacement for the original Forge config specs.
 *
 * <p>The instance is swapped wholesale on {@link #load()}; {@link #generation()} increments on every
 * swap so derived caches (compiled patterns, per-item lookups) can invalidate themselves cheaply.
 */
public final class ThirstConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("thirstwastaken.json");
    private static volatile ThirstConfig INSTANCE;
    private static volatile int generation;

    // ---- thirst depletion -------------------------------------------------
    public double thirstDepletionModifier = 1.2;
    public boolean thirstDepletionInPeaceful = false;
    public double netherThirstDepletionModifier = 3.0;
    public int fireResistanceDehydrationPercent = 50;
    /** Mirrors the original DEPLETES_WHEN_NAUSEA: nausea adds extra exhaustion while active. */
    public boolean depletesWhenNauseous = true;
    public boolean preventSprintingWhenThirsty = true;
    public boolean canDrinkRain = true;
    public boolean canDrinkByHand = false;
    public boolean drinkByHandNeedsBothHandsEmpty = false;
    public int handDrinkingHydration = 3;
    public int handDrinkingQuenched = 2;
    public boolean extraHydrationConvertsToQuenched = true;
    public boolean dehydrationHaltsHealthRegen = true;

    // ---- HUD --------------------------------------------------------------
    public int thirstBarXOffset = 0;
    public int thirstBarYOffset = 0;
    /**
     * Draws the dithered "exhaustion" strip behind the thirst bar. The original only drew it when
     * AppleSkin was installed and its own SHOW_FOOD_EXHAUSTION_UNDERLAY option was enabled, so it is
     * off by default here.
     */
    public boolean showExhaustionUnderlay = false;
    /** Draws the lighter quenched (saturation-style) overlay on top of the droplets. */
    public boolean showQuenchedOverlay = true;

    // ---- water purity -----------------------------------------------------
    public int mountainsY = 100;
    public int cavesY = 48;
    public int runningWaterPurification = 1;
    public int defaultPurity = 2;
    public boolean quenchWhenDebuffed = true;
    public int[] nauseaChance = {100, 50, 5, 0};
    public int[] poisonChance = {30, 10, 0, 0};

    // ---- item values ------------------------------------------------------
    public boolean enableKeywordMatching = false;
    public String keywordBlacklist = "dried|candied|leaf|leaves|gummy|crate|jam|sauce|bucket|seed|cookie|pie|bush|sapling|bean|curry|cake|candy";
    public String drinkKeywords = "drink|juice|tea|soda|coffee|wine|beer|cider|yogurt|milkshake|smoothie";
    public String soupKeywords = "soup|stew|porridge";
    public String fruitKeywords = "fruit|berry|berries|grape|orange|peach|pear|coconut|lemon|melon|cherry|apple";
    public int[] keywordDrinkValue = {10, 14};
    public int[] keywordSoupValue = {4, 5};
    public int[] keywordFruitValue = {2, 3};
    public Set<String> itemBlacklist = new LinkedHashSet<>();
    public Map<String, int[]> drinks = defaultDrinks();
    public Map<String, int[]> foods = defaultFoods();

    private transient Pattern keywordBlacklistPattern;
    private transient Pattern drinkKeywordPattern;
    private transient Pattern soupKeywordPattern;
    private transient Pattern fruitKeywordPattern;

    public static ThirstConfig get() {
        ThirstConfig config = INSTANCE;
        return config != null ? config : load();
    }

    /** Bumped whenever {@link #load()} replaces the active instance. */
    public static int generation() {
        return generation;
    }

    public static synchronized ThirstConfig load() {
        ThirstConfig loaded = null;
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                loaded = GSON.fromJson(reader, ThirstConfig.class);
            } catch (Exception exception) {
                ThirstWasTaken.LOGGER.error("Could not read {}", PATH, exception);
            }
        }
        ThirstConfig config = loaded == null ? new ThirstConfig() : loaded;
        config.sanitize();
        INSTANCE = config;
        generation++;
        save();
        return config;
    }

    public static synchronized void save() {
        ThirstConfig config = INSTANCE;
        if (config == null) return;
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            ThirstWasTaken.LOGGER.error("Could not write {}", PATH, exception);
        }
    }

    /** Re-validates and persists after an in-place edit (used by the config screen). */
    public static synchronized void commit() {
        ThirstConfig config = INSTANCE;
        if (config == null) return;
        config.sanitize();
        generation++;
        save();
    }

    public Pattern keywordBlacklistPattern() { return keywordBlacklistPattern; }
    public Pattern drinkKeywordPattern() { return drinkKeywordPattern; }
    public Pattern soupKeywordPattern() { return soupKeywordPattern; }
    public Pattern fruitKeywordPattern() { return fruitKeywordPattern; }

    private void sanitize() {
        if (drinks == null) drinks = defaultDrinks();
        if (foods == null) foods = defaultFoods();
        if (itemBlacklist == null) itemBlacklist = new LinkedHashSet<>();
        if (nauseaChance == null || nauseaChance.length != 4) nauseaChance = new int[]{100, 50, 5, 0};
        if (poisonChance == null || poisonChance.length != 4) poisonChance = new int[]{30, 10, 0, 0};
        if (keywordDrinkValue == null || keywordDrinkValue.length != 2) keywordDrinkValue = new int[]{10, 14};
        if (keywordSoupValue == null || keywordSoupValue.length != 2) keywordSoupValue = new int[]{4, 5};
        if (keywordFruitValue == null || keywordFruitValue.length != 2) keywordFruitValue = new int[]{2, 3};
        for (int i = 0; i < 4; i++) {
            nauseaChance[i] = clamp(nauseaChance[i], 0, 100);
            poisonChance[i] = clamp(poisonChance[i], 0, 100);
        }
        defaultPurity = clamp(defaultPurity, 0, 3);
        fireResistanceDehydrationPercent = clamp(fireResistanceDehydrationPercent, 0, 100);
        runningWaterPurification = clamp(runningWaterPurification, 0, 3);
        handDrinkingHydration = clamp(handDrinkingHydration, 0, 20);
        handDrinkingQuenched = clamp(handDrinkingQuenched, 0, 20);
        thirstBarXOffset = clamp(thirstBarXOffset, -200, 200);
        thirstBarYOffset = clamp(thirstBarYOffset, -200, 200);
        thirstDepletionModifier = clamp(thirstDepletionModifier, 0.0, 10.0);
        netherThirstDepletionModifier = clamp(netherThirstDepletionModifier, 0.0, 10.0);

        keywordBlacklistPattern = compile(keywordBlacklist);
        drinkKeywordPattern = compile(drinkKeywords);
        soupKeywordPattern = compile(soupKeywords);
        fruitKeywordPattern = compile(fruitKeywords);
    }

    private static Pattern compile(String pattern) {
        if (pattern == null || pattern.isBlank()) return null;
        try {
            return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } catch (Exception exception) {
            ThirstWasTaken.LOGGER.error("Invalid keyword pattern '{}', ignoring it", pattern, exception);
            return null;
        }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    private static Map<String, int[]> defaultDrinks() {
        Map<String, int[]> values = new LinkedHashMap<>();
        put(values, 6, 8, "minecraft:potion");
        put(values, 4, 5, "thirstwastaken:terracotta_water_bowl");
        put(values, 10, 14, "farmersrespite:green_tea", "farmersrespite:yellow_tea", "farmersrespite:black_tea");
        put(values, 12, 22, "farmersrespite:rose_hip_tea", "farmersrespite:dandelion_tea", "create:builders_tea");
        put(values, 6, 11, "farmersrespite:coffee");
        put(values, 8, 13, "farmersdelight:apple_cider", "farmersdelight:melon_juice");
        put(values, 10, 14, "brewinandchewin:beer", "brewinandchewin:vodka", "brewinandchewin:rice_wine", "brewinandchewin:mead", "brewinandchewin:egg_grog", "brewinandchewin:glittering_grenadine");
        put(values, 12, 22, "brewinandchewin:bloody_mary", "brewinandchewin:salty_folly", "brewinandchewin:pale_jane", "brewinandchewin:saccharine_rum", "brewinandchewin:strongroot_ale", "brewinandchewin:dread_nog");
        put(values, 14, 22, "brewinandchewin:kombucha", "brewinandchewin:red_rum", "brewinandchewin:steel_toe_stout");
        put(values, 8, 13, "collectorsreap:berry_limeade", "collectorsreap:limeade", "collectorsreap:pink_limeade");
        put(values, 10, 14, "collectorsreap:pomegranate_black_tea", "collectorsreap:lime_green_tea");
        put(values, 6, 8, "toughasnails:dirty_water_bottle");
        put(values, 8, 10, "toughasnails:purified_water_bottle", "toughasnails:dirty_water_canteen");
        put(values, 9, 11, "toughasnails:water_canteen");
        put(values, 10, 12, "toughasnails:purified_water_canteen");
        put(values, 8, 13, "toughasnails:melon_juice", "toughasnails:apple_juice", "toughasnails:cactus_juice", "toughasnails:carrot_juice", "toughasnails:glow_berry_juice", "toughasnails:chorus_fruit_juice", "toughasnails:suspicious_water_cup", "toughasnails:pumpkin_juice", "toughasnails:sweet_berry_juice");
        return values;
    }

    private static Map<String, int[]> defaultFoods() {
        Map<String, int[]> values = new LinkedHashMap<>();
        put(values, 2, 3, "minecraft:apple", "minecraft:golden_apple", "minecraft:enchanted_golden_apple", "minecraft:mushroom_stew", "minecraft:rabbit_stew");
        put(values, 4, 5, "minecraft:melon_slice");
        put(values, 1, 2, "minecraft:carrot", "minecraft:beetroot", "minecraft:sweet_berries", "minecraft:glow_berries", "minecraft:golden_carrot");
        put(values, 5, 7, "minecraft:beetroot_soup");
        put(values, 2, 1, "farmersdelight:pumpkin_slice");
        put(values, 1, 2, "farmersdelight:cabbage_leaf", "collectorsreap:lime_slice");
        put(values, 7, 9, "farmersdelight:melon_popsicle", "collectorsreap:lime_popsicle");
        put(values, 6, 8, "farmersdelight:fruit_salad", "collectorsreap:portobello_rice_soup");
        put(values, 4, 5, "farmersdelight:tomato_sauce", "farmersdelight:mixed_salad", "farmersdelight:beef_stew", "farmersdelight:chicken_soup", "farmersdelight:vegetable_soup", "farmersdelight:fish_stew", "farmersdelight:pumpkin_soup", "farmersdelight:baked_cod_stew", "farmersdelight:noodle_soup");
        put(values, 2, 3, "collectorsreap:lime");
        return values;
    }

    private static void put(Map<String, int[]> values, int hydration, int quenched, String... ids) {
        for (String id : ids) values.put(id, new int[]{hydration, quenched});
    }
}
