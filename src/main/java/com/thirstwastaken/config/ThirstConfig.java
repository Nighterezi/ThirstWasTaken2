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

/** Loader-independent replacement for the original Forge config specs. */
public final class ThirstConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("thirstwastaken.json");
    private static ThirstConfig INSTANCE;

    public double thirstDepletionModifier = 1.2;
    public boolean thirstDepletionInPeaceful = false;
    public double netherThirstDepletionModifier = 3.0;
    public int fireResistanceDehydrationPercent = 50;
    public boolean depletesWhenNauseous = true;
    public boolean preventSprintingWhenThirsty = true;
    public boolean canDrinkRain = true;
    public boolean canDrinkByHand = false;
    public int handDrinkingHydration = 3;
    public int handDrinkingQuenched = 2;
    public boolean extraHydrationConvertsToQuenched = true;
    public boolean dehydrationHaltsHealthRegen = true;

    public int mountainsY = 100;
    public int cavesY = 48;
    public int runningWaterPurification = 1;
    public int defaultPurity = 2;
    public boolean quenchWhenDebuffed = true;
    public int[] nauseaChance = {100, 50, 5, 0};
    public int[] poisonChance = {30, 10, 0, 0};

    public boolean enableKeywordMatching = false;
    public String keywordBlacklist = "dried|candied|leaf|leaves|gummy|crate|jam|sauce|bucket|seed|cookie|pie|bush|sapling|bean|curry|cake|candy";
    public String drinkKeywords = "drink|juice|tea|soda|coffee|wine|beer|cider|yogurt|milkshake|smoothie";
    public String soupKeywords = "soup|stew|porridge";
    public String fruitKeywords = "fruit|berry|berries|grape|orange|peach|pear|coconut|lemon|melon|cherry|apple";
    public Set<String> itemBlacklist = new LinkedHashSet<>();
    public Map<String, int[]> drinks = defaultDrinks();
    public Map<String, int[]> foods = defaultFoods();

    public static ThirstConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        ThirstConfig loaded = null;
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                loaded = GSON.fromJson(reader, ThirstConfig.class);
            } catch (Exception exception) {
                ThirstWasTaken.LOGGER.error("Could not read {}", PATH, exception);
            }
        }
        INSTANCE = loaded == null ? new ThirstConfig() : loaded;
        INSTANCE.sanitize();
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException exception) {
            ThirstWasTaken.LOGGER.error("Could not write {}", PATH, exception);
        }
    }

    private void sanitize() {
        if (drinks == null) drinks = defaultDrinks();
        if (foods == null) foods = defaultFoods();
        if (itemBlacklist == null) itemBlacklist = new LinkedHashSet<>();
        if (nauseaChance == null || nauseaChance.length != 4) nauseaChance = new int[]{100, 50, 5, 0};
        if (poisonChance == null || poisonChance.length != 4) poisonChance = new int[]{30, 10, 0, 0};
        defaultPurity = clamp(defaultPurity, 0, 3);
        fireResistanceDehydrationPercent = clamp(fireResistanceDehydrationPercent, 0, 100);
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

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
