# src/gametest — automated in-game tests

Server-side tests that run in a real Minecraft server with real registries, real items and a real
player, headlessly and in a few seconds.

```bash
./gradlew ":26.2.x:runGametest"
```

```bash
./gradlew ":1.21.1-neoforge:runGametest"
```

CI runs this for every node, NeoForge included, on every push. A failing test fails the build.

## What this is

GameTest is Mojang's own framework, shipped inside Minecraft
(`net.minecraft.gametest.framework`), and `fabric-gametest-api-v1` is a thin wrapper that finds
annotated methods and reports results. The runner replaces the dedicated server entirely: it skips
the EULA prompt, never opens a window, places each test in its own patch of a scratch world, then
exits non-zero if any required test failed and writes a JUnit XML report to
`versions/<version>/build/gametest/report.xml`.

This is an ordinary Gradle source set, not part of the mod. `thirstwastaken2_gametest` is its own
small mod declared in `src/gametest/resources/fabric.mod.json`, and on NeoForge also in
`src/gametest/neoforge/resources/META-INF/neoforge.mods.toml`, so none of it can reach a published
jar. Verify that with `unzip -l build/libs/<jar> | grep gametest` after a release build. The id has an
underscore rather than a hyphen because NeoForge mod ids cannot contain one.

## Writing a test

- Test classes are listed under the `fabric-gametest` entrypoint. A class that is not listed is
  silently never run, on either loader: the NeoForge harness reads the same list.
- A test is a `public`, non-static method taking one `GameTestHelper`, annotated `@GameTest`, ending
  in `helper.succeed()`.
- `helper.assertTrue` takes a `Component` from 1.21.5 and a string before it. Use
  `TestFixtures.check`, which takes a plain string on every version.
- Positions passed to `helper` are relative to the test's own patch of world. Positions passed to
  mod or vanilla code are absolute. `helper.absolutePos` converts; mixing them up is the easiest
  mistake to make here.
- A test body with no sequence runs inside a single tick. That is why `TestFixtures.water` can place
  a water source and sample it immediately: fluid spreading is scheduled, not immediate.
- Drive interactions through `player.gameMode.useItem(...)` rather than calling `ItemStack.use`
  directly. Only the game-mode path applies the result back to the player, which is what the fill
  hooks depend on.
- Use survival mode for anything that fills a container. `ItemUtils.createFilledResult` behaves
  differently once the player has infinite materials.

## The 1.21.1 harness

Fabric API's own `@GameTest` arrived with 1.21.5. On 1.21.1 a test uses vanilla's annotation with
Fabric's empty structure as its template, and `stonecutter.gradle.kts` rewrites both the import and
the annotation, so no test file changes for it. Write `@GameTest` with no arguments, or that
replacement stops matching.

The same 124 tests run on every node. Later nodes report 125 because their runner adds vanilla's own
`minecraft:always_pass`; that one is not the mod's.

## The NeoForge harness

The `-neoforge` nodes run the same 124 test methods, with no test body changed and no NeoForge-only
branch in any of them. What stands in for Fabric API lives in `src/gametest/neoforge`:

| | Fabric API | NeoForge node |
|---|---|---|
| Annotation | `net.fabricmc.fabric.api.gametest.v1.GameTest` | `com.thirstwastaken2.gametest.neoforge.GameTest`, no arguments. `stonecutter.gradle.kts` swaps the import on `-neoforge` nodes only |
| Discovery | the `fabric-gametest` entrypoints | `ThirstWasTaken2GameTests` reads the same entrypoint list out of this mod's `fabric.mod.json` as plain JSON |
| Registration | `TEST_FUNCTION` at init, test instances when the dynamic registries load | `RegisterEvent` for `TEST_FUNCTION`, `RegisterGameTestsEvent` for the instances |
| Ids | `thirstwastaken2_gametest:<class>_<method>`, snake case | the same rule, so the report names match |
| Defaults | an empty 8x8x8 structure, `minecraft:default`, 20 ticks, required, padding 1 | the same values. The empty structure is this mod's own `data/thirstwastaken2_gametest/structure/empty.nbt`, and the environment an empty one registered as `thirstwastaken2_gametest:default`, the same definition as vanilla's, which the event cannot look up |
| Runner | `-Dfabric-api.gametest` on the server run | ModDevGradle's `gameTestServer` run type |
| Report | `fabric-api.gametest.report-file` | vanilla's `--report`, to the same `versions/<node>/build/gametest/report.xml` |

