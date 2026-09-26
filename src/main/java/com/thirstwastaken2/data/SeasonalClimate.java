package com.thirstwastaken2.data;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/**
 * What the season does to the climate at a player, for an integration that keeps a calendar. Serene
 * Seasons' sets one at init through {@link ThirstManager#setSeasonalClimate}.
 *
 * <p>Not API: no other mod may rely on it. Asked only when the drain recomputes its modifier, at most
 * once a second per player, on the server thread.
 */
public interface SeasonalClimate {
    /**
     * A factor on the climate modifier, after the curve: above 1 drains faster. 1 where the season
     * changes nothing, such as a dimension without seasons or a biome the season never warms or cools.
     */
    float drainMultiplier(Player player, Holder<Biome> biome);

    /**
     * Whether it rains in {@code biome} this time of year, for the climate's humidity. {@code biomeSays}
     * is the biome's own answer, which a place without a wet and a dry season returns unchanged.
     */
    boolean hasPrecipitation(Player player, Holder<Biome> biome, boolean biomeSays);
}
