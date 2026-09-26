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
    public boolean preventSprintingWhenThirsty = true;
    public boolean canDrinkByHand = true;
    public boolean dehydrationHaltsHealthRegen = true;
    /**
     * With Cold Sweat installed, the drain follows the temperature it measures around the player in
     * place of the biome's. Does nothing without it.
     */
    public boolean coldSweatClimate = true;

    // ---- AppleSkin (client side, and only while AppleSkin is installed) ----
    public QuenchedOverlay appleskinQuenchedOverlay = QuenchedOverlay.DIAMOND;
    public boolean appleskinTooltipDroplets = true;

    // ---- water purity -----------------------------------------------------
    public int defaultPurity = 2;

    // ---- water sickness ---------------------------------------------------
    // This replaced quenchWhenDebuffed, nauseaChance, poisonChance and nauseaSeconds in the sickness
    // rework, and the chances are now fixed in SicknessTable. The config has no migration: dropped keys
    // are ignored, and every fresh drink now quenches.
    public SicknessPreset sicknessPreset = SicknessPreset.REALISTIC;

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
        // And the canteen and flask, which came later: a drink from them is the waterskin's drink.
        drinks.putIfAbsent("thirstwastaken2:copper_canteen", new int[]{4, 5});
        drinks.putIfAbsent("thirstwastaken2:iron_flask", new int[]{4, 5});
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
        // And for Kaleidoscope Cookery, added after both.
        kaleidoscopeCookeryDrinks(drinks);
        kaleidoscopeCookeryFoods(foods);
        // And for Brewin' and Chewin', added after that.
        brewinAndChewinDrinks(drinks);
        brewinAndChewinFoods(foods);
        // And for Cold Sweat's waterskin, added after that.
        coldSweatDrinks(drinks);
        // And for Cultural Delights, added after that.
        culturalDelightsDrinks(drinks);
        culturalDelightsFoods(foods);
        // And for Fruits Delight, added after that.
        fruitsDelightDrinks(drinks);
        fruitsDelightFoods(foods);
        if (itemBlacklist == null) itemBlacklist = new LinkedHashSet<>();
        if (sicknessPreset == null) sicknessPreset = SicknessPreset.REALISTIC;
        if (drinkTagValue == null || drinkTagValue.length != 2) drinkTagValue = new int[]{6, 8};
        if (keywordDrinkValue == null || keywordDrinkValue.length != 2) keywordDrinkValue = new int[]{10, 14};
        if (keywordSoupValue == null || keywordSoupValue.length != 2) keywordSoupValue = new int[]{4, 5};
        if (keywordFruitValue == null || keywordFruitValue.length != 2) keywordFruitValue = new int[]{2, 3};
        defaultPurity = clamp(defaultPurity, 0, 3);
        // Gson reads a name it does not know, including a hand typo, as null.
        if (appleskinQuenchedOverlay == null) appleskinQuenchedOverlay = QuenchedOverlay.DIAMOND;
        thirstDepletionModifier = clamp(thirstDepletionModifier, 0.0, 10.0);

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
        put(values, 4, 5, "thirstwastaken2:waterskin", "thirstwastaken2:copper_canteen", "thirstwastaken2:iron_flask");
        put(values, 8, 13, "farmersdelight:apple_cider", "farmersdelight:melon_juice", "farmersdelight:hot_cocoa");
        put(values, 6, 8, "farmersdelight:milk_bottle");
        kaleidoscopeCookeryDrinks(values);
        brewinAndChewinDrinks(values);
        coldSweatDrinks(values);
        culturalDelightsDrinks(values);
        fruitsDelightDrinks(values);
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
        kaleidoscopeCookeryFoods(values);
        brewinAndChewinFoods(values);
        culturalDelightsFoods(values);
        fruitsDelightFoods(values);
        return values;
    }

    /**
     * Kaleidoscope Cookery's teas and soups, by id alone: the official build and Refabricated share one
     * mod id, so these reach every node, one with no integration included, and an id a build lacks is
     * never matched. Listed because keyword matching is off by default, and when on, {@code tea} matches
     * {@code tea_egg}, a food, and misses {@code tieguanyin} and the other named teas.
     *
     * <p>A teacup is a cup of the four a bucket of water brews, a little above a bottle of water since
     * it costs a tea bag and heat. Tea is brewed from boiled water, so it is safe whatever went into the
     * teapot. Only what is eaten out of a bowl in hand is here: the pot soups are eaten off a placed
     * block, which is solid food and restores no thirst. Merged with {@code putIfAbsent}, so the same
     * call fills a fresh config and brings an older file up to date without overwriting a player's edit.
     */
    private static void kaleidoscopeCookeryDrinks(Map<String, int[]> drinks) {
        putMissing(drinks, 6, 9, "kaleidoscope_cookery:barley_tea", "kaleidoscope_cookery:tieguanyin",
                "kaleidoscope_cookery:biluochun", "kaleidoscope_cookery:oolong", "kaleidoscope_cookery:sakura_fubuki",
                "kaleidoscope_cookery:flower_tea");
        putMissing(drinks, 6, 10, "kaleidoscope_cookery:butter_tea");
        // What a teapot brews from the wrong recipe.
        putMissing(drinks, 3, 3, "kaleidoscope_cookery:mystery_tea");
        putMissing(drinks, 8, 12, "kaleidoscope_cookery:clay_pot_milk_tea");
    }

    /** Kaleidoscope Cookery's soups and noodles; see {@link #kaleidoscopeCookeryDrinks}. */
    private static void kaleidoscopeCookeryFoods(Map<String, int[]> foods) {
        putMissing(foods, 5, 7, "kaleidoscope_cookery:pork_bone_soup");
        putMissing(foods, 4, 5, "kaleidoscope_cookery:seafood_miso_soup", "kaleidoscope_cookery:fearsome_thick_soup",
                "kaleidoscope_cookery:lamb_and_radish_soup", "kaleidoscope_cookery:wild_mushroom_rabbit_soup",
                "kaleidoscope_cookery:pufferfish_soup", "kaleidoscope_cookery:borscht",
                "kaleidoscope_cookery:beef_meatball_soup", "kaleidoscope_cookery:chicken_and_mushroom_stew",
                "kaleidoscope_cookery:laba_congee", "kaleidoscope_cookery:donkey_soup",
                "kaleidoscope_cookery:tomato_beef_brisket_soup");
        putMissing(foods, 3, 4, "kaleidoscope_cookery:beef_noodle", "kaleidoscope_cookery:hui_noodle",
                "kaleidoscope_cookery:udon_noodle");
        putMissing(foods, 2, 3, "kaleidoscope_cookery:tomato");
    }

    /**
     * Brewin' and Chewin's drinks, by id alone, as Kaleidoscope Cookery's are: they reach every node, one
     * with no integration included, and match nothing where the mod is absent. None of them is tagged
     * {@code c:drinks}, so without these they restore nothing.
     *
     * <p>A tankard or a bottle is one drink, a bottle of water's size. The stronger it is, the less it
     * restores: light brews a little under water, strong ones a third of it, and spirits nothing, so they
     * are left out, as are Salty Folly and Withering Dross, which no one drinks for their water. Brewed
     * drinks are safe whatever water went into the keg, as tea is.
     */
    private static void brewinAndChewinDrinks(Map<String, int[]> drinks) {
        putMissing(drinks, 6, 8, "brewinandchewin:kombucha");
        putMissing(drinks, 5, 6, "brewinandchewin:beer", "brewinandchewin:mead", "brewinandchewin:egg_grog",
                "brewinandchewin:glittering_grenadine");
        putMissing(drinks, 4, 5, "brewinandchewin:bloody_mary");
        putMissing(drinks, 3, 4, "brewinandchewin:red_wine", "brewinandchewin:white_wine",
                "brewinandchewin:currant_wine", "brewinandchewin:verruca_wine", "brewinandchewin:twisted_wine",
                "brewinandchewin:rice_wine", "brewinandchewin:old_wine");
        putMissing(drinks, 3, 3, "brewinandchewin:pale_jane", "brewinandchewin:strongroot_ale",
                "brewinandchewin:dread_nog");
        putMissing(drinks, 2, 2, "brewinandchewin:saccharine_rum", "brewinandchewin:steel_toe_stout",
                "brewinandchewin:red_rum");
    }

    /** Brewin' and Chewin's soups and porridges eaten out of a bowl in hand; see {@link #brewinAndChewinDrinks}. */
    private static void brewinAndChewinFoods(Map<String, int[]> foods) {
        putMissing(foods, 4, 5, "brewinandchewin:creamy_onion_soup");
        putMissing(foods, 2, 3, "brewinandchewin:fiery_fondue", "brewinandchewin:grits",
                "brewinandchewin:chopped_liver");
    }

    /**
     * Cold Sweat's filled waterskin, by id alone like the other mods' drinks. It holds 250 mB, a bottle,
     * and by default one sip empties it, so a sip is a bottle of water's worth. A server that gives it
     * more sips in Cold Sweat's config gets a bottle's worth from each.
     */
    private static void coldSweatDrinks(Map<String, int[]> drinks) {
        putMissing(drinks, 6, 8, "cold_sweat:filled_waterskin");
    }

    /**
     * Cultural Delights' drinks, by id alone like the other mods': brewed in its vat since 0.18, a glass
     * bottle each, one drink per bottle. On Brewin' and Chewin's scale: the soft ones about a bottle of
     * water, light brews a little under, wine a third, a liqueur less. The spirits and the two that are
     * not drinks at all are listed as zero rather than left out: the mod tags its alcohol
     * {@code c:drinks/alcohol}, and a zero keeps the {@code c:drinks} tag value off them should a pack
     * fold that tag in. Brewed drinks are safe whatever water went into the vat, as tea is; sea water
     * brews nothing (see {@code src/main/culturaldelights}).
     */
    private static void culturalDelightsDrinks(Map<String, int[]> drinks) {
        putMissing(drinks, 6, 8, "culturaldelights:cola");
        putMissing(drinks, 5, 6, "culturaldelights:ginger_beer", "culturaldelights:butterbeer",
                "culturaldelights:beer", "culturaldelights:mead", "culturaldelights:apple_cider");
        putMissing(drinks, 4, 5, "culturaldelights:bloody_mary", "culturaldelights:mojito",
                "culturaldelights:margarita");
        putMissing(drinks, 3, 4, "culturaldelights:wine", "culturaldelights:glow_wine");
        putMissing(drinks, 2, 2, "culturaldelights:lemon_liqueur");
        putMissing(drinks, 0, 0, "culturaldelights:tequila", "culturaldelights:gin", "culturaldelights:brandy",
                "culturaldelights:vodka", "culturaldelights:whiskey", "culturaldelights:rum",
                "culturaldelights:acid", "culturaldelights:vinegar");
    }

    /**
     * Cultural Delights' watery foods; see {@link #culturalDelightsDrinks}. A cucumber is most of a melon
     * slice, the salad and the soft corn and eggplant dishes are Farmer's Delight's. Pickles are salty and
     * the rest is dry, so neither is here.
     */
    private static void culturalDelightsFoods(Map<String, int[]> foods) {
        putMissing(foods, 3, 4, "culturaldelights:cucumber");
        putMissing(foods, 1, 2, "culturaldelights:cut_cucumber");
        putMissing(foods, 4, 5, "culturaldelights:hearty_salad");
        putMissing(foods, 2, 3, "culturaldelights:creamed_corn", "culturaldelights:poached_eggplants");
    }

    /**
     * Fruits Delight's juices and teas, by id alone like the other mods'. The mod ships thirst values
     * of its own, but only for the original Thirst Was Taken, whose mod id is not ours, so without these
     * its drinks restore nothing. The drinks keep its values, which are already Farmer's Delight's juice
     * value here; a juice is safe whatever water went into it, as tea is.
     */
    private static void fruitsDelightDrinks(Map<String, int[]> drinks) {
        putMissing(drinks, 8, 13, "fruitsdelight:hamimelon_juice", "fruitsdelight:kiwi_juice",
                "fruitsdelight:orange_juice", "fruitsdelight:lemon_juice", "fruitsdelight:pear_juice",
                "fruitsdelight:hawberry_tea", "fruitsdelight:mango_tea", "fruitsdelight:peach_tea",
                "fruitsdelight:lychee_cherry_tea", "fruitsdelight:mangosteen_tea", "fruitsdelight:bayberry_soup");
        putMissing(drinks, 8, 12, "fruitsdelight:mango_milkshake");
        putMissing(drinks, 5, 6, "fruitsdelight:bellini_cocktail");
    }

    /**
     * Fruits Delight's watery foods; see {@link #fruitsDelightDrinks}. Its own values for food are two
     * to three times ours, so these follow the Farmer's Delight food each is closest to: a hamimelon
     * slice is a melon slice, a fruit an apple, a popsicle the melon popsicle, a stew a stew. Jam,
     * cookies, pies and the dry foods are left out.
     */
    private static void fruitsDelightFoods(Map<String, int[]> foods) {
        putMissing(foods, 8, 10, "fruitsdelight:hamimelon_shaved_ice");
        putMissing(foods, 7, 9, "fruitsdelight:hamimelon_popsicle", "fruitsdelight:kiwi_popsicle");
        putMissing(foods, 3, 4, "fruitsdelight:apple_jello", "fruitsdelight:bayberry_jello",
                "fruitsdelight:blueberry_jello", "fruitsdelight:chorus_jello", "fruitsdelight:cranberry_jello",
                "fruitsdelight:durian_jello", "fruitsdelight:fig_jello", "fruitsdelight:glowberry_jello",
                "fruitsdelight:hamimelon_jello", "fruitsdelight:hawberry_jello", "fruitsdelight:kiwi_jello",
                "fruitsdelight:lemon_jello", "fruitsdelight:lychee_jello", "fruitsdelight:mango_jello",
                "fruitsdelight:mangosteen_jello", "fruitsdelight:melon_jello", "fruitsdelight:orange_jello",
                "fruitsdelight:peach_jello", "fruitsdelight:pear_jello", "fruitsdelight:persimmon_jello",
                "fruitsdelight:pineapple_jello", "fruitsdelight:sweetberry_jello");
        putMissing(foods, 4, 5, "fruitsdelight:hamimelon_slice");
        putMissing(foods, 2, 3, "fruitsdelight:orange", "fruitsdelight:lychee", "fruitsdelight:pineapple_slice",
                "fruitsdelight:kiwi", "fruitsdelight:peach", "fruitsdelight:mango", "fruitsdelight:pear");
        putMissing(foods, 1, 2, "fruitsdelight:orange_slice", "fruitsdelight:lemon_slice", "fruitsdelight:baked_pear");
        putMissing(foods, 6, 8, "fruitsdelight:pear_with_rock_sugar");
        putMissing(foods, 4, 5, "fruitsdelight:fig_chicken_stew", "fruitsdelight:mango_salad");
        putMissing(foods, 2, 3, "fruitsdelight:blueberry_custard");
    }

    private static void put(Map<String, int[]> values, int thirst, int quenched, String... ids) {
        for (String id : ids) values.put(id, new int[]{thirst, quenched});
    }

    private static void putMissing(Map<String, int[]> values, int thirst, int quenched, String... ids) {
        for (String id : ids) values.putIfAbsent(id, new int[]{thirst, quenched});
    }
}
