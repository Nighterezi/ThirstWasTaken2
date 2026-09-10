# compat/

Optional integrations. **No hard dependency ever lands here**: gate on `FabricLoader.isModLoaded`,
keep the touching classes off the load path, and let the mod run identically with none of them
installed.

Note that most third-party support is *not* in this package — food and drink values resolve by
registry id in `ThirstConfig`, and container detection resolves by namespace in `WaterPurity.resolve`.
Neither references a foreign class, so neither needs a gate. Prefer that approach.

## LootIntegration

Fabric's `LootTableEvents.MODIFY` replacing the original's Forge global loot modifiers. One extra pool
is appended to five vanilla chest tables (`SIMPLE_DUNGEON`, `ABANDONED_MINESHAFT`, `SHIPWRECK_SUPPLY`,
`NETHER_BRIDGE`, `BASTION_OTHER`) and to `PIGLIN_BARTERING`, with different weights for each case. The
`source.isBuiltin()` check keeps datapack overrides of those tables untouched.

Water bottles are emitted as `minecraft:potion` + `SetPotionFunction` + a `SetComponentsFunction` that
stamps `water_purity`, so loot water arrives already stamped rather than falling back to
`defaultPurity`.
