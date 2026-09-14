# compat/

Optional integrations. **No hard dependency ever lands here**: gate on `Loader.isModLoaded`,
keep the touching classes off the load path, and let the mod run identically with none of them
installed.

Note that most third-party support is *not* in this package — food and drink values resolve by
registry id in `ThirstConfig`, and container detection resolves by namespace in `WaterPurity.resolve`.
Neither references a foreign class, so neither needs a gate. Prefer that approach.

## AppleSkin

`AppleSkin` is the presence check common code can ask without touching an AppleSkin class, and the two
settings the mod keeps for it. The quenched outline on the thirst bar and the tooltip droplet rows are
the thirst half of what AppleSkin adds for hunger, so both are only shown while it is loaded. Reading
AppleSkin's own config names its classes, so that part stays client-side in
`client/compat/AppleSkinIntegration`, which is only reached after `AppleSkin.isLoaded()`.

## Jade

`client/compat/JadeIntegration` (client source set, since Jade's tooltip API is client code) shows
the grade of the water under the crosshair: world water, waterlogged blocks and water cauldrons. It is
reached only through the `jade` entrypoint in `fabric.mod.json`, which Jade resolves on the dedicated
server too, so the plugin class itself names no client class outside `registerClient`. Jade toggles it
under `config.jade.plugin_thirstwastaken2.water_purity`. Why client-side sampling is safe is in
[purity/AGENTS.md](../purity/AGENTS.md).

The part of the Jade API it uses is identical on every supported version, so it carries no version
branch. A water cauldron nothing was poured into shows the client's `defaultPurity`, the same
limitation an unstamped item tooltip already has on a server with a different config.

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
