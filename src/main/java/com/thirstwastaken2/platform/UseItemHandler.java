package com.thirstwastaken2.platform;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * A player using the item in a hand without aiming at a block, on both sides. Returning anything but
 * {@link InteractionResult#PASS} ends the interaction and skips every handler registered after it.
 */
@FunctionalInterface
public interface UseItemHandler {
    InteractionResult use(Player player, Level level, InteractionHand hand);
}
