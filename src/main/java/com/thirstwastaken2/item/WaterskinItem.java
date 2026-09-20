package com.thirstwastaken2.item;

import com.thirstwastaken2.platform.DrinkItem;
import com.thirstwastaken2.platform.Vanilla;
import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;

/** A reusable leather water container holding three drinks of one mixed purity. */
public final class WaterskinItem extends DrinkItem {
    public static final int CAPACITY = 3;

    public WaterskinItem(Properties properties) {
        super(properties, null);
    }

    public static int servings(ItemStack stack) {
        return stack.getOrDefault(ThirstComponents.WATER_SERVINGS, 0);
    }

    public static boolean addWater(ItemStack stack, int purity, int amount) {
        return addWater(stack, WaterQuality.fresh(purity), amount);
    }

    public static boolean addWater(ItemStack stack, WaterQuality added, int amount) {
        int current = servings(stack);
        if (!stack.is(ThirstItems.WATERSKIN) || current >= CAPACITY || amount <= 0) return false;

        int poured = Math.min(amount, CAPACITY - current);
        WaterQuality mixed = current == 0 ? added : mix(WaterPurity.quality(stack), current, added, poured);
        setServings(stack, current + poured);
        WaterPurity.setQuality(stack, mixed);
        return true;
    }

    /**
     * Serving-weighted mixing, rounded down, so that one clean mouthful cannot talk a whole batch up
     * a grade. Salt is not averaged at all: a single salty serving turns the skin into sea water,
     * which is what keeps the sea worth avoiding.
     */
    private static WaterQuality mix(WaterQuality existing, int held, WaterQuality added, int poured) {
        if (existing instanceof WaterQuality.Fresh inside && added instanceof WaterQuality.Fresh pouring) {
            return WaterQuality.fresh((inside.purity() * held + pouring.purity() * poured) / (held + poured));
        }
        return WaterQuality.SALT;
    }

    /** Removes stored drinks, clearing their quality once the waterskin becomes empty. */
    public static boolean removeWater(ItemStack stack, int amount) {
        int current = servings(stack);
        if (!stack.is(ThirstItems.WATERSKIN) || current <= 0 || amount <= 0) return false;

        int remaining = Math.max(0, current - amount);
        setServings(stack, remaining);
        if (remaining == 0) clearWaterQuality(stack);
        return true;
    }

    @Override
    protected boolean canDrink(ItemStack stack) {
        return servings(stack) > 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int current = servings(stack);
        // Vanilla's side effects only: a drink leaves the skin itself, one serving lighter.
        if (current > 0) drinkEffects(stack, level, entity);
        boolean creativePlayer = entity instanceof Player player && player.getAbilities().instabuild;
        if (!level.isClientSide() && current > 0 && !creativePlayer) {
            removeWater(stack, 1);
        }
        return stack;
    }

    /**
     * Right-click a slotted waterskin with a water bottle or bucket on the cursor. Bottles add one
     * serving; a bucket fills every remaining serving. Dynamic purity is retained and mixed.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack waterskin, ItemStack carried, Slot slot,
                                            ClickAction action, Player player, SlotAccess carriedAccess) {
        if (action != ClickAction.SECONDARY || servings(waterskin) >= CAPACITY) return false;

        ItemStack remainder;
        int amount;
        if (isWaterBottle(carried)) {
            remainder = new ItemStack(Items.GLASS_BOTTLE);
            amount = 1;
        } else if (carried.is(Items.WATER_BUCKET)) {
            remainder = new ItemStack(Items.BUCKET);
            amount = CAPACITY;
        } else {
            return false;
        }

        if (!addWater(waterskin, WaterPurity.quality(carried), amount)) return false;
        if (!player.getAbilities().instabuild) consumeContainer(carried, remainder, player, carriedAccess);
        slot.setChanged();
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return servings(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * servings(stack) / CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return switch (WaterPurity.quality(stack)) {
            case WaterQuality.Salt ignored -> 0xD8D2BE;
            case WaterQuality.Fresh fresh -> switch (fresh.purity()) {
                case 0 -> 0x8A5A2B;
                case 1 -> 0xB09A63;
                case 2 -> 0x3F76E4;
                default -> 0x42C8F5;
            };
        };
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    private static void setServings(ItemStack stack, int servings) {
        stack.set(ThirstComponents.WATER_SERVINGS, servings);
        if (servings == 0) {
            stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_MODEL_DATA,
                    Vanilla.modelSelector(ThirstItems.WATERSKIN_MODEL_INDEX, servings));
        }
    }

    private static void clearWaterQuality(ItemStack stack) {
        stack.remove(ThirstComponents.WATER_PURITY);
        stack.remove(ThirstComponents.WATER_SALTY);
    }

    private static void consumeContainer(ItemStack carried, ItemStack remainder, Player player,
                                         SlotAccess carriedAccess) {
        if (carried.getCount() == 1) {
            carriedAccess.set(remainder);
            return;
        }
        carried.shrink(1);
        Vanilla.placeItemBackInInventory(player, remainder);
    }
}