On 1.21.1 (`1.21.1-neoforge`) there are no test registries, environments or padding yet. The harness
adds a vanilla `TestFunction` per method to `GameTestRegistry.getAllTestFunctions()` from
`RegisterGameTestsEvent`, in `defaultBatch`, with the same structure, ticks and id; it bypasses
NeoForge's own registration, which only accepts vanilla's annotation and takes the structure's
namespace from a NeoForge annotation on the test class. That server has no `--report` option either,
so the build passes the report path as `-Dthirstwastaken2.gametest.report` and the harness installs
vanilla's `JUnitLikeTestReporter` itself. The report lists 124 tests there, without
`minecraft:always_pass`.

On 1.21.11 `TestData` has no padding and `TestEnvironmentDefinition` takes no type parameter; the
harness leaves padding out before 26.1 and holds both values in `var`s.

Two things that differ underneath and have not mattered to any test so far:

- Test classes are instantiated on their first test, not while the mod loads: they keep the mod's
  items in static fields, and NeoForge constructs mods before anything may register.
- Fabric API makes the test server report itself as a dedicated server; NeoForge leaves vanilla's
  `false`. Commands are still registered for a dedicated server on both.

After changing the harness, break the NeoForge `Loader` on purpose and watch the NeoForge nodes alone go red.
Skipping `onUseItem` there fails the bowl and waterskin scooping tests; skipping `onUseBlock` fails the
cauldron bottle draw.

## Rules that keep these tests worth having

- **Every test must be able to fail.** After writing one, break the code it covers on purpose and
  confirm the test goes red. A test that passes against broken code is worse than no test, because
  it reports safety that is not there.
- **Assert against a freshly computed expectation, not a hard-coded number**, wherever the value
  depends on the world. Water quality is sampled from the biome and surroundings, so
  `WaterPurity.sampleAt` is the reference, not a literal.
- **Pair a negative assertion with a positive control.** `dehydratedPlayerDoesNotRegenerate` only
  means something next to `hydratedPlayerStillRegenerates`; without it the first would pass even if
  regeneration never triggered.
- **Datapack behaviour needs a test, because the compiler has no opinion about it.** The 18
  purification recipes match on components from a JSON file; nothing fails to build when a container
  stops carrying what they look for. `PurificationGameTest` asks the real recipe manager instead.
- **Only assert what the config makes deterministic.** Purity tiers 0 and 3 have nausea chances of
  100 and 0 in the default config and are safe to assert. Tiers 1 and 2 are dice rolls and are
  deliberately left alone.

## What is covered

