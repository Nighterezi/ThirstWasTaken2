# compat/

Optional integrations. **No hard dependency ever lands here**: gate on `Loader.isModLoaded`,
keep the touching classes off the load path, and let the mod run identically with none of them
installed.

Note that most third-party support is *not* in this package — food and drink values resolve by
registry id in `ThirstConfig`, and container detection resolves by namespace in `WaterPurity.resolve`.
Neither references a foreign class, so neither needs a gate. Prefer that approach.

## LootIntegration

`Loader.onLootTable` replacing the original's Forge global loot modifiers (Fabric's
`LootTableEvents.MODIFY` underneath). One extra pool is appended to five vanilla chest tables
(`SIMPLE_DUNGEON`, `ABANDONED_MINESHAFT`, `SHIPWRECK_SUPPLY`, `NETHER_BRIDGE`, `BASTION_OTHER`) and to
`PIGLIN_BARTERING`, with different weights for each case.

The pool is added to a table with one of those ids whoever wrote it, a data pack's replacement
included. The mod used to skip replaced tables, but a loader cannot tell a player's pack from one of
vanilla's own experiment packs, and Fabric API changed its answer between versions: with the Villager
Trade Rebalance experiment on, mineshaft chests held water on 1.21.1 and none on later versions. A
few water bottles are an addition any pack can live with; a rule that differs per version is not.

Water bottles are emitted as `minecraft:potion` + `SetPotionFunction` + a `SetComponentsFunction` that
stamps `water_purity`, so loot water arrives already stamped rather than falling back to
`defaultPurity`.
