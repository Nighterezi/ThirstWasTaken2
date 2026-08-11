# purity/

Everything about how clean a given body of water is. Purity is an int `0..3`
(dirty, slightly dirty, acceptable, purified), `WaterPurity.MIN`/`MAX`.

| File | Owns |
|---|---|
| `ThirstComponents` | the `water_purity` and `water_servings` data component types |
| `WaterPurity` | reading, writing and deciding purity; the sickness table; the container test |
| `WaterInteractions` | the interaction callbacks that move purity between world, blocks and items |

## Where a purity value can live

| Carrier | Storage | Read with |
|---|---|---|
| Item stack | `thirstwastaken2:water_purity` component | `WaterPurity.get(stack)` |
| Cauldron | `purity` blockstate property, **offset by +1 so 0 means "unset"** | `WaterPurity.at(level, pos)` |
| Water in the world | derived from Y level and flow | `WaterPurity.at(level, pos)` |
| Anything unstamped | `ThirstConfig.defaultPurity` | falls out of `get`/`at` |

The `+1` offset is the single most common thing to get wrong: `BLOCK_PURITY` ranges `0..4`, a stored
`0` means nothing has been poured in yet, and every read must do `stored - 1` after checking
`stored > 0`. `WaterInteractions.storeInCauldron` writes `purity + 1`.

## Rules the code keeps

- **Mixing takes the worse purity.** Pouring into a cauldron stores `min(held, stored)`;
  `WaterskinItem.addWater` mixes the same way. Never average, never take the better one.
- **`isWaterContainer` is per stack, not per item.** Water bottles are plain `minecraft:potion` stacks
  distinguished only by their `POTION_CONTENTS`, and an empty waterskin is not a container. The
  `INFO` cache only answers the per-`Item` half of the question.
- **`INFO` caches forever.** Only put facts in it that cannot change at runtime. Config-dependent
  purity is stored as the sentinel `PURITY_FROM_CONFIG` (`-1`) and resolved on each call.
- **Optional mod support is by registry id only.** `resolve` matches namespaces
  (`toughasnails`, `farmersdelight`, `collectorsreap`, `farmersrespite`, `brewinandchewin`, plus
  `create:builders_tea`) as strings — no class is ever referenced, so none of those mods is a
  dependency. Add support by extending `resolve`, not by importing anything.
- **One roll drives both effects.** `applyEffects` rolls once and compares it against
  `nauseaChance[purity]` and `poisonChance[purity]`, matching the original mod; it returns whether
  hydration should still be granted (`quenchWhenDebuffed`).
- **`tooltip(purity)` owns both the lang key suffix and the colour.** Adding a tier means touching the
  two switches together plus `thirst.purity.*` in all nine lang files.

## Why interactions are deferred

Vanilla resolves a cauldron fill or drain *after* our `UseBlockCallback` returns, so
`transferCauldronPurity` cannot read the result inline. It computes the value, returns `PASS` (so the
vanilla interaction still happens and the later callbacks still run), and queues a `Runnable` on
`END_OF_TICK`, drained by `WaterInteractions.tick` on the same server tick. The queue is an
`ArrayDeque` with no locking — **server thread only**.

Draining is messier than filling: the filled container does not have to end up in the interaction
hand (a stacked glass bottle sends the water bottle to the first free slot), so
`stampDrawnContainer` stamps the first freshly created container that is not stamped yet.

Waterskins are excluded from `transferCauldronPurity` on purpose — they draw through
`fillWaterskinFromCauldron`, and vanilla has no interaction that could pour one back, so a scheduled
transfer would be a phantom.
