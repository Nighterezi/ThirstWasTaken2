package com.thirstwastaken2.kaleidoscope;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.purity.WaterPurity;
import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The only place that reads a grade off what goes into a stockpot or a teapot, and reads and writes it on
 * the block entity and on a teapot item. Both blocks keep only a fluid id of their own, so without this
 * a Dirty bucket poured in came back out at {@code defaultPurity} and sea water came back out fresh.
 */
public final class BrewedWaterQuality {
    /** One int, {@link WaterPurity#storedValue}: 1-4 for the four grades, 5 for sea water, absent for none. */
    private static final String KEY = ThirstWasTaken2.MOD_ID + ":water_quality";
    private static final String SALT_REFUSED = "thirstwastaken2.message.salt_water_refused";

    /** The fluid id both blocks keep for water: the stockpot's water soup base and the teapot's tea fluid. */
    private static final Identifier WATER = Identifier.withDefaultNamespace("water");

    private BrewedWaterQuality() { }

    /** Whether a fluid id one of the blocks keeps is water. */
    public static boolean isWater(Identifier fluidId) {
        return WATER.equals(fluidId);
    }

    /** The grade of the water in {@code stack}, or {@code null} when it is not a water container. Read it before the call that spends it. */
    public static WaterQuality of(ItemStack stack) {
        return WaterPurity.isWaterContainer(stack) ? WaterPurity.quality(stack) : null;
    }

    /** The grade of the water at {@code pos}, sampled as a bucket filled there is, or {@code null} when there is no water. */
    public static WaterQuality sample(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!(level instanceof Level world) || !state.getFluidState().is(FluidTags.WATER)) return null;
        return WaterPurity.sampleAt(world, pos);
    }

    /** Adds {@code quality} to the block entity data a teapot item carries, which the teapot loads once placed. */
    public static void stampItem(ItemStack teapot, WaterQuality quality) {
        CustomData data = teapot.get(DataComponents.BLOCK_ENTITY_DATA);
        if (quality == null || data == null) return;
        CompoundTag tag = data.copyTag();
        save(tag, quality);
        teapot.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
    }

    public static void save(CompoundTag tag, WaterQuality quality) {
        if (quality != null) tag.putInt(KEY, WaterPurity.storedValue(quality));
    }

    /** What {@link #save} wrote, or {@code null}: a block filled before the integration existed has no grade to keep. */
    public static WaterQuality load(CompoundTag tag) {
        if (!tag.contains(KEY)) return null;
        int stored = tag.getInt(KEY);
        if (stored == WaterPurity.BLOCK_SALT) return WaterQuality.SALT;
        return stored > WaterPurity.BLOCK_UNSET ? WaterQuality.fresh(stored - 1) : null;
    }

    /**
     * Tells a player why the teapot would not take sea water, in the action bar as Kaleidoscope Cookery's
     * own refusals are. The teapot runs on both sides, so only the server says it, once.
     */
    public static void refuseSalt(LivingEntity user) {
        if (user instanceof ServerPlayer player) player.displayClientMessage(Component.translatable(SALT_REFUSED), true);
    }
}
