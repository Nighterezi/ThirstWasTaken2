package com.thirstwastaken2.item;

import com.thirstwastaken2.purity.ThirstComponents;
import com.thirstwastaken2.purity.WaterPurity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;

/** A reusable leather water container holding three drinks of one mixed purity. */
public final class WaterskinItem extends Item {
    public static final int CAPACITY = 3;

    public WaterskinItem(Properties properties) {
        super(properties);
    }

    public static int servings(ItemStack stack) {
        return stack.getOrDefault(ThirstComponents.WATER_SERVINGS, 0);
    }

    public static boolean addWater(ItemStack stack, int purity, int amount) {
        int current = servings(stack);
        if (!stack.is(ThirstItems.WATERSKIN) || current >= CAPACITY || amount <= 0) return false;

        int added = Math.min(amount, CAPACITY - current);
        int mixedPurity = current == 0 ? purity : Math.min(WaterPurity.get(stack), purity);
        stack.set(ThirstComponents.WATER_SERVINGS, current + added);
        WaterPurity.set(stack, mixedPurity);
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return servings(stack) == 0 ? InteractionResult.PASS : super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        int current = servings(stack);
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (current > 0 && consumable != null) {
            // Run vanilla sounds, game event, stat, criterion and component listeners against a
            // disposable copy; Consumable#onConsume is otherwise hard-wired to shrink its input.
            consumable.onConsume(level, entity, stack.copy());
        }
        boolean creativePlayer = entity instanceof Player player && player.getAbilities().instabuild;
        if (!level.isClientSide() && current > 0 && !creativePlayer) {
            int remaining = current - 1;
            stack.set(ThirstComponents.WATER_SERVINGS, remaining);
            if (remaining == 0) stack.remove(ThirstComponents.WATER_PURITY);
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

        if (!addWater(waterskin, WaterPurity.get(carried), amount)) return false;
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
        return 0x3F76E4;
    }

    private static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) return false;
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    private static void consumeContainer(ItemStack carried, ItemStack remainder, Player player,
                                         SlotAccess carriedAccess) {
        if (carried.getCount() == 1) {
            carriedAccess.set(remainder);
            return;
        }
        carried.shrink(1);
        player.getInventory().placeItemBackInInventory(remainder);
    }
}
