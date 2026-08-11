package com.thirstwastaken2.purity;

import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.item.ThirstItems;
import com.thirstwastaken2.item.WaterskinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterPurity {
    public static final int MIN = 0;
    public static final int MAX = 3;
    /** Zero means unset; stored values 1-4 correspond to purity 0-3. */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 4);

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
        stack.set(ThirstComponents.WATER_PURITY, Math.max(MIN, Math.min(MAX, purity)));
        return stack;
    }

    public static ItemStack purify(ItemStack stack, int levels) {
        if (isWaterContainer(stack)) set(stack, Math.min(MAX, get(stack) + levels));
        return stack;
    }

    /** Purity of the water at {@code pos}, taking block purity, altitude and flow into account. */
    public static int at(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BLOCK_PURITY)) {
            int stored = state.getValue(BLOCK_PURITY);
            if (stored > 0) return stored - 1;
        }

        ThirstConfig config = ThirstConfig.get();
        FluidState fluid = state.getFluidState();
        if (!fluid.is(FluidTags.WATER)) return config.defaultPurity;

        int purity = pos.getY() > config.mountainsY || pos.getY() < config.cavesY ? 1 : 0;
        if (!fluid.isSource()) purity += config.runningWaterPurification;
        return Math.max(MIN, Math.min(MAX, purity));
    }

    /** Applies the original four-tier sickness table and returns whether hydration should be granted. */
    public static boolean applyEffects(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer) || !isWaterContainer(stack)) return true;
        int purity = Math.max(MIN, Math.min(MAX, get(stack)));
        ThirstConfig config = ThirstConfig.get();
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
            case 0 -> 0xA84D25;
            case 1 -> 0x7964F1;
            case 2 -> 0x5D82DD;
            default -> 0x21B1FF;
        };
        return Component.translatable("thirst.purity." + suffix).withColor(color);
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
