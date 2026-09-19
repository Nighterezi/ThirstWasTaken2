# Water sickness rework

A plan to make drinking bad water feel closer to what really happens, while it stays Minecraft. The
work is split into steps, each its own commit with its own gametests. Steps that change the same
behaviour ship in the same release, so players see one change rather than a balance that moves
twice.

| Step | What | Status |
|---|---|---|
| 1a | Drop Hunger from dirty water | done |
| 1b | A Parched effect | done |
| 1c | Sea water makes you Parched | done |
| 1d | Longer Nausea as a stand-in, and a bigger sip by hand | done |
| 2 | A new Upset Stomach effect | idea |
| 3 | Incubation: sickness starts later, not on the sip | idea |
| 4 | Salt and oral rehydration salts | idea |
| 5 | Toxins in stagnant water, and smaller extras | idea |

1a and 1b ship together. The docs and CHANGELOG are written once, at the end of 1b.

## The two effects

| | **Parched** | **Upset Stomach** |
|---|---|---|
| What it is | A symptom: a dry mouth, right after the drink | The illness, lasting longer |
| From | Sea water, right away | Bad water, after incubation (step 3) |
| Does | Thirst drains faster | Thirst drains faster, food satisfies less, short bursts of Nausea |
| Ends | On its own | On its own, faster with oral rehydration salts (step 4) |
| Icon | `textures/mob_effect/parched.png`, a dry tongue | still to draw |

Parched is to thirst what vanilla's Hunger effect is to food, and "dehydration" stays the name of
dying from an empty bar, the way starvation is for food. That is why the effect is not called
Dehydration: the word already means something else in the mod's death messages and config.

Both effects drain thirst through one shared path in `ThirstManager`, with their own rate. Upset
Stomach does not apply Parched on top, so the HUD never shows two icons for one consequence.

**Bad fresh water does not make you Parched.** It was the first design, and was dropped after play:
salt makes you thirsty at once, but bad water dries you out only later, once it makes you ill.
Giving both the same instant effect blurred the two lessons. Bad water's dehydration is Upset
Stomach's job.

## Why

Today `WaterPurity.applyEffects` rolls once and, for fresh water, compares that roll against two
tables in the config:

| Grade | Nausea (5 s) and Hunger (30 s) | Poison (10 s) |
|---|---|---|
| Dirty | 100% | 30% |
| Murky | 50% | 10% |
| Clean | 5% | none |
| Pure | none | none |

Hunger is the odd one out. It is vanilla's word for food poisoning (raw chicken, rotten flesh), and
the original mod reused it. But the real danger of bad water is **losing water**, through diarrhoea
and vomiting, and people with an upset stomach usually *lose* their appetite rather than grow
hungrier. Hunger also does nothing to the thirst bar: `ThirstManager.tickPlayer` refunds its
exhaustion so that food poisoning does not double as dehydration.

## Goals

- **Real where it teaches something.** Each effect should map to one thing that is true outside the
  game and can be explained in one short sentence.
- **Still Minecraft.** No graphic symptoms, no named diseases in the UI, no punishment the player
  cannot see coming or explain afterwards.
- **Always visible.** An effect icon, a line on the tooltip or a message on the action bar for every
  consequence. A hidden penalty reads as a bug.
- **Configurable.** Every chance and duration sits in `ThirstConfig`, so a pack can tune it or turn
  it off.

## The real-world basis

| Fact | Where it shows up |
|---|---|
| Bad water makes you sick through diarrhoea and vomiting, and the danger is dehydration | Step 2: thirst drains faster, not hunger |
| An upset stomach kills the appetite | Phase 2: food restores less saturation |
| Illness from water starts hours to days after drinking, not on the sip (norovirus about 12-48 hours, many bacteria a few days, Giardia one to three weeks) | Phase 3 |
| The more contaminated the water, and the more of it you drink, the likelier and worse the illness | Phase 2: grade and repeated drinks set the level |
| Oral rehydration salts (clean water, sugar, salt) treat dehydration from diarrhoea; the sugar helps the gut absorb salt, and water follows it. It is one of the medical advances that has saved the most lives | Phase 4 |
| Sea salt is made by evaporating sea water | Phase 4, and the distillation idea in [ROADMAP.md](ROADMAP.md) |
| Drinking sea water (about 3.5% salt) dehydrates you: the kidneys need more water to get rid of the salt than the drink brought in | Already true: salt water never hydrates and costs exhaustion. Step 1c adds Parched |
| Warm, still water can grow blue-green algae whose toxins boiling does not destroy | Phase 5 |

Time in the game runs 72 times faster than real time: a day is 20 minutes, so one in-game hour is
50 seconds. Real incubation periods would be far too long to feel connected to the drink, so phase 3
compresses them.

---

## Step 1a: drop Hunger from dirty water

