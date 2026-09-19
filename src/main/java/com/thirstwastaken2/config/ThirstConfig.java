package com.thirstwastaken2.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.platform.Loader;

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
    private static final Path PATH = Loader.configDir().resolve("thirstwastaken2.json");
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
    public boolean canDrinkByHand = true;
    public boolean drinkByHandNeedsBothHandsEmpty = false;
    /** Two rather than the original's one, so a drink by hand is worth the click, bad water included. */
    public int handDrinkingThirst = 2;
    public int handDrinkingQuenched = 2;
    public boolean extraThirstConvertsToQuenched = true;
    public boolean dehydrationHaltsHealthRegen = true;

    // ---- HUD --------------------------------------------------------------
    public int thirstBarXOffset = 0;
    public int thirstBarYOffset = 0;

    // ---- AppleSkin (client side, and only while AppleSkin is installed) ----
    public QuenchedOverlay appleskinQuenchedOverlay = QuenchedOverlay.DIAMOND;
    public boolean appleskinTooltipDroplets = true;

    // ---- water purity -----------------------------------------------------
    public int defaultPurity = 2;
    /**
     * Grade a cauldron is given when rain fills it. Chosen here rather than left to
     * {@link #defaultPurity}, so that collecting rain is a decision with a known outcome.
     */
    public int rainwaterPurity = 2;
    /** Grade a cauldron is given when a pointed dripstone drips into it, having filtered it. */
    public int dripstonePurity = 3;
    /**
     * Seconds each serving in a copper hanging pot over a lit campfire takes to boil pure, so a full pot
     * takes three times as long as a bottle. A furnace takes 10 seconds a bucket and raises it two
     * grades; see docs/dev/WATER-PURIFICATION-BALANCE.md for how the numbers were chosen.
     */
    public int copperPotSecondsPerServing = 4;
    /** The same for the iron hanging pot, which is slower: iron carries heat worse than copper. */
    public int ironPotSecondsPerServing = 6;
    public boolean quenchWhenDebuffed = true;
    public int[] nauseaChance = {100, 50, 5, 0};
    public int[] poisonChance = {30, 10, 0, 0};
    /**
     * Seconds of Nausea from water of each grade, Dirty first. The original gave five for every grade,
     * which ends before the screen has finished warping. Worse water lasts longer, and with
     * {@link #depletesWhenNauseous} that is also what bad water costs in thirst, until the planned
     * Upset Stomach (docs/dev/WATER-SICKNESS.md) takes that over.
     */
    public int[] nauseaSeconds = {12, 8, 5, 5};

    // ---- item values ------------------------------------------------------
    /**
     * Items their own mod tags {@code c:drinks} restore {@link #drinkTagValue} when neither list names
     * them. On by default, unlike keyword matching, because the tag is the mod's word rather than a guess.
     */
    public boolean enableDrinkTagMatching = true;
    public int[] drinkTagValue = {6, 8};
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

    /** The config file on disk. */
    public static Path path() {
        return PATH;
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
                ThirstWasTaken2.LOGGER.error("Could not read {}", PATH, exception);
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
            ThirstWasTaken2.LOGGER.error("Could not write {}", PATH, exception);
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

    /** A detached copy of the active config, for a screen that may throw its edits away. */
    public static synchronized ThirstConfig snapshot() {
        ThirstConfig copy = GSON.fromJson(GSON.toJson(get()), ThirstConfig.class);
        copy.sanitize();
        return copy;
    }

    /**
     * Makes a {@link #snapshot()} the active config again, without saving: edits are only written by
     * {@link #commit()}, so the file on disk still holds what the snapshot does.
     */
    public static synchronized void restore(ThirstConfig snapshot) {
        snapshot.sanitize();
        INSTANCE = snapshot;
        generation++;
    }

    public Pattern keywordBlacklistPattern() { return keywordBlacklistPattern; }
    public Pattern drinkKeywordPattern() { return drinkKeywordPattern; }
    public Pattern soupKeywordPattern() { return soupKeywordPattern; }
    public Pattern fruitKeywordPattern() { return fruitKeywordPattern; }

    private void sanitize() {
        if (drinks == null) drinks = defaultDrinks();
        // Existing config files predate the waterskin, so merge its required built-in value once.
        drinks.putIfAbsent("thirstwastaken2:waterskin", new int[]{4, 5});
        // Same for milk and honey, added later still. A player who does not want them can set both
        // values to zero or list the item in itemBlacklist; only a missing key is filled in.
        drinks.putIfAbsent("minecraft:milk_bucket", new int[]{6, 8});
        drinks.putIfAbsent("minecraft:honey_bottle", new int[]{4, 6});
        // And for the Farmer's Delight drinks and meals the first lists missed.
        drinks.putIfAbsent("farmersdelight:milk_bottle", new int[]{6, 8});
        drinks.putIfAbsent("farmersdelight:hot_cocoa", new int[]{8, 13});
        if (foods == null) foods = defaultFoods();
        foods.putIfAbsent("farmersdelight:bone_broth", new int[]{5, 7});
        foods.putIfAbsent("farmersdelight:onion_soup", new int[]{4, 5});
        foods.putIfAbsent("farmersdelight:glow_berry_custard", new int[]{2, 3});
        foods.putIfAbsent("farmersdelight:tomato", new int[]{2, 3});
        if (itemBlacklist == null) itemBlacklist = new LinkedHashSet<>();
        if (nauseaChance == null || nauseaChance.length != 4) nauseaChance = new int[]{100, 50, 5, 0};
        if (poisonChance == null || poisonChance.length != 4) poisonChance = new int[]{30, 10, 0, 0};
        if (nauseaSeconds == null || nauseaSeconds.length != 4) nauseaSeconds = new int[]{12, 8, 5, 5};
        if (drinkTagValue == null || drinkTagValue.length != 2) drinkTagValue = new int[]{6, 8};
        if (keywordDrinkValue == null || keywordDrinkValue.length != 2) keywordDrinkValue = new int[]{10, 14};
        if (keywordSoupValue == null || keywordSoupValue.length != 2) keywordSoupValue = new int[]{4, 5};
        if (keywordFruitValue == null || keywordFruitValue.length != 2) keywordFruitValue = new int[]{2, 3};
        for (int i = 0; i < 4; i++) {
            nauseaChance[i] = clamp(nauseaChance[i], 0, 100);
            poisonChance[i] = clamp(poisonChance[i], 0, 100);
            nauseaSeconds[i] = clamp(nauseaSeconds[i], 1, 60);
        }
        defaultPurity = clamp(defaultPurity, 0, 3);
        rainwaterPurity = clamp(rainwaterPurity, 0, 3);
        dripstonePurity = clamp(dripstonePurity, 0, 3);
        copperPotSecondsPerServing = clamp(copperPotSecondsPerServing, 1, 100);
        ironPotSecondsPerServing = clamp(ironPotSecondsPerServing, 1, 100);
        fireResistanceDehydrationPercent = clamp(fireResistanceDehydrationPercent, 0, 100);
        handDrinkingThirst = clamp(handDrinkingThirst, 0, 20);
        handDrinkingQuenched = clamp(handDrinkingQuenched, 0, 20);
        thirstBarXOffset = clamp(thirstBarXOffset, -200, 200);
        thirstBarYOffset = clamp(thirstBarYOffset, -200, 200);
        // Gson reads a name it does not know, including a hand typo, as null.
        if (appleskinQuenchedOverlay == null) appleskinQuenchedOverlay = QuenchedOverlay.DIAMOND;
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
            ThirstWasTaken2.LOGGER.error("Invalid keyword pattern '{}', ignoring it", pattern, exception);
            return null;
        }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, value)); }

    private static Map<String, int[]> defaultDrinks() {
        Map<String, int[]> values = new LinkedHashMap<>();
        put(values, 6, 8, "minecraft:potion");
        // Milk is as good as a bottle of water and never needs purifying, but it costs a bucket and
        // a cow. Honey is half the drink and lingers a little longer than its size suggests.
        put(values, 6, 8, "minecraft:milk_bucket");
        put(values, 4, 6, "minecraft:honey_bottle");
        put(values, 4, 5, "thirstwastaken2:terracotta_water_bowl");
        put(values, 4, 5, "thirstwastaken2:waterskin");
        put(values, 8, 13, "farmersdelight:apple_cider", "farmersdelight:melon_juice", "farmersdelight:hot_cocoa");
        put(values, 6, 8, "farmersdelight:milk_bottle");
        return values;
    }

    private static Map<String, int[]> defaultFoods() {
        Map<String, int[]> values = new LinkedHashMap<>();
        put(values, 2, 3, "minecraft:apple", "minecraft:golden_apple", "minecraft:enchanted_golden_apple", "minecraft:mushroom_stew", "minecraft:rabbit_stew");
        put(values, 4, 5, "minecraft:melon_slice");
        put(values, 1, 2, "minecraft:carrot", "minecraft:beetroot", "minecraft:sweet_berries", "minecraft:glow_berries", "minecraft:golden_carrot");
        put(values, 5, 7, "minecraft:beetroot_soup");
        put(values, 2, 1, "farmersdelight:pumpkin_slice");
        put(values, 2, 3, "farmersdelight:tomato", "farmersdelight:glow_berry_custard");
        put(values, 5, 7, "farmersdelight:bone_broth");
        put(values, 1, 2, "farmersdelight:cabbage_leaf");
        put(values, 7, 9, "farmersdelight:melon_popsicle");
        put(values, 6, 8, "farmersdelight:fruit_salad");
        put(values, 4, 5, "farmersdelight:tomato_sauce", "farmersdelight:mixed_salad", "farmersdelight:beef_stew", "farmersdelight:chicken_soup", "farmersdelight:vegetable_soup", "farmersdelight:fish_stew", "farmersdelight:pumpkin_soup", "farmersdelight:baked_cod_stew", "farmersdelight:noodle_soup", "farmersdelight:onion_soup");
        return values;
    }

    private static void put(Map<String, int[]> values, int thirst, int quenched, String... ids) {
        for (String id : ids) values.put(id, new int[]{thirst, quenched});
    }
}
