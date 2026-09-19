package com.thirstwastaken2.gametest.platform;

import com.thirstwastaken2.purity.WaterQuality;
import net.minecraft.world.item.ItemStack;

/**
 * One fill or drain through a container's fluid capability, in terms every loader shares.
 *
 * @param amount    millibuckets moved
 * @param quality   the grade the drained water carried, or {@code null} for a fill, for nothing drained,
 *                  or for water that carried no grade at all
 * @param container the container afterwards
 */
public record FluidMove(int amount, WaterQuality quality, ItemStack container) { }
