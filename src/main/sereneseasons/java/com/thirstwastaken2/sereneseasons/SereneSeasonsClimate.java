package com.thirstwastaken2.sereneseasons;

import com.thirstwastaken2.ThirstWasTaken2;
import com.thirstwastaken2.config.ThirstConfig;
import com.thirstwastaken2.data.SeasonalClimate;
import com.thirstwastaken2.data.ThirstManager;
import com.thirstwastaken2.sereneseasons.platform.SeasonsPlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

/**
 * Serene Seasons' calendar in the thirst drain: a factor per season, blended from the middle of one
 * season to the middle of the next, and a tropical biome's dry season counted as dry.
 *
 * <p>It follows Serene Seasons' own rules for where a season is felt, so the drain and the snow agree: a
 * dimension its config leaves out, a biome in {@code #sereneseasons:blacklisted_biomes} and a biome
 * warmer than 0.8 (deserts, badlands) have no season; a tropical biome has a wet and a dry season in
 * place of the four, which changes its humidity and nothing else. Serene Seasons' own temperature
 * offsets are not read: they only ever cool (summer is 0), so they could only make winter easier, and
 * they live in its internal config.
 *
 * <p>Read at most once a second per player: the drain caches its modifier for 20 ticks.
 */
public final class SereneSeasonsClimate implements SeasonalClimate {
    private static final TagKey<Biome> BLACKLISTED = TagKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath(SereneSeasonsPresence.MOD_ID, "blacklisted_biomes"));
    /** Serene Seasons leaves any biome warmer than this at its own temperature all year. */
    private static final float MAX_SEASONAL_TEMPERATURE = 0.8F;
    /** Three sub-seasons to a season; spring's middle is 1.5 sub-seasons into the year. */
    private static final int SUB_SEASONS_PER_SEASON = 3;
    private static final int SEASONS = 4;

    private SereneSeasonsClimate() { }

    static void install() {
        ThirstManager.setSeasonalClimate(new SereneSeasonsClimate());
        ThirstWasTaken2.LOGGER.info("Serene Seasons found, thirst follows the season");
    }

    @Override
    public float drainMultiplier(Player player, Holder<Biome> biome) {
        Level level = player.level();
        if (!hasSeasons(level, biome) || SeasonHelper.usesTropicalSeasons(biome)
                || biome.value().getBaseTemperature() > MAX_SEASONAL_TEMPERATURE) {
            return 1.0F;
        }
        return (float) blend(SeasonHelper.getSeasonState(level), ThirstConfig.get());
    }

    /** Mirrors Serene Seasons' own rain: none in the middle of the dry season, always in the wet one's. */
    @Override
    public boolean hasPrecipitation(Player player, Holder<Biome> biome, boolean biomeSays) {
        Level level = player.level();
        if (!hasSeasons(level, biome) || !SeasonHelper.usesTropicalSeasons(biome)) return biomeSays;
        Season.TropicalSeason season = SeasonHelper.getSeasonState(level).getTropicalSeason();
        if (season == Season.TropicalSeason.MID_DRY) return false;
        if (season == Season.TropicalSeason.MID_WET) return true;
        return biomeSays;
    }

    private static boolean hasSeasons(Level level, Holder<Biome> biome) {
        return SeasonsPlatform.hasSeasons(level) && !biome.is(BLACKLISTED);
    }

    /**
     * The factor at this point of the year: each season's value holds at its middle, and the factor
     * moves in a straight line from one middle to the next, so a sub-season's first day is no step.
     */
    static double blend(ISeasonState state, ThirstConfig config) {
        int subSeason = state.getSubSeasonDuration();
        if (subSeason <= 0) return 1.0;
        double subSeasons = Season.SubSeason.VALUES.length;
        // Sub-seasons since the middle of spring, wrapped into one year.
        double position = ((double) state.getSeasonCycleTicks() / subSeason) % subSeasons;
        double sinceSpring = position - SUB_SEASONS_PER_SEASON / 2.0;
        if (sinceSpring < 0) sinceSpring += subSeasons;
        int from = (int) (sinceSpring / SUB_SEASONS_PER_SEASON) % SEASONS;
        double progress = (sinceSpring - from * SUB_SEASONS_PER_SEASON) / SUB_SEASONS_PER_SEASON;
        double start = factor(config, from);
        return start + (factor(config, (from + 1) % SEASONS) - start) * progress;
    }

    /** Spring, summer, autumn, winter: the order of {@code Season.SubSeason}, which starts in spring. */
    private static double factor(ThirstConfig config, int season) {
        return switch (season) {
            case 0 -> config.seasonDrainSpring;
            case 1 -> config.seasonDrainSummer;
            case 2 -> config.seasonDrainAutumn;
            default -> config.seasonDrainWinter;
        };
    }
}
