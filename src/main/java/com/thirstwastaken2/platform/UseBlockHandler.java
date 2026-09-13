package com.thirstwastaken2.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A player using an item or an empty hand on a block, on both sides. Returning anything but
 * {@link InteractionResult#PASS} ends the interaction and skips every handler registered after it.
 */
@FunctionalInterface
public interface UseBlockHandler {
    InteractionResult use(Player player, Level level, InteractionHand hand, BlockHitResult hit);
}
