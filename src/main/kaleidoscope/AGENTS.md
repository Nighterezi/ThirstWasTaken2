# src/main/kaleidoscope — water quality in Kaleidoscope Cookery's stockpot and teapot

[Kaleidoscope Cookery](https://modrinth.com/mod/kaleidoscope-cookery) (mod id `kaleidoscope_cookery`)
keeps water in two blocks, the stockpot and the teapot, and both keep only a fluid id, so the grade a
bucket had is lost on the way in. This directory is where that gets fixed. The plan, the order of work
and what is still to do are in
[docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md](../../../docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md).

**Built on `1.21.1-neoforge` only, for now.** Its row in the integration table names NeoForge alone,
so no Fabric node compiles this directory, although nothing here names a loader. What turning Fabric
on takes is in the plan. What it does there:

- **the stockpot and the teapot keep the grade** of the water poured in, and hand it back on the
  bucket taken out;
- **a teapot picked up and placed again** keeps it, in the item's block entity data;
- **an empty teapot item dipped into water** samples it where it lies, as a bucket does;
- **dripstone** fills a teapot with `dripstonePurity` water, as it does a cauldron;
- **sea water**: the teapot refuses it, from a bucket or from the world, since tea brewed from it
  comes out safe; the stockpot takes it and hands it back salty;
- and **Jade** names the grade under the crosshair.

The drink and soup values are not here. They are ids in `ThirstConfig`, common code that names no class
of the mod, so they reach every node, including those that do not compile this directory and a player
on the unsupported official Fabric build.

## Which build

| Node | Mod | Modrinth project |
|---|---|---|
| `1.21.1-neoforge` | the official mod | `kaleidoscope-cookery` |
| `1.21.1`, `1.21.11`, `26.1.x`, `26.2.x`, `26.3.x` | Refabricated, the Fabric port | `kaleidoscope-cookery-refabricated` |

The Fabric nodes pin it and run it in `runClient`, but do not compile this directory yet.

The official mod has no NeoForge build past 1.21.1, so the other NeoForge nodes do not set the key and
do not compile this directory. The official Fabric build stopped at 1.0.1, before the teapot, and is not
supported. Refabricated has the same mod id and the same package, so one directory serves both, and
since it names no loader and no fluid API, **both loaders are meant to compile it**, as with
[src/main/supplementaries](../supplementaries/AGENTS.md). Keep it that way while only NeoForge does:
`checkLoaderSeam` does not hold it to that until Fabric is back in its row. Each key is pinned by Modrinth version id; the
Fabric uploads of different Minecraft versions share one version number. `1.21.11` is frozen upstream at
1.3.0.9, and `update_mc_deps.py` leaves it alone.

On `1.21.1` and `1.21.11` Refabricated requires Forge Config API Port, `deps.forge_config_api_port`, on
the `runClient` classpath only. Its Night Config is nested in its jar, which Loom does not unpack into a
run, so `nestedMods` in `build.gradle.kts` takes it out, as it does Moonlight's CodecUI. From 26.1 on it
is optional and no table names it.

```
kaleidoscope/java/com/thirstwastaken2/kaleidoscope/
  KaleidoscopePresence       the gate: a classpath probe for the teapot, then one per mixin target
  KaleidoscopeMixinPlugin    applies each mixin only where the gate allows it
  BrewedWater                the mod's own interface on both block entities: the grade they hold now
  BrewedWaterQuality         the only place that reads a grade off what goes in, and stores it
  ReturnedWater              the grade on its way back out, per thread, for one remove call
  mixin/StockpotBlockEntityMixin   addSoupBase, removeSoupBase, save and load
  mixin/TeapotBlockEntityMixin     addTeaFluid (and the salt refusal), removeTeaFluid, getDrops, save and load
  mixin/TeapotDripstoneMixin       receiveDripstoneFluid, which only the 1.21.1 builds have
  mixin/TeapotItemMixin            an empty teapot scooping world water
  mixin/ItemUtilsMixin             stamps the bucket both blocks hand back
kaleidoscope/resources/
  thirstwastaken2.kaleidoscope.mixins.json
../../client/kaleidoscope/java/com/thirstwastaken2/client/kaleidoscope/
  KaleidoscopeJade           adds a reader to the mod's own Jade plugin
```

## How a grade moves

Both blocks keep a fluid id and nothing else, and both build the bucket they hand back from nothing,
so the grade lives in a `@Unique` field on the block entity, saved as one int under
`thirstwastaken2:water_quality` (`WaterPurity.storedValue`: 1 to 4 for the grades, 5 for sea water).

- **Read before the call, never after.** `addSoupBase` shrinks the bucket and `addTeaFluid` empties
  it, so the grade is read off the stack first and kept only when the call returns true.
- **The field is only trusted while the block holds water**: the stockpot in `PUT_INGREDIENT` with
  water as its soup base, the teapot in `PUT_INGREDIENT` with water as its tea fluid.
  `thirst$heldWater` answers null otherwise, which is what is saved, what Jade shows and what a remove
  call stamps. Every way into that state writes the field again, so a stale value is never read and
  nothing needs clearing when the soup is served.
- **Out through `ItemUtils.getItemToLivingEntity`.** Both remove calls end there, the teapot's
  through `FluidUtils.fillItem`. `ReturnedWater.during` holds the grade for the length of the call and
  restores what was there however it ends. It is a `ThreadLocal` because both blocks run their calls
  on the client too, and in single player the two threads run them at once.
- **Stamped through `WaterPurity.setQuality`**, so fresh water gets `water_salty: false` and sea water
  its sprite. A block filled before the integration existed has no grade and hands back a plain
  bucket, which reads as `defaultPurity`.

## How it stays optional

The same three layers as Supplementaries.

1. **Build.** Only when `deps.kaleidoscope_cookery` is set and the loader is in its row, the loader script adds this directory and
   append the mixin config to the built manifest (on NeoForge with `kaleidoscope_cookery` as an optional
   dependency), from [its row in the integration table](../../../build-logic/src/main/kotlin/com/thirstwastaken2/buildlogic/Integrations.kt). Only NeoForge compiles it for
   now; once Fabric does too, `checkLoaderSeam` keeps it free of either loader's API. Fabric needs it remapped, since it is mixed into, so it
   is `modCompileOnly`; NeoForge takes it as `compileOnly`. Both put it on `runClient` only, so
   `runServer` and the gametests run without it, and `-PwithoutOptional=kaleidoscope_cookery` (or
   `kaleidoscope-cookery`, `kaleidoscope-cookery-refabricated`) leaves it out of `runClient` as well.
2. **Runtime gate.** `KaleidoscopePresence` answers every question with a resource lookup, which never
   loads a class, and names no class of the mod's, no Minecraft class and no loader. The teapot's class
   is the version check: the official Fabric 1.0.1 has the same mod id and package but no teapot, so
   without it the whole integration is off and one warning says to install Refabricated. Past that,
   each target is probed by name, and a missing one logs once and is skipped.
3. **Mixin plugin.** `KaleidoscopeMixinPlugin.shouldApplyMixin` asks the gate for the mixin's own
   target, so a class renamed upstream only takes its own mixins down. `onLoad` asks too, so the warning
   about an unsupported build is logged at startup whatever the config lists.

Mixins here match their targets **by name alone, with no descriptor**, as in Supplementaries, so it
does not matter that the Fabric jar uses intermediary names for Minecraft types.

## Checking it

- `checkOptionalSeam` finds the plugin, the gate and `KaleidoscopeJade` as classes loaded without the
  mod, and passes.
- `runGametest` passes unchanged on every node: the mod is never on its classpath.
- `./gradlew ":<node>:runClient" -Pagent=tools/agent/boot.jsonl`, with the mod and with
  `-PwithoutOptional=kaleidoscope_cookery` (Jade still there, the 1.0.9 shape) and
  `kaleidoscope_cookery,jade`, comes up and stays up.
- What it does is checked in a real client with
  [tools/agent/kaleidoscope-cookery.jsonl](../../../tools/agent/kaleidoscope-cookery.jsonl), whose
  header says how to run and verify it. Every `execute` line asserts its own "Test passed". It covers
  a Dirty bucket through each block, sea water through the stockpot and refused by the teapot, a
  teapot of Murky water picked up, placed and emptied, a teapot item dipped in a swamp and in the sea,
  and dripstone, and leaves two Jade screenshots. It passed on `1.21.1-neoforge` on 2026-09-22.
