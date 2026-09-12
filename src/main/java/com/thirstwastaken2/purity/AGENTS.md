# purity/

Everything about sampled water quality.

`WaterQuality` is a **sealed** interface with two cases: `Fresh(purity)`, graded `0..3` (dirty,
murky, clean, pure), and `Salt`. Sea water is a different kind of water, not a low grade of fresh
water - cooking cannot improve it, one salty serving spoils a whole batch, and it never hydrates.
Sealing it is the point: a `switch` over a `WaterQuality` has to answer for salt water or it does not
compile, which is what stops salt water from quietly inheriting a grade's tooltip, sprite or effects.

| File | Owns |
|---|---|
| `ThirstComponents` | purity, salinity and serving data component types |
| `WaterQuality` | the sealed pair, `Fresh` or `Salt` |
| `WaterPurity` | environmental sampling, storage, sickness, sprites and container tests |
| `WaterInteractions` | the interaction callbacks that move quality between world, blocks and items |

## Where a quality can live

| Carrier | Storage | Read with |
|---|---|---|
| Item stack | `water_purity` for a grade, `water_salty` for sea water | `WaterPurity.quality(stack)` |
| Cauldron | one `purity` blockstate value | `WaterPurity.storedQuality(state)` |
| Water in the world | biome baseline plus small local modifiers | `WaterPurity.sampleAt(level, pos)` |
| Anything unstamped | `ThirstConfig.defaultPurity`, fresh | falls out of `quality` |

The cauldron value is `0` for unset, `1..4` for the four grades (offset by one so that zero can mean
"nothing poured in yet") and `5` for salt water. It is one property rather than a grade plus a
boolean flag on purpose: **vanilla hands a freshly placed block the first value of every property it
carries, and for a boolean that value is `true`**, so a separate salinity flag makes every new
cauldron read as sea water. `WaterPurity.storedValue` and `storedQuality` are the only two places
that know the encoding.

## Rules the code keeps

- **Salt water carries no grade.** `setQuality` removes `water_purity` from a salty stack. That is
  what keeps the 18 purification recipes, which all match on a grade, from matching sea water, and
  what stops `get` from inventing one. Anything asking "how clean is it" goes through `quality`.
- **A fresh container always writes `water_salty: false`,** even though false is the component's
  default. Every cooking recipe matches on it, so a container that leaves it out silently stops being
  cookable - which is exactly what went wrong with looted water bottles once, and what
  `PurificationGameTest` now watches.
- **Sprites are part of the contract, not decoration.** `syncModel` runs inside `setQuality`. The
  mod's bowl switches on custom model data index 1 (`0..3` grades, `4` salt); vanilla's bottle and
  bucket cannot be given a model at registration, so salt water points `minecraft:item_model` at one
  of this mod's item definitions instead. The bottle's definition reuses vanilla's own potion model
  and only swaps the tint, so it follows resource packs. Clearing the component only ever clears a
  model this mod set, so a modded container keeps its own.
- **Waterskin mixing is serving-weighted and rounds down**, so one clean mouthful cannot talk a batch
  up a grade. Salt is not averaged at all: a single salty serving turns the whole skin into sea
  water. Cauldrons cannot average, because their blockstate has room for one value, so they keep the
  worse of what they hold and what is poured in.
- **Water that arrives on its own is graded where it lands.** Rain and pointed dripstones fill
  cauldrons with nobody pouring anything in, so `filledByRain` and `filledByDripstone` stamp
  `ThirstConfig.rainwaterPurity` and `dripstonePurity` rather than letting the cauldron fall through
  to `defaultPurity`. Both keep the worse of what the cauldron held and what fell in, like pouring,
  and both check that the blockstate actually changed: the vanilla hooks run whether or not a layer
  was added.
- **Sampling is interaction-only and server-only.** The fixed 5x3x5 block inspection must never move
  into a tick or tooltip path. Ocean and beach biomes return `Salt` before that scan runs. Bottle and
  bucket mixins skip sampling on the prediction client.
- **The contamination score is never stored.** `sampleAt` scores a source, grades it, and keeps only
  the grade, so no container carries a hidden number that the player cannot see and the tooltip
  cannot explain.
- **`isWaterContainer` is per stack, not per item.** Water bottles are plain `minecraft:potion` stacks
  distinguished only by their `POTION_CONTENTS`, and an empty waterskin is not a container. The
  `INFO` cache only answers the per-`Item` half of the question.
- **`INFO` caches forever.** Only put facts in it that cannot change at runtime. Config-dependent
  purity is stored as the sentinel `PURITY_FROM_CONFIG` (`-1`) and resolved on each call.
- **Optional mod support is by registry id only.** `resolve` matches namespaces
  (`toughasnails`, `farmersdelight`, `collectorsreap`, `farmersrespite`, `brewinandchewin`) as
  strings - no class is ever referenced, so none of those mods is a dependency. Add support by
  extending `resolve`, not by importing anything.
- **One roll drives both effects.** `applyEffects` rolls once for fresh water and compares it against
  `nauseaChance[purity]` and `poisonChance[purity]`, matching the original mod; it returns whether
  thirst should still be restored (`quenchWhenDebuffed`). Salt water never reaches the roll: it
  spends exhaustion, applies Nausea and returns false.
- **`purityKey` and `purityColor` own the lang key and the colour together**, and they are the only
  palette for water quality; the tooltip tiers in `src/main/java/com/thirstwastaken2/AGENTS.md` say
  where those colours sit among the other lines. Adding a grade means
  Adding a grade means touching both switches plus `thirst.purity.*` in all nine lang files. The
  grade colours run warm to cool so that all four stay apart on a dark tooltip, and salt's line sits
  off that ramp entirely, in the pale cream of dried salt. Its sprites are turquoise instead, deep
  enough that a bowl of sea water is not mistaken for the light blue of a pure one.

## Why interactions are deferred

Vanilla resolves a cauldron fill or drain *after* our `UseBlockCallback` returns, so
`transferCauldronPurity` cannot read the result inline. It computes the value, returns `PASS` (so the
vanilla interaction still happens and the later callbacks still run), and queues a `Runnable` on
`END_OF_TICK`, drained by `WaterInteractions.tick` on the same server tick. The queue is an
`ArrayDeque` with no locking - **server thread only**.

Draining is messier than filling: the filled container does not have to end up in the interaction
hand (a stacked glass bottle sends the water bottle to the first free slot), so
`stampDrawnContainer` stamps the first freshly created container that is not stamped yet.

Waterskins are excluded from `transferCauldronPurity` on purpose - they draw through
`fillWaterskinFromCauldron`, and vanilla has no interaction that could pour one back, so a scheduled
transfer would be a phantom.
