package com.thirstwastaken.purity;

import com.thirstwastaken.config.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import java.util.Locale;

public final class WaterPurity {
    public static final int MIN = 0;
    public static final int MAX = 3;
    /** Zero means unset; stored values 1-4 correspond to purity 0-3. */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 4);

    private WaterPurity() { }

    public static boolean isWaterContainer(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET) || id(stack).equals("thirstwastaken:terracotta_water_bowl")) return true;
        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if (potion != null && potion.is(Potions.WATER)) return true;
        String id = id(stack);
        return id.startsWith("toughasnails:") && (id.contains("water_bottle") || id.contains("water_canteen"))
                || id.equals("create:builders_tea") || id.startsWith("farmersrespite:")
                || id.startsWith("brewinandchewin:");
    }

    public static int get(ItemStack stack) {
        Integer purity = stack.get(ThirstComponents.WATER_PURITY);
        if (purity != null) return purity;
        String id = id(stack);
        if (id.equals("toughasnails:dirty_water_bottle") || id.equals("toughasnails:dirty_water_canteen")) return 0;
        if (id.equals("toughasnails:water_canteen")) return 2;
        if (id.startsWith("toughasnails:") || id.startsWith("farmersdelight:")) return 3;
        return ThirstConfig.get().defaultPurity;
    }

    public static ItemStack set(ItemStack stack, int purity) {
        stack.set(ThirstComponents.WATER_PURITY, Math.max(MIN, Math.min(MAX, purity)));
        return stack;
    }

    public static ItemStack purify(ItemStack stack, int levels) {
        if (isWaterContainer(stack)) set(stack, Math.min(MAX, get(stack) + levels));
        return stack;
    }

    public static int at(Level level, BlockPos pos) {
        if (level.getBlockState(pos).hasProperty(BLOCK_PURITY)) {
            int stored = level.getBlockState(pos).getValue(BLOCK_PURITY);
            if (stored > 0) return stored - 1;
        }
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return ThirstConfig.get().defaultPurity;
        ThirstConfig config = ThirstConfig.get();
        int purity = pos.getY() > config.mountainsY || pos.getY() < config.cavesY ? 1 : 0;
        if (!level.getFluidState(pos).isSource()) purity += config.runningWaterPurification;
        return Math.max(MIN, Math.min(MAX, purity));
    }

    /** Applies the original four-tier sickness table and returns whether hydration should be granted. */
    public static boolean applyEffects(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer) || !isWaterContainer(stack)) return true;
        int purity = get(stack);
        ThirstConfig config = ThirstConfig.get();
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

    private static String id(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().key().identifier().toString().toLowerCase(Locale.ROOT);
    }
}