Small and nearly balance-neutral. Hunger I for 30 seconds costs 600 ticks × 0.005 = 3 food
exhaustion, three quarters of a hunger point, and nothing on the thirst bar. Dirty water already
dries the player out through Nausea: with `depletesWhenNauseous` on, `ThirstManager` adds 0.06 thirst
exhaustion per tick, which over 5 seconds is about 1.5 thirst points before the climate modifier.

### Changes

- [x] `WaterPurity.applyEffects`
  ([WaterPurity.java](../../src/main/java/com/thirstwastaken2/purity/WaterPurity.java)): remove the
  Hunger effect. Leave a comment at the divergence from the original mod, as `AGENTS.md` asks.
- [x] Keep `ThirstConfig.nauseaChance` as it is. Same name, same defaults, so existing config files
  keep working.
- [x] `WaterEffectsGameTest`
  ([WaterEffectsGameTest.java](../../src/gametest/java/com/thirstwastaken2/gametest/WaterEffectsGameTest.java)):
  rename `dirtyWaterCausesNauseaAndHunger` to `dirtyWaterCausesNauseaWithoutHunger` and assert that
  the player has Nausea and not Hunger.
- [x] Lang: `thirstwastaken2.config.nausea_chance.tooltip` no longer mentions Hunger, in all nine
  files (`en_us`, `vi_vn`, `fr_fr`, `ja_jp`, `ko_kr`, `pl_pl`, `ru_ru`, `zh_cn`, `zh_tw`). The
  in-game text follows each commit; the docs wait for 1b.
- [x] `purity/AGENTS.md`: "One roll drives both effects" still holds (Nausea and Poison).

### Left alone on purpose

- The Hunger refund in `ThirstManager.tickPlayer`. Hunger from food still must not dehydrate.
- `PlayerStateGameTest` and the benchmark's `TickScenario`. They test Hunger from other sources.

### Verify

- `./gradlew ":26.2.x:runGametest"` and `./gradlew ":1.21.1-neoforge:runGametest"`.
- No generated files change, so `checkDatagen` is not needed.

## Step 1b: Parched

