# src/gametest — automated in-game tests

Server-side tests that run in a real Minecraft server with real registries, real items and a real
player, headlessly and in a few seconds.

```bash
./gradlew ":26.2.x:runGametest"
```

CI runs this for every supported version on every push. A failing test fails the build.

## What this is

GameTest is Mojang's own framework, shipped inside Minecraft
(`net.minecraft.gametest.framework`), and `fabric-gametest-api-v1` is a thin wrapper that finds
annotated methods and reports results. The runner replaces the dedicated server entirely: it skips
the EULA prompt, never opens a window, places each test in its own patch of a scratch world, then
exits non-zero if any required test failed and writes a JUnit XML report to
`versions/<version>/build/gametest/report.xml`.

This is an ordinary Gradle source set, not part of the mod. `thirstwastaken2-gametest` is its own
small mod declared in `src/gametest/resources/fabric.mod.json`, so none of it can reach a published
jar. Verify that with `unzip -l build/libs/<jar> | grep gametest` after a release build.

## Writing a test

- Test classes are listed under the `fabric-gametest` entrypoint. A class that is not listed is
  silently never run.
- A test is a `public`, non-static method taking one `GameTestHelper`, annotated `@GameTest`, ending
  in `helper.succeed()`.
- `helper.assertTrue` only accepts a `Component` on every supported version. Use
  `TestFixtures.check`, which takes a plain string.
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
- **Only assert what the config makes deterministic.** Purity tiers 0 and 3 have nausea chances of
  100 and 0 in the default config and are safe to assert. Tiers 1 and 2 are dice rolls and are
  deliberately left alone.

## What is covered

| Class | Covers |
|---|---|
| `WaterFillingGameTest` | bottle and bucket filling, that each fill resamples the water, that an abandoned fill leaves nothing behind |
| `WaterEffectsGameTest` | salt water, dirty water, purified water, drinking, purity surviving boiling |
| `HealthRegenGameTest` | dehydration halting regeneration and the food refund that has to accompany it |
| `WaterskinGameTest` | mixing, salinity, capacity, emptying |
| `TooltipGameTest` | the lines the mod adds to a tooltip, droplet row arithmetic, and that cached lines are handed out as copies |
| `PlayerStateGameTest` | the sprint gate, exhaustion mirroring waiting for the tick, small exhaustion being carried until it crosses a sync step, the Hunger effect cancelling out, and that riding does not dehydrate |
| `CauldronGameTest` | the cauldron blockstate properties (water cauldron only, old powder snow saves still load) and the deferred quality transfer |
| `EnvironmentGameTest` | the datapack damage type and tag, and the version-forked environment call |
| `CreativeTabGameTest` | the creative tab is registered, has the right icon, and holds every item the mod adds |

## Known limits of the harness

A mock player reports `GameType.CREATIVE` and ignores `setGameMode`, and vanilla refuses to damage a
creative player. Anything whose outcome is vanilla applying damage cannot be asserted here; assert
that the mod registered the right thing instead. `EnvironmentGameTest` checks that the dehydration
damage type and its `bypasses_armor` entry loaded, and leaves losing health to a manual check.

Its abilities are survival-like even so, which is why the filling tests behave normally.

Client rendering is not covered, and neither is anything on a timer: dehydration damage, peaceful
regeneration and the faster Nether drain all stay manual. `fabric-client-gametest-api-v1` could cover
the rendering, but it needs a real window and screenshot baselines maintained per Minecraft version.

Three of the four shipped version forks are client-side, so the manual pass that matters after a HUD
change is the thirst bar, the tooltip glyphs, F1 and the config screen, on each version.
