package com.thirstwastaken2.purity;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import com.thirstwastaken2.platform.Vanilla;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterPurity {
    public static final int MIN = 0;
    public static final int MAX = 3;
    /**
     * What a cauldron holds, as one value rather than a grade plus a flag: {@link #BLOCK_UNSET} for
     * a cauldron nothing has been poured into, 1-4 for the four grades, and {@link #BLOCK_SALT} for
     * sea water. A separate boolean could not work, because vanilla hands a freshly placed block the
     * first value of every property it has, and for a boolean that value is {@code true}.
     */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 5);
    public static final int BLOCK_UNSET = 0;
    public static final int BLOCK_SALT = 5;
    /** Vanilla's description id for {@code Blocks.WATER_CAULDRON}, see {@link #addCauldronProperties}. */
    private static final String WATER_CAULDRON = "block.minecraft.water_cauldron";

    private static final TagKey<Biome> STAGNANT_WATER = TagKey.create(
            Registries.BIOME, ThirstWasTaken2.id("stagnant_water"));
    private static final int SURFACE_MOUNTAIN_Y = 100;
    private static final int DEEP_AQUIFER_Y = 32;
    private static final int SALTY_EXHAUSTION = 8;

    /** Bounds of the contamination score a sample is graded from. It is never stored on an item. */
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;

    /**
     * Vanilla containers cannot be given a model of ours at registration, so salt water swaps their
     * whole item model through {@code minecraft:item_model}. The mod's own bowl has custom model
     * data for the same job.
     */
    private static final Identifier SALT_WATER_BOTTLE_MODEL = ThirstWasTaken2.id("salt_water_bottle");
    private static final Identifier SALT_WATER_BUCKET_MODEL = ThirstWasTaken2.id("salt_water_bucket");
    /** The bowl's model variant for salt water, one past the four grades. */
    private static final float SALT_BOWL_MODEL = 4.0F;

    /** Purity that has to be looked up from the config instead of being baked into the item. */
    private static final int PURITY_FROM_CONFIG = -1;

    private record ItemInfo(boolean container, boolean plainWater, int staticPurity) { }

    private static final ItemInfo NOT_A_CONTAINER = new ItemInfo(false, false, PURITY_FROM_CONFIG);
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

    /** Water-only drinks are blocked at a full thirst bar, unlike drinks with other gameplay uses. */
    public static boolean isPlainWaterDrink(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ThirstItems.WATERSKIN)) return WaterskinItem.servings(stack) > 0;
        if (stack.is(ThirstItems.TERRACOTTA_WATER_BOWL)) return true;
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (stack.is(Items.POTION) && potion != null && potion.is(Potions.WATER)) return true;
        return info(stack.getItem()).plainWater();
    }

    /**
     * The grade of the fresh water in {@code stack}. Salt water has no grade, so unless salinity has
     * already been ruled out, ask {@link #quality(ItemStack)} instead of this.
     */
    public static int get(ItemStack stack) {
        Integer purity = stack.get(ThirstComponents.WATER_PURITY);
        if (purity != null) return purity;
        int staticPurity = info(stack.getItem()).staticPurity();
        return staticPurity == PURITY_FROM_CONFIG ? ThirstConfig.get().defaultPurity : staticPurity;
    }

    public static WaterQuality quality(ItemStack stack) {
        return isSalty(stack) ? WaterQuality.SALT : WaterQuality.fresh(get(stack));
    }

    public static boolean isSalty(ItemStack stack) {
        return stack.getOrDefault(ThirstComponents.WATER_SALTY, false);
    }

    /** Whether a container already carries a sampled quality of its own. */
    public static boolean isStamped(ItemStack stack) {
        return stack.has(ThirstComponents.WATER_PURITY) || isSalty(stack);
    }

    public static ItemStack set(ItemStack stack, int purity) {
        return setQuality(stack, WaterQuality.fresh(purity));
    }

    public static ItemStack setQuality(ItemStack stack, WaterQuality quality) {
        switch (quality) {
            case WaterQuality.Salt ignored -> {
                // Salt water stores no grade at all, so nothing can read one off it by accident and
                // the purification recipes, which all match on a grade, cannot match it either.
                stack.remove(ThirstComponents.WATER_PURITY);
                stack.set(ThirstComponents.WATER_SALTY, true);
            }
            case WaterQuality.Fresh fresh -> {
                stack.set(ThirstComponents.WATER_PURITY, fresh.purity());
                // Written even though false is the default: the purification recipes match on it, and
                // a container that left it out would silently stop being cookable.
                stack.set(ThirstComponents.WATER_SALTY, false);
            }
        }
        syncModel(stack, quality);
        return stack;
    }

    /** Raises the grade of fresh water. Salt water has no grade to raise and comes back unchanged. */
    public static ItemStack purify(ItemStack stack, int levels) {
        if (isWaterContainer(stack) && quality(stack) instanceof WaterQuality.Fresh fresh) {
            setQuality(stack, WaterQuality.fresh(fresh.purity() + levels));
        }
        return stack;
    }

    /**
     * Samples only when water is collected or drunk. The fixed 5x3x5 inspection has no entity
     * lookup, allocation per block or tick-time cost, while biome tags keep modded worlds extensible.
     */
    public static WaterQuality sampleAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        WaterQuality stored = storedQuality(state);
        if (stored != null) return stored;

        ThirstConfig config = ThirstConfig.get();
        FluidState fluid = state.getFluidState();
        if (!fluid.is(FluidTags.WATER)) return WaterQuality.fresh(config.defaultPurity);

        var biome = level.getBiome(pos);
        // The sea is not a grade of fresh water, so it never reaches the scoring below. This also
        // spares the neighbourhood scan on every coastline.
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) return WaterQuality.SALT;

        int score;
        if (biome.is(STAGNANT_WATER)) score = 85;
        else if (biome.is(BiomeTags.IS_RIVER)) score = 42;
        else if (biome.is(BiomeTags.IS_MOUNTAIN)) score = 28;
        else if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_SAVANNA)
                || biome.is(BiomeTags.IS_BADLANDS)) score = 70;
        else score = 55;

        float temperature = biome.value().getBaseTemperature();
        if (temperature >= 1.5F) score += 10;
        else if (temperature <= 0.15F) score -= 10;
        if (pos.getY() > SURFACE_MOUNTAIN_Y || pos.getY() < DEEP_AQUIFER_Y) score -= 5;
        if (!fluid.isSource()) score -= 5;
        score += nearbyPollution(level, pos);
        return WaterQuality.fresh(grade(score));
    }

    /** Applies the four-grade sickness table and returns whether thirst should still be restored. */
    public static boolean applyEffects(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer) || !isWaterContainer(stack)) return true;
        ThirstConfig config = ThirstConfig.get();
        switch (quality(stack)) {
            case WaterQuality.Salt ignored -> {
                ThirstManager.addExhaustion(player, SALTY_EXHAUSTION);
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5));
                return false;
            }
            case WaterQuality.Fresh fresh -> {
                // A single roll drives both effects, exactly like the original mod.
                float roll = player.getRandom().nextFloat() * 100.0F;
                if (roll < config.nauseaChance[fresh.purity()]) {
                    player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5));
                    player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30));
                }
                boolean poisoned = roll < config.poisonChance[fresh.purity()];
                if (poisoned) player.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10));
                return config.quenchWhenDebuffed || !poisoned;
            }
        }
    }

    /** @return a fresh copy of the grade line, see {@link TooltipLines}. */
    public static Component tooltip(int purity) {
        return TooltipLines.PURITY[Math.clamp(purity, MIN, MAX)].copy();
    }

    /** The one line salt water gets. It replaces the grade line rather than joining it. */
    public static Component saltTooltip() {
        return TooltipLines.SALT.copy();
    }

    /**
     * Adds the stored quality properties to the water cauldron and nothing else. Powder snow cauldrons
     * are {@code LayeredCauldronBlock}s too, but they never hold water, and the two properties would
     * multiply their blockstates tenfold for nothing.
     *
     * <p>Runs inside the block's constructor, before {@code Blocks.WATER_CAULDRON} is assigned or the
     * block is registered, so its description id is the only identity available.
     */
    public static void addCauldronProperties(Block block, StateDefinition.Builder<Block, BlockState> builder) {
        if (WATER_CAULDRON.equals(block.getDescriptionId())) builder.add(BLOCK_PURITY);
    }

    /** What a cauldron holds, or {@code null} when nothing has been poured into it yet. */
    public static WaterQuality storedQuality(BlockState state) {
        if (!state.hasProperty(BLOCK_PURITY)) return null;
        int stored = state.getValue(BLOCK_PURITY);
        if (stored == BLOCK_UNSET) return null;
        return stored == BLOCK_SALT ? WaterQuality.SALT : WaterQuality.fresh(stored - 1);
    }

    /** The blockstate value that stores {@code quality} in a cauldron. */
    public static int storedValue(WaterQuality quality) {
        // Grades are offset by one so that zero can act as "unset".
        return quality instanceof WaterQuality.Fresh fresh ? fresh.purity() + 1 : BLOCK_SALT;
    }

    /** The grade a sampled contamination score falls into. */
    private static int grade(int score) {
        int clamped = Math.clamp(score, MIN_SCORE, MAX_SCORE);
        if (clamped <= 15) return 3;
        if (clamped <= 35) return 2;
        if (clamped <= 65) return 1;
        return 0;
    }

    private static String purityKey(int purity) {
        return switch (purity) {
            case 0 -> "thirst.purity.dirty";
            case 1 -> "thirst.purity.slightly_dirty";
            case 2 -> "thirst.purity.acceptable";
            default -> "thirst.purity.purified";
        };
    }

    /**
     * The grade ramp runs warm to cool so that all four stay apart on a dark tooltip. Salt sits off
     * that ramp on purpose: on this tooltip, blue means drinkable.
     */
    private static int purityColor(int purity) {
        return switch (purity) {
            case 0 -> 0xB0632E;
            case 1 -> 0xC2A878;
            case 2 -> 0x74B8E0;
            default -> 0x4FD6FF;
        };
    }

    /**
     * Tooltip lines are rebuilt every frame a stack is hovered, so each one is built once and copied
     * out. A copy shares the translatable contents, and with them the parsed translation, while the
     * caller stays free to restyle its own line in place. Nested so that nothing is built during block
     * bootstrap, which is when {@code WaterPurity} itself loads.
     */
    private static final class TooltipLines {
        static final Component[] PURITY = new Component[MAX + 1];
        static final Component SALT = Component.translatable("thirst.water.salty").withColor(0xE6DFC8);

        static {
            for (int purity = MIN; purity <= MAX; purity++) {
                PURITY[purity] = Component.translatable(purityKey(purity)).withColor(purityColor(purity));
            }
        }
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

    /** Keeps the sprite in step with the contents, so that salt water never looks drinkable. */
    private static void syncModel(ItemStack stack, WaterQuality quality) {
        if (stack.is(ThirstItems.TERRACOTTA_WATER_BOWL)) {
            float variant = quality instanceof WaterQuality.Fresh fresh ? fresh.purity() : SALT_BOWL_MODEL;
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    new CustomModelData(List.of(0.0F, variant), List.of(), List.of(), List.of()));
            return;
        }
        Identifier saltModel = saltModel(stack);
        if (saltModel == null) return;
        if (quality.salty()) {
            stack.set(DataComponents.ITEM_MODEL, saltModel);
        } else if (saltModel.equals(stack.get(DataComponents.ITEM_MODEL))) {
            // Only ever clears a model this mod set, so a modded container keeps its own.
            stack.remove(DataComponents.ITEM_MODEL);
        }
    }

    /** The salt-water sprite for a vanilla container, or {@code null} for anything else. */
    private static Identifier saltModel(ItemStack stack) {
        if (stack.is(Items.POTION)) return SALT_WATER_BOTTLE_MODEL;
        if (stack.is(Items.WATER_BUCKET)) return SALT_WATER_BUCKET_MODEL;
        return null;
    }

    private static ItemInfo info(Item item) {
        ItemInfo cached = INFO.get(item);
        return cached != null ? cached : INFO.computeIfAbsent(item, WaterPurity::resolve);
    }

    private static ItemInfo resolve(Item item) {
        if (item == Items.WATER_BUCKET || item == ThirstItems.TERRACOTTA_WATER_BOWL) {
            return new ItemInfo(true, item == ThirstItems.TERRACOTTA_WATER_BOWL, PURITY_FROM_CONFIG);
        }
        if (item == Items.POTION) {
            // Only water bottles count, which isWaterContainer decides per stack.
            return NOT_A_CONTAINER;
        }

        Identifier id = Vanilla.itemId(item);
        String namespace = id.getNamespace();
        String path = id.getPath();

        if (namespace.equals("toughasnails")) {
            boolean container = path.contains("water_bottle") || path.contains("water_canteen");
            int purity = switch (path) {
                case "dirty_water_bottle", "dirty_water_canteen" -> 0;
                case "water_canteen" -> 2;
                default -> 3;
            };
            return new ItemInfo(container, container, purity);
        }
        if (namespace.equals("farmersdelight")) {
            // Only the two bottled drinks were registered as containers by the original mod.
            boolean container = path.equals("melon_juice") || path.equals("apple_cider");
            return new ItemInfo(container, false, 3);
        }
        if (namespace.equals("collectorsreap")) {
            boolean container = path.equals("pomegranate_black_tea") || path.equals("lime_green_tea");
            return new ItemInfo(container, false, PURITY_FROM_CONFIG);
        }
        if (namespace.equals("farmersrespite") || namespace.equals("brewinandchewin")) {
            return new ItemInfo(true, false, PURITY_FROM_CONFIG);
        }
        return NOT_A_CONTAINER;
    }
}
