# Cold Sweat integration plan

What ThirstWasTaken2 should do with [Cold Sweat](https://modrinth.com/mod/cold-sweat) (mod id
`cold_sweat`, package `com.momosoftworks.coldsweat`), the body temperature mod by Mikul. This file sets
the order of work, what each step needs and how each one is checked. Once the work is built, how it
works goes in `src/main/coldsweat/AGENTS.md`.

Written on 2026-09-25 from:

- the repository [Momo-Softworks/Cold-Sweat](https://github.com/Momo-Softworks/Cold-Sweat), branch
  `1.21-FG` at `52d01d3` (2026-09-15), NeoForge `21.1.234`, `neo_version_range=[21.1.181,)`;
- the Modrinth project `cold-sweat`, whose newest 1.21.1 upload is `2.4.3.1` (`r2cD4Llq`,
  2026-09-15). It has **no required dependency**.

Nothing below has been run yet. Every "it does X" about Cold Sweat is read from the source, and step 3
exists to confirm it in a game.

## Which build for which node

Cold Sweat is **NeoForge only on 1.21.1** (Forge for 1.20.1 and older, nothing newer than 1.21.1, no
Fabric build). So one node:

| Node | Build | Modrinth id | Requires on `runClient` |
|---|---|---|---|
| `1.21.1-neoforge` | `2.4.3.1` | `r2cD4Llq` | nothing |
| every other node | none | — | — |

The row gets `loaders = setOf(Loader.NEOFORGE)`, like Create. On NeoForge 1.21.1 the jar is in
Mojang names, so mixins name its members directly, with no `remap = false`.

## What Cold Sweat already does about thirst, and why none of it reaches us

Cold Sweat has a built-in integration with a thirst mod, but it is keyed on the mod id **`thirst`**
(`CompatManager.THIRST_LOADED = modLoaded("thirst", "1.21.1-3.0.0")`) and imports
`cn.mlus.thirst.*`: the other 1.21 port of upstream Thirst Was Taken. Our id is `thirstwastaken2`, so
all of it is dead code with our mod installed:

| Cold Sweat code | With `thirst` | With us, today |
|---|---|---|
| `WaterskinItem.getFilledItem` | stamps `thirst:purity` from `WaterPurity.getBlockPurity` | the filled waterskin carries no grade |
| `BoilerBlockEntity.tick` | every 200 ticks (÷ `TEMP_RATE`), +1 purity on each drinkable in slots 1-9, up to 3 | purifies nothing |
| `BoilerContainer` / `canPlaceItem` | accepts any stack with `thirst:purity` | accepts only `#cold_sweat:boiler_valid` |
| `FilledWaterskinItem.getCraftingRemainingItem` | strips `thirst:purity` from the empty waterskin | would copy **our** components onto the empty skin (it `applyComponents` everything) |
| `recipe/compat/thirst/*.json` | furnace and campfire recipes raise the waterskin's purity | not loaded (`neoforge:mod_loaded` `thirst`) |
| `RegisterThirstValueEvent` listener | registers the waterskin as a container with purity | not fired |

We do not ask Cold Sweat to change this. We reproduce what makes sense from our side, in our own
integration, because our grades are `thirstwastaken2:water_purity` + `water_salty`, a different
component from `thirst:purity`.

## Where water and heat live in Cold Sweat

| Thing | What it does | What it means for us |
|---|---|---|
| **Waterskin** (`cold_sweat:waterskin` → `cold_sweat:filled_waterskin`) | filled from water in the world; carries `cold_sweat:water_temperature` (-50..50) set from the world temperature where it was filled; used by **pouring** on yourself (default use) or **drinking** (crouch, per player `Preference`), both change body temperature; has durability = sips; can fill a cauldron by one layer | a second "waterskin" in the game next to ours. Drinking it restores no thirst today; its water has no grade |
| **Boiler** | heats filled waterskins in slots 1-9 up to 50 | the natural place to purify water, as Cold Sweat meant it |
| **Icebox** | cools filled waterskins down to -50 | nothing about grade |
| **Hearth** | takes water (bucket or fluid capability) as cooling fuel, lava as heating fuel | a graded bucket is still `minecraft:water_bucket`; to confirm in step 3 that the fuel check does not compare components |
| **Temperature API** (`api.util.Temperature`) | `Temperature.get(entity, Trait.WORLD)` ambient temperature at the player in MC units (biome, time, hearths, shade, elevation); `Trait.BODY` -150..150, damage at ±100 | the climate input step 2 wants |
| **Food temperatures** (`data/<ns>/cold_sweat/item/food/*.json`, registry `cold_sweat:item/food`, `FoodData` codec) | an item consumed changes body temperature, optionally for a duration | data only, no class reference: our drinks can warm or cool (step 7) |

Mixins that meet ours:

- Both mods mixin `FoodData.tick`: Cold Sweat `@Redirect`s the `GameRules.getBoolean` call (limits
  natural regeneration at extreme temperatures), ours `@Redirect`s the two `heal` calls. Different
  targets, so they apply together; step 3 checks that both limits hold at once.
- Cold Sweat's `MixinCampfire` changes `CampfireBlockEntity.cookTick`; our canteen and flask boil in
  hand over a lit campfire through `WaterskinItem.useOn`, not the block entity. Expected independent;
  step 3 checks it.
- Cold Sweat draws its body temperature gauge above the hotbar (`MixinXPBar`, `MixinHeartRender`).
  Step 3 checks our bar does not overlap it, at every `hudPosition`.

## Status

| # | Item | Kind | Nodes | Status |
|---|---|---|---|---|
| 1 | Build dependency and gate | build | `1.21.1-neoforge` | Planned |
| 2 | Climate: Cold Sweat's temperature drives the thirst drain | feature | one | Planned, decision 2a |
| 3 | What actually happens today: waterskin, boiler, hearth, mixins, HUD | investigation | one | Planned |
| 4 | Cold Sweat's waterskin carries our grade: filled, drunk, poured, crafted | feature | one | Planned |
| 5 | Drinking Cold Sweat's waterskin restores thirst | feature | one | Planned |
| 6 | The Boiler purifies | feature | one | Planned, decision 6a |
| 7 | Our drinks change body temperature | data | one | Planned, decision 7a |
| 8 | Thirst and heat: does being parched hurt heat tolerance? | decision | one | Planned |
| 9 | Changelog and player docs | docs | — | Planned |
| 10 | Nothing crashes without the mod: `checkOptionalSeam`, `-PwithoutOptional`, `boot.jsonl` | test | all | Planned, again after every step |

## 1. Build dependency and gate

- A row at the end of [the integration table](../../../build-logic/src/main/kotlin/com/thirstwastaken2/buildlogic/Integrations.kt):
  `dir = "coldsweat"`, `depsKey = "deps.cold_sweat"`, `loaders = setOf(Loader.NEOFORGE)`, mixin config
  `thirstwastaken2.coldsweat.mixins.json`, `neoForgeDependencies = listOf("cold_sweat")`. No client
  directory until a step needs one.
- `deps.cold_sweat = "r2cD4Llq"` in `[neoforge."1.21.1"]` of `stonecutter.properties.toml`, with the
  comment the other pins have (`# 2.4.3.1, the NeoForge upload.`), and `cold-sweat` in `MODRINTH_DEPS`
  of `.github/scripts/update_mc_deps.py`.
- `compileOnly` and `runClientMod` lines in `build.neoforge.gradle.kts`.
- `ColdSweatPresence` (the `Loader.isModLoaded("cold_sweat")` gate, plus a marker-class probe on
  `com.momosoftworks.coldsweat.api.util.Temperature`) and `ColdSweatMixinPlugin`, as the other
  integrations have. The entry class names no Cold Sweat type, lambdas included, and hands over through
  a static call.

**Check:** `:1.21.1-neoforge:build`, `checkOptionalSeam`, `checkLoaderSeam`, `runServer` prints the
init line with and without the mod, `runGametest`, and `boot.jsonl` with
`-PwithoutOptional=cold_sweat`.

## 2. Climate: Cold Sweat's temperature drives the drain

Today `ThirstManager.climateModifier` reads the **biome's base temperature** and precipitation. With
Cold Sweat, the player's real surroundings are known: a hearth in a desert, shade, night, altitude.
Options:

- **(a) Use `Trait.WORLD` in place of the biome's base temperature**, keeping everything else: the
  precipitation humidity, the `+0.2` / `exp` / `×0.5` curve, the harshness softening, the scorching
  (`waterEvaporates`) override, Fire Resistance and Fire Protection. Cold Sweat's world temperature is
  in the same MC units as `Biome.getBaseTemperature`, so the curve needs no new constants. A player by a
  hearth in the tundra drains like one in a plains biome, and a desert at night drains less.
- (b) Use `Trait.BODY`: drain rises as the player overheats, falls as they are cold. Closer to "you
  sweat", but body temperature already reacts to drinking (step 7) and armour insulation, so thirst
  would chase a value the player partly controls through thirst itself.
- (c) Both: (a), times an extra factor when `BODY` is above some threshold (say 50, half way to damage).

**Recommended (a)**, with (c) as a later tweak if playtests want it.

How it reaches core without core naming Cold Sweat: a small internal seam in `ThirstManager`, e.g. a
`ToDoubleFunction<Player>` climate temperature source that defaults to the biome and that
`ColdSweatPresence` replaces at init. It is **not** `com.thirstwastaken2.api` (no `API_VERSION` bump,
no promise to other mods); `ThirstEvents.EXHAUSTION` does not fit, since it fires before the modifier
and would stack on top of the biome instead of replacing it.

- Cost: `Temperature.get` reads a capability, and the modifier is already cached per player for 20
  ticks (`MODIFIER_REFRESH_TICKS`), so this adds one read a second per player. `runBenchmark` on this
  node before and after.
- A config toggle `coldSweatClimate` (default true): field, `sanitize()`, widget and reset line in
  `ConfigCategory`, lang in `en_us` and `vi_vn`. It shows on every node but does nothing without the
  mod; the tooltip says so.

**Check:** an agent script (`tools/agent/integrations/cold-sweat.jsonl`) reads the modifier in a
plains biome, then next to a lit hearth in a snowy biome and in a desert by day and by night, with the
toggle on and off.

## 3. Investigation: what actually happens today

A throwaway agent script on `1.21.1-neoforge` with the mod and no mixin of ours. To record, as the
Brewin' and Chewin' plan did, in a table:

| Case | To find out |
|---|---|
| Fill Cold Sweat's waterskin at a Dirty, a Pure and a sea water source | components on the result (expected: `water_temperature` only) |
| Drink it (crouch) | thirst restored (expected: none), sickness (expected: none) |
| Fill a cauldron from it | cauldron `purity` value (expected: `0`, unset, then `defaultPurity`) |
| Empty waterskin from a crafting remainder | does it copy components off the filled one |
| Put our waterskin, canteen, a graded bottle in the Boiler | accepted or refused (expected: refused, not `#cold_sweat:boiler_valid`) |
| A Dirty bucket, a sea water bucket in the Hearth | taken as cooling fuel (expected: yes, by item id) |
| Our canteen boiled over a campfire | still Pure after 3 s a serving |
| Regeneration with low thirst in a hot biome | both mods' limits apply |
| HUD at each `hudPosition` | overlap with the temperature gauge |

## 4. Cold Sweat's waterskin carries our grade

The waterskin becomes a water container in our sense, recognised by registry id so that core never
names the mod (`purity/AGENTS.md`: "Optional mod support is by registry id only"):

- `WaterPurity.resolve` learns `cold_sweat:filled_waterskin`, so tooltips show its grade and
  `isWaterContainer` answers yes. `INFO` caches the per-item half as usual.
- A mixin on `WaterskinItem.getFilledItem(ItemStack, Level, BlockPos)` (static, has the source
  position) stamps `WaterPurity.sampleAt(level, pos)` on the result, server side only. Sea water gives
  a salty skin.
- `FilledWaterskinItem.useOn` into a cauldron: the cauldron keeps the worse of what it held and what
  is poured, as every pour does (`WaterQuality.worse`).
- `getCraftingRemainingItem`: strip `water_purity` and `water_salty` from the empty skin, as Cold
  Sweat strips its own.
- Sprites: Cold Sweat's waterskin has its own model, so no salt model is pointed at it; the tooltip
  line is the only sign of the grade. `syncModel` already leaves a modded container's model alone.
- Two waterskins in one game: ours is "Waterskin" too. The tooltip already says whose it is through
  the mod name line; if playtests find it confusing, rename ours in `en_us` rather than theirs.

**Check:** the step 3 script again, now expecting the grade on every path.

## 5. Drinking it restores thirst

On a crouch-drink (`FilledWaterskinItem.finishUsingItem`), one sip: thirst and quenched of one serving
of water of the skin's grade, through the same path a bottle takes (`ThirstEvents.DRINK` fires,
sickness rolls, salt water parches). Pouring on yourself restores nothing. The skin's durability is
the number of sips; values stay those of a water bottle per sip unless step 3 shows the skin holds
many more sips than our waterskin's servings, in which case a sip is scaled down.

