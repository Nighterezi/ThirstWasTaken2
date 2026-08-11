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

## createfly/ — currently broken and off

The Create Fly Sand Filter. **The integration does not work and is documented as unsupported.** The
code is intact and still gated, so the shipped behaviour is simply "block never registers". Turning it
back on means fixing it *and* reverting the notes in `FORK-STATUS.md`, `README.md` and the docs site
in the same change.

Two gates, both required:

1. `FabricLoader.isModLoaded("create")` — necessary but not sufficient, any mod may claim that id.
2. A marker-class probe for `com.zurrtum.create.foundation.blockEntity.SmartBlockEntity`, since
   `SandFilterBlockEntity` extends a Create class and would fail to link against a different mod.

`CreateFlyIntegration.isAvailable()` runs both. **Nothing in this package may be referenced unless it
returned true** — that includes touching `CreateFlyIntegration.sandFilter()` from elsewhere. The
Create Fly artifact is `compileOnly` in `build.gradle`, so a stray reference from common code compiles
and then crashes at runtime for everyone without Create installed.

Registration adds the block, its `BlockItem`, the block entity type and a creative-tab entry via
`CreativeModeTabEvents.modifyOutputEvent`. The matching recipe
(`data/thirstwastaken2/recipe/compat_create_sand_filter.json`) carries its own
`fabric:load_conditions`, so it stays inert independently of this class.