| Class | Covers |
|---|---|
| `WaterFillingGameTest` | bottle and bucket filling, that each fill resamples the water, that an abandoned fill leaves nothing behind |
| `WaterEffectsGameTest` | salt water, dirty water, purified water, drinking, boiling not desalinating |
| `HealthRegenGameTest` | dehydration halting regeneration and the food refund that has to accompany it |
| `WaterskinGameTest` | mixing, salinity, capacity, emptying |
| `TooltipGameTest` | the lines the mod adds to a tooltip, droplet row arithmetic, that the rows need AppleSkin, and that cached lines are handed out as copies |
| `PlayerStateGameTest` | the sprint gate, exhaustion mirroring waiting for the tick, small exhaustion being carried until it crosses a sync step, the Hunger effect cancelling out, and that riding does not dehydrate |
| `CauldronGameTest` | the cauldron blockstate property (water cauldron only, old powder snow saves still load, a fresh cauldron is not sea water), the deferred quality transfer, and the grades rain and dripstone leave behind |
| `PurificationGameTest` | which water the furnace accepts: looted bottles yes, salt water never |
| `EnvironmentGameTest` | the datapack damage type and tag, and the version-forked environment call |
| `CreativeTabGameTest` | the creative tab is registered, has the right icon, and holds every item the mod adds |
| `AdvancementGameTest` | the mod's advancement tab loads, hangs off one root, its recipe advancements unlock recipes that exist, and the Cooking Pot files are skipped without Farmer's Delight |
| `ThirstDataGameTest` | the state record: drinking, the quenched cap, overflow into quenched, spending exhaustion, clamping, both codecs round-tripping, including a save from before `enabled` existed, and a whole player saved to NBT and loaded back, which is the attachment under the codec |
| `ThirstTickGameTest` | the tick spending quenched before thirst, peaceful with and without depletion, disabled and invulnerable players, Fire Resistance and Fire Protection slowing the drain, salt water charging at once, the full-bar rule |
| `DrinkingGameTest` | drinking a bowl and a waterskin through the real right-click path, what is left in the hand, the drink animation and duration, water refused on a full bar while honey is not, the advancements a drink earns, and drinking by hand with every way it is refused |
| `ThirstApiGameTest` | what items restore from the config, the blacklist, the `c:drinks` tag fallback (the gametest mod tags a nautilus shell for it) and the magic drinks it leaves out, keyword matching and its blacklist, the per-item cache dropping on commit, and `sanitize` clamping a hand-edited config |
| `CommandGameTest` | `/thirst set` and `/thirst enable` through the dispatcher, the argument range, and the permission requirement |
| `WaterInteractionsGameTest` | scooping with the bowl and the waterskin, the clay bowl holding nothing, drawing the waterskin from a cauldron, pouring it out, and a bottle drawn from a cauldron keeping its grade |
| `LootGameTest` | graded water in each seeded chest and in piglin bartering, no water anywhere else, and a table a data pack replaced still getting it |
| `ItemAppearanceGameTest` | the custom model data bowls and waterskins dispatch on, the sea-water item model (1.21.2 and later), and the waterskin bar's width and colour |

`WaterskinGameTest` also covers pouring a bottle or bucket into a slotted waterskin from the cursor,
and `PurificationGameTest` the grade every boiling recipe produces and the crafted water bowl.

What none of them can reach - the client, damage to a player, other dimensions and biomes, real
time - is in [docs/dev/MANUAL-TESTING.md](../../../docs/dev/MANUAL-TESTING.md), with a section per
version.

The test server enables every experiment, including the Villager Trade Rebalance data pack, which
replaces the mineshaft chest. `LootGameTest` relies on that to test a replaced table, and checks the
pack is enabled first so that a server that stops enabling it fails loudly instead of proving nothing.

## Known limits of the harness

A mock player reports `GameType.CREATIVE` and ignores `setGameMode`, and vanilla refuses to damage a
creative player. Anything whose outcome is vanilla applying damage cannot be asserted here; assert
that the mod registered the right thing instead. `EnvironmentGameTest` checks that the dehydration
damage type and its `bypasses_armor` entry loaded, and leaves losing health to a manual check.

Its abilities are survival-like even so, which is why the filling tests behave normally.

Client rendering is not covered, and neither is anything on a timer: dehydration damage, peaceful
regeneration and the faster Nether drain all stay manual. `fabric-client-gametest-api-v1` could cover
the rendering, but it needs a real window and screenshot baselines maintained per Minecraft version.

Much of what forks per version is client-side, so the manual pass that matters after a HUD change is
the thirst bar (a mixin of its own on 1.21.1), the tooltip glyphs, F1 and the config screen, on each
version.