Cold Sweat's temperature effect (`WaterskinTempModifier`) is left as it is: a hot sip warms, a cold
one cools, whatever the grade.

## 6. The Boiler purifies

Options:

- **(a) As Cold Sweat meant it for `thirst`:** the Boiler accepts our graded containers and Cold
  Sweat's waterskin, and every 200 ticks (÷ `TEMP_RATE`) raises each fresh one by one grade up to Pure.
  Salt water stays salt: boiling does not desalinate, as everywhere else in the mod.
- (b) Only Cold Sweat's waterskin is purified, and only once it reaches 50 (boiling).
- (c) Nothing; the furnace and our campfire boil are enough.

**Recommended (a)**; it is what Cold Sweat's own players expect. Our items go into
`#cold_sweat:boiler_valid` by a tag file in `src/main/coldsweat/resources`, and one mixin in
`BoilerBlockEntity.tick` next to the `isThirstLoaded()` branch does the raise, through
`WaterPurity.setQuality` so sprites and `water_salty: false` stay right. How fast it is compared with a
furnace goes in [WATER-PURIFICATION-BALANCE.md](../mechanics/WATER-PURIFICATION-BALANCE.md).

Furnace and campfire recipes for Cold Sweat's waterskin, like `recipe/compat/thirst/*.json` but on
our components, hand-written in the integration's resources with `neoforge:conditions`
`mod_loaded cold_sweat`. Datagen is Fabric only and this node's mod is NeoForge only, so they cannot be
generated; `checkNeoForgeResources` must still pass.

