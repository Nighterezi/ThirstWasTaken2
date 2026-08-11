# Water Quality

Every collected container stores the water's contamination and whether it is salty. The tooltip and
item sprite summarize contamination as one of four familiar grades.

| Contamination | Grade |
|---:|---|
| 0 to 15 | Purified |
| 16 to 35 | Acceptable |
| 36 to 65 | Slightly dirty |
| 66 to 100 | Dirty |

## Sampling a source

Water is sampled once, when it is collected or drunk directly. The mod does not scan water every
tick. Biome tags choose the baseline, then a few local conditions make small adjustments.

| Source | Baseline contamination | Typical grade |
|---|---:|---|
| Ocean or beach | 25, salty | Acceptable, but not drinkable |
| Swamp or mangrove swamp | 85 | Dirty |
| River | 42 | Slightly dirty |
| Mountain | 28 | Acceptable |
| Jungle, savanna or badlands | 70 | Dirty |
| Other biomes | 55 | Slightly dirty |

Very hot biomes add 10 contamination and very cold biomes remove 10. Water above y 100 or below y
32 removes 5. Flowing water removes only 5, so a waterfall is not automatically safe. Mud,
mangrove roots, farmland or a composter within two blocks can add contamination.

Modpacks can add biomes to `thirstwastaken2:stagnant_water` without changing code. A container that
has no sampled quality, such as an unknown modded drink, still uses `defaultPurity`.

## Salt water

Salinity is separate from cleanliness. Ocean water can look acceptable while still being unsafe to
drink. A salty drink restores no thirst, adds thirst exhaustion and causes five seconds of Nausea.
Furnaces, campfires and sand filters do not remove salt.

## Mixing and cauldrons

A waterskin mixes contamination by the number of servings already inside it. If either side is
dirty, the result receives 10 extra contamination, so one clean serving cannot cheaply neutralize a
dirty batch. Adding any salt water makes the mixed waterskin salty.

Cauldrons retain the worse grade when water is poured together and remember salinity. Water drawn
back into a bottle, bucket or waterskin keeps that stored quality.

## Drinking contaminated water

Contaminated fresh water still hydrates you. The existing sickness roll remains unchanged.

| Grade | Nausea and Hunger | Poison |
|---|---|---|
| Dirty | 100% | 30% |
| Slightly dirty | 50% | 10% |
| Acceptable | 5% | none |
| Purified | none | none |

Nausea lasts five seconds, Hunger lasts thirty seconds and Poison lasts ten seconds. A longer-term
infection system is not part of this release.

## Cleaning fresh water

Put a fresh water bottle, terracotta water bowl or water bucket in a furnace or on a campfire.

| In | Out |
|---|---|
| Dirty | Acceptable |
| Slightly dirty | Purified |
| Acceptable | Purified |

A furnace takes ten seconds and a campfire takes thirty. Dirty water needs two passes to become
purified. Salty containers are rejected instead of being silently desalinated.

### Sand filter

::: warning Not available yet
The Sand Filter's Create Fly integration is still disabled. The behavior below applies when that
integration returns.
:::

The filter improves fresh or salty water by one grade but preserves salinity. It cannot turn ocean
water into drinking water.