The effect itself, thirst's counterpart of Hunger. Only sea water gives it (1c). It first came from
the nausea roll of bad water too; that was dropped, see [The two effects](#the-two-effects).

### Design

- Adds thirst exhaustion every tick, scaled by level like Hunger: **0.01 per tick per level**, so
  30 seconds of Parched II cost 3 thirst points before the climate modifier. Tune in play.
- A harmful effect, so it shows in the inventory and the HUD with the dry-tongue icon.
- While it lasts the thirst bar is drawn in dry sand, outlines and empty droplets included, the way
  vanilla recolours the food bar for Hunger (`thirst_icons_parched.png`, drawn by
  `tools/generate_parched_icons.py`).
- A drink gives it without particles: a dry mouth is felt, not seen. The particle colour, tan, only
  shows when a command gives it with them.
- A second drink while Parched refreshes the duration; it does not stack.

### Changes

- [x] The icon: `src/main/resources/assets/thirstwastaken2/textures/mob_effect/parched.png`, 18x18.
  The game finds it by the effect's id; no JSON needed.
- [x] A new `ThirstEffects` holding `PARCHED`, registered through
  `Loader.onRegister(Registries.MOB_EFFECT, ...)` in `ThirstWasTaken2.initialize`. Record the new
  step in the init order in `src/main/java/com/thirstwastaken2/AGENTS.md`.
- [x] `MobEffect`'s constructor is protected, so a small subclass with no overrides.
  `Registry.registerForHolder` gives the `Holder<MobEffect>` that `MobEffectInstance` takes on every
  version. If a node disagrees, the fork goes in `platform/Vanilla`, next to `mobEffect`.
- [x] **The effect is a marker.** Do not override `applyEffectTick`: its signature differs between
  1.21.1 and later. `ThirstManager.tickPlayer` adds the exhaustion next to the Nausea line, from one
  `getEffect` lookup, so the tick's fast path allocates nothing.
- [x] Lang in all nine files: `effect.thirstwastaken2.parched` (English "Parched", Vietnamese "Khô
  họng").
- [x] Gametests: sea water makes the player Parched II without particles, a dirty drink does not,
  Parched drains thirst faster by level, a pure drink does nothing.
- [x] An agent-client script, [tools/agent/parched.jsonl](../../tools/agent/parched.jsonl): the real
  right click, the real server tick and the client's lang, on every node.
- [x] Docs, written with the `write-docs` skill, covering 1a and 1b together:
  [water-purity.md](../docs/features/water-purity.md) (the "Drinking bad water" table and the
  durations), the grade tables in [MODRINTH.md](../MODRINTH.md) and
  [CURSEFORGE.md](../CURSEFORGE.md), and one `CHANGELOG.md` line under `[Unreleased]`.
- [x] `docs/dev/MANUAL-TESTING.md`: check the icon in the inventory and HUD on one node per loader.

### Verify

- `runGametest` on every node; this is the first registry the mod adds since items.
- `runClient` once per loader to see the icon.

## Step 1c: sea water makes you Parched

Sea water keeps its exhaustion, its five seconds of Nausea, and never hydrates, and adds Parched II
for 30 seconds, since the salt makes the body lose more water than the drink brought in. Level II,
like vanilla's pufferfish gives Hunger III, so the one source of Parched hits hard. Its own
CHANGELOG line. Whether it should keep Nausea is still worth a second look after some play.

## Step 1d: longer Nausea, bigger sips

Until Upset Stomach exists, Nausea is what bad fresh water costs. Five seconds ended before the
screen had finished warping, so it now lasts by grade, set by `nauseaSeconds`:

| Grade | Nausea | Thirst it costs, about | A bottle (+6) nets | A bowl (+4) nets |
|---|---|---|---|---|
| Dirty | 12 s | 3.6 | +2.4 | +0.4 |
| Murky | 8 s | 2.4 | +3.6 | +1.6 |
| Clean | 5 s | 1.5 | +4.5 | +2.5 |

Bad water still always quenches, but barely. Drinking by hand now restores 2 thirst and 2 quenched,
so a player who chooses to drink from a swamp does not have to click as often. Existing config files
keep their old hand drinking values, since the config has no migration.

When Upset Stomach arrives (step 2), Nausea can go back to being short: the illness will carry the
thirst cost instead.

---

## Phase 2: Upset Stomach

The illness itself, longer than Parched, with its own icon still to draw. It drains thirst through the
same path as Parched, at its own rate, and adds what Parched does not: a weaker appetite and short
bursts of Nausea.

### Design

| Level | From | Thirst drain | Food | Nausea |
|---|---|---|---|---|
| I | Murky, sometimes Clean | ×1.5 | saturation ×0.75 | a rare short burst |
| II | Dirty, or drinking bad water while already sick | ×2 | saturation ×0.5 | an occasional short burst |

Durations and multipliers above are starting points to tune, not final numbers.

- The immediate Nausea on the sip becomes short (1-2 s) or goes away: it stands for the taste, not
  the illness.
- Drinking more bad water while sick extends the effect, and can raise it to level II.
- Poison keeps its current roll until phase 5.

### Implementation notes

- Register a `MobEffect` through `Loader.onRegister(Registries.MOB_EFFECT, ...)`, the same seam that
  blocks, components and items use.
- **The effect is a marker only.** Do not override `applyEffectTick`: its signature differs between
  1.21.1 and later versions. `ThirstManager.tickPlayer` checks for the effect, as it already does for
  Nausea, so nothing needs a version fork.
- Keep the tick's fast path: one `hasEffect` lookup, no allocation. Watch the benchmark.
- Reduced saturation goes through the existing `FoodDataMixin`.
- New assets: the effect icon, `effect.thirstwastaken2.upset_stomach` in nine lang files, config
  fields and sliders in `ConfigCategory`.
- Gametests: drain is faster with the effect, food restores less saturation, a dirty drink applies
  level II, a pure drink applies nothing.

### Open questions

- Whether milk cures it. Vanilla milk clears everything, which would make phase 4 pointless. NeoForge
  can exclude an effect through `EffectCure`; Fabric would need a mixin.
- Whether a player at full thirst can still catch it by drinking by hand.

---

## Phase 3: incubation

Drinking bad water rolls the sickness as today, but the effect starts 1-3 minutes later instead of on
the sip. Shortly before it starts, the action bar says something like *"Your stomach feels
uneasy..."*, with a stomach sound.

### Where the pending sickness lives

| Option | For | Against |
|---|---|---|
| A hidden effect (no icon, no particles) that turns into Upset Stomach when it runs out | saved with the player by vanilla, no codec change | milk clears it; the expiry hook differs between versions |
| A field in `ThirstData` | full control, survives milk | codec and sync change, every save migrates |

Decide at the start of the phase.

---

## Phase 4: salt and oral rehydration salts

- **Salt**, a new item, left behind when sea water boils away in a hanging pot or a cauldron over
  heat. This overlaps the distillation idea in [ROADMAP.md](ROADMAP.md) and should be designed with
  it.
- **Oral Rehydration Salts**, a bottle made from Pure water, sugar and salt. Restores more thirst
  than plain water and shortens Upset Stomach. The tooltip says why in one short grey line.
- Recipes and models through datagen, textures, tooltip lines in `ThirstTooltip`, and an
  advancement for the first cure.

---

## Phase 5: later

- **Toxins in stagnant water.** Poison only from water sampled in the `stagnant_water` tag or hot
  biomes, standing for algae toxins. Going further, boiling would not clear it and charcoal would,
  which touches the whole purification system and `WATER-PURIFICATION-BALANCE.md`.
- **A `classic` / `realistic` switch** in the config, if players ask for the original mod's effects
  back.
