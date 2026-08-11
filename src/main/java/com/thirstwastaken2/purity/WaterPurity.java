package com.thirstwastaken2.purity;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterPurity {
    public static final int MIN = 0;
    public static final int MAX = 3;
    /** Zero means unset; stored values 1-4 correspond to purity 0-3. */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 4);
    public static final BooleanProperty BLOCK_SALTY = BooleanProperty.create("salty");

    private static final TagKey<Biome> STAGNANT_WATER = TagKey.create(
            Registries.BIOME, com.thirstwastaken2.ThirstWasTaken2.id("stagnant_water"));
    private static final int SURFACE_MOUNTAIN_Y = 100;
    private static final int DEEP_AQUIFER_Y = 32;
    private static final int SALTY_EXHAUSTION = 8;

    /** Purity that has to be looked up from the config instead of being baked into the item. */
    private static final int PURITY_FROM_CONFIG = -1;

    private record ItemInfo(boolean container, int staticPurity) { }

    private static final ItemInfo NOT_A_CONTAINER = new ItemInfo(false, PURITY_FROM_CONFIG);
    private static final Map<Item, ItemInfo> INFO = new ConcurrentHashMap<>();

    private WaterPurity() { }

    public static boolean isWaterContainer(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ThirstItems.WATERSKIN)) return WaterskinItem.servings(stack) > 0;
        if (info(stack.getItem()).container()) return true;
        // Water bottles are plain potions distinguished only by their contents component.
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        return potion != null && potion.is(Potions.WATER);
    }

    public static int get(ItemStack stack) {
        Integer purity = stack.get(ThirstComponents.WATER_PURITY);
        if (purity != null) return purity;
        int staticPurity = info(stack.getItem()).staticPurity();
        return staticPurity == PURITY_FROM_CONFIG ? ThirstConfig.get().defaultPurity : staticPurity;
    }

    public static ItemStack set(ItemStack stack, int purity) {
        return setQuality(stack, WaterQuality.fromPurity(purity, isSalty(stack)));
    }

    public static WaterQuality quality(ItemStack stack) {
        Integer contamination = stack.get(ThirstComponents.WATER_CONTAMINATION);
        return contamination == null
                ? WaterQuality.fromPurity(get(stack), isSalty(stack))
                : new WaterQuality(contamination, isSalty(stack));
    }

    public static boolean isSalty(ItemStack stack) {
        return stack.getOrDefault(ThirstComponents.WATER_SALTY, false);
    }

    public static ItemStack setQuality(ItemStack stack, WaterQuality quality) {
        stack.set(ThirstComponents.WATER_PURITY, quality.purity());
        stack.set(ThirstComponents.WATER_CONTAMINATION, quality.contamination());
        stack.set(ThirstComponents.WATER_SALTY, quality.salty());
        syncModel(stack, quality.purity());
        return stack;
    }

    public static ItemStack purify(ItemStack stack, int levels) {
        if (isWaterContainer(stack)) {
            WaterQuality quality = quality(stack);
            int targetPurity = Math.min(MAX, quality.purity() + levels);
            setQuality(stack, WaterQuality.fromPurity(targetPurity, quality.salty()));
        }
        return stack;
    }

    /** Player-facing tier for the full environmental sample at {@code pos}. */
    public static int at(Level level, BlockPos pos) {
        return sampleAt(level, pos).purity();
    }

    /**
     * Samples only when water is collected or drunk. The fixed 5x3x5 inspection has no entity
     * lookup, allocation per block or tick-time cost, while biome tags keep modded worlds extensible.
     */
    public static WaterQuality sampleAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BLOCK_PURITY)) {
            int stored = state.getValue(BLOCK_PURITY);
            if (stored > 0) {
                boolean salty = state.hasProperty(BLOCK_SALTY) && state.getValue(BLOCK_SALTY);
                return WaterQuality.fromPurity(stored - 1, salty);
            }
        }

        ThirstConfig config = ThirstConfig.get();
        FluidState fluid = state.getFluidState();
        if (!fluid.is(FluidTags.WATER)) return WaterQuality.fromPurity(config.defaultPurity, false);

        var biome = level.getBiome(pos);
        boolean salty = biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH);
        int contamination;
        if (salty) contamination = 25;
        else if (biome.is(STAGNANT_WATER)) contamination = 85;
        else if (biome.is(BiomeTags.IS_RIVER)) contamination = 42;
        else if (biome.is(BiomeTags.IS_MOUNTAIN)) contamination = 28;
        else if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_SAVANNA)
                || biome.is(BiomeTags.IS_BADLANDS)) contamination = 70;
        else contamination = 55;

        float temperature = biome.value().getBaseTemperature();
        if (temperature >= 1.5F) contamination += 10;
        else if (temperature <= 0.15F) contamination -= 10;
        if (pos.getY() > SURFACE_MOUNTAIN_Y || pos.getY() < DEEP_AQUIFER_Y) contamination -= 5;
        if (!fluid.isSource()) contamination -= 5;
        contamination += nearbyPollution(level, pos);
        return new WaterQuality(contamination, salty);
    }

    /** Applies the original four-tier sickness table and returns whether hydration should be granted. */
    public static boolean applyEffects(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer) || !isWaterContainer(stack)) return true;
        int purity = Math.max(MIN, Math.min(MAX, get(stack)));
        ThirstConfig config = ThirstConfig.get();
        if (isSalty(stack)) {
            ThirstManager.addExhaustion(player, SALTY_EXHAUSTION);
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5));
            return false;
        }
        // A single roll drives both effects, exactly like the original mod.
        float roll = player.getRandom().nextFloat() * 100.0F;
        if (roll < config.nauseaChance[purity]) {
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5));
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30));
        }
        boolean poisoned = roll < config.poisonChance[purity];
        if (poisoned) player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10));
        return config.quenchWhenDebuffed || !poisoned;
    }

    public static Component tooltip(int purity) {
        String suffix = switch (purity) {
            case 0 -> "dirty";
            case 1 -> "slightly_dirty";
            case 2 -> "acceptable";
            default -> "purified";
        };
        int color = switch (purity) {
            case 0 -> 0xA84825;
            case 1 -> 0x796C71;
            case 2 -> 0x5D829D;
            default -> 0x21B1FF;
        };
        return Component.translatable("thirst.purity." + suffix).withColor(color);
    }

    public static Component salinityTooltip() {
        return Component.translatable("thirst.water.salty").withColor(0x55C8E8);
    }

    private static int nearbyPollution(Level level, BlockPos origin) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean muddy = false;
        boolean agricultural = false;
        for (int dy = -1; dy <= 1 && !(muddy && agricultural); dy++) {
            for (int dx = -2; dx <= 2 && !(muddy && agricultural); dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockState nearby = level.getBlockState(cursor.setWithOffset(origin, dx, dy, dz));
                    if (nearby.is(Blocks.MUD) || nearby.is(Blocks.MANGROVE_ROOTS)
                            || nearby.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
                        muddy = true;
                    } else if (nearby.is(Blocks.COMPOSTER) || nearby.is(Blocks.FARMLAND)) {
                        agricultural = true;
                    }
                    if (muddy && agricultural) break;
                }
            }
        }
        return (muddy ? 15 : 0) + (agricultural ? 10 : 0);
    }

    private static void syncModel(ItemStack stack, int purity) {
        if (stack.is(ThirstItems.TERRACOTTA_WATER_BOWL)) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(0.0F, (float) purity), List.of(), List.of(), List.of()));
        }
    }

    private static ItemInfo info(Item item) {
        ItemInfo cached = INFO.get(item);
        return cached != null ? cached : INFO.computeIfAbsent(item, WaterPurity::resolve);
    }

    private static ItemInfo resolve(Item item) {
        if (item == Items.WATER_BUCKET || item == ThirstItems.TERRACOTTA_WATER_BOWL) {
            return new ItemInfo(true, PURITY_FROM_CONFIG);
        }
        if (item == Items.POTION) {
            // Only water bottles count, which isWaterContainer decides per stack.
            return NOT_A_CONTAINER;
        }

        Identifier id = item.builtInRegistryHolder().key().identifier();
        String namespace = id.getNamespace();
        String path = id.getPath();

        if (namespace.equals("toughasnails")) {
            boolean container = path.contains("water_bottle") || path.contains("water_canteen");
            int purity = switch (path) {
                case "dirty_water_bottle", "dirty_water_canteen" -> 0;
                case "water_canteen" -> 2;
                default -> 3;
            };
            return new ItemInfo(container, purity);
        }
        if (namespace.equals("farmersdelight")) {
            // Only the two bottled drinks were registered as containers by the original mod.
            boolean container = path.equals("melon_juice") || path.equals("apple_cider");
            return new ItemInfo(container, 3);
        }
        if (namespace.equals("collectorsreap")) {
            boolean container = path.equals("pomegranate_black_tea") || path.equals("lime_green_tea");
            return new ItemInfo(container, PURITY_FROM_CONFIG);
        }
        if (namespace.equals("farmersrespite") || namespace.equals("brewinandchewin")
                || (namespace.equals("create") && path.equals("builders_tea"))) {
            return new ItemInfo(true, PURITY_FROM_CONFIG);
        }
        return NOT_A_CONTAINER;
    }
}