## 7. Our drinks change body temperature

Data only: `FoodData` files under `src/main/coldsweat/resources/data/thirstwastaken2/cold_sweat/item/food/`,
no class reference. Options:

- **(a) Nothing for plain water**; a small warmth for the drinks this mod gives values to that are
  hot (Farmer's Delight hot cocoa, Kaleidoscope teas) only if Cold Sweat does not already list them.
- (b) Every drink of water cools a little, for a short time.
- (c) Nothing at all.

**Recommended (a)**: water temperature already lives on Cold Sweat's own waterskin, and a second
cooling source on every bottle would make water a heat cure. The exact `FoodData` fields to confirm
against the 2.4.3.1 jar before writing any.

## 8. Decision: thirst and heat

Should a parched player tolerate heat worse, as in Tough As Nails?

- (a) No. Each mod keeps its own penalties.
- (b) Parched, or thirst under some level, lowers `HEAT_RESISTANCE` through a Cold Sweat
  `TempModifier` registered by the integration.

**Recommended (a)** for the first release; (b) is a gameplay change worth its own playtest.

## 9. Changelog and player docs

A CHANGELOG entry and the supported mods page, through the `write-docs` skill: water from Cold
Sweat's waterskin is graded, drinking it quenches, the Boiler purifies, and thirst follows the
temperature Cold Sweat shows. A line in [MANUAL-TESTING.md](../MANUAL-TESTING.md) for the HUD overlap.

## 10. Nothing crashes without the mod

After every step: `checkOptionalSeam`, `checkLoaderSeam`, `checkVersionSeam`, `runGametest` on
`1.21.1-neoforge`, and `boot.jsonl` with `-PwithoutOptional=cold_sweat`. The core seam of step 2 must
compile and run on all ten nodes; build the others once when it lands.
