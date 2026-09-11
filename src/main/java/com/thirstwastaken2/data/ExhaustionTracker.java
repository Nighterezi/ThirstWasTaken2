package com.thirstwastaken2.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Per-player scratch state for the exhaustion path, added to every {@link Player} by
 * {@code PlayerMixin}. None of it is persisted or synced: it lives on the entity so that the exhaustion
 * hook neither writes the attachment nor recomputes the modifier every time vanilla charges exhaustion.
 *
 * <p>Server thread only. A respawn creates a new player and so a fresh tracker, which loses at most one
 * tick of pending exhaustion.
 */
public final class ExhaustionTracker {
    /** Raw vanilla exhaustion mirrored since the last tick, before the modifier is applied. */
    float pending;
    float modifier;
    int modifierExpiresAt;
    int modifierGeneration;
    Level modifierLevel;

    /** Implemented by {@code PlayerMixin}; the member is prefixed because it lands on a vanilla class. */
    public interface Holder {
        ExhaustionTracker thirst$exhaustionTracker();
    }

    static ExhaustionTracker of(Player player) {
        return ((Holder) player).thirst$exhaustionTracker();
    }
}
