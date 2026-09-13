package com.thirstwastaken2.platform;

import net.minecraft.world.entity.player.Player;

/**
 * A value stored on every player, saved with the player and synced to that player's own client.
 * Created by {@code Loader.playerData}; each loader backs it with its own attachment system.
 */
public interface PlayerData<T> {
    /** The stored value, or the initial value when nothing has been stored yet. */
    T get(Player player);

    /** Replaces the stored value. Every call on the server costs a sync packet, so skip unchanged values. */
    void set(Player player, T value);
}
