# Fruits Delight integration plan

What ThirstWasTaken2 should do with [Fruits Delight](https://modrinth.com/mod/fruits-delight) (mod id
`fruitsdelight`, package `dev.xkmc.fruitsdelight`), the fruit, juice, jam and jello addon for Farmer's
Delight. This file sets the order of work, what each step needs and how each one is checked. Once the
work is built, how it works goes where the steps below say.

Written on 2026-09-26 from:

- the repository [Minecraft-LightLand/FruitsDelight](https://github.com/Minecraft-LightLand/FruitsDelight),
  branch `1.21` at `f9ec48a` (2026-05-09), which is the released `1.2.14`;
- the Modrinth project `g6sbyCTu`, whose newest 1.21.1 upload is `1.2.14` (`TWbuEFZt`, 2026-05-09).

## Which build for which node

| Node | Build | Modrinth id | Requires on `runClient` |
|---|---|---|---|
| `1.21.1-neoforge` | `1.2.14` | `TWbuEFZt` | Farmer's Delight `[1.2.4,)` (we pin `1.21.1-1.3.4`). L2 Core, L2 Serial, L2 Modular Blocks, L2 Harvester and Registrate are nested in its jar (`jarJar`), and NeoForge loads nested jars itself |
| every other node | none | — | — |

There is **no Fabric build and no build newer than 1.21.1**. Modrinth lists `1.21` as well, but every
1.21.x upload asks for `[1.21.1,1.22)`.

## What Fruits Delight already does for thirst

The mod ships `compat/thirst/ThirstCompat`, behind its own common config `enableThirstCompat` (true by
default). It listens for `dev.ghen.thirst...RegisterThirstValueEvent` and is gated on
`ModList.isLoaded(Thirst.ID)`, the mod id `thirst` of the **upstream** Thirst Was Taken. Our mod id is
`thirstwastaken2`, so with us that code never runs, and today every one of its drinks restores
nothing: the juices are tagged `fruitsdelight:juice`, not `c:drinks`, and keyword matching is off by
default.

Its values are on the same 0-20 scale as ours (thirst, quenched):

| Upstream call | Items | Value |
|---|---|---|
| `addDrink` | every `FDJuice`: the 12 below | 8, 13 |
| `addFood` | orange, lychee, pineapple, kiwi, peach, hamimelon, mango (the fruit item) | 4, 6 |
| | pear | 6, 10 |
| | `baked_pear`, `hamimelon_popsicle`, `kiwi_popsicle` | 8, 13 |
| | `hamimelon_shaved_ice`, `fig_chicken_stew` | 10, 16 |
| | `pear_with_rock_sugar` | 14, 20 |

The drink value matches ours for Farmer's Delight's own `apple_cider` and `melon_juice` (8, 13). The
food values are two to three times what we give comparable food (a melon slice is 4, 5, Farmer's
Delight's stews 4, 5), so the proposal below keeps the drinks and scales the foods to ours.

## Where water lives in Fruits Delight

No block entity holds water, so there is no tank to keep a grade in. Water is used four ways:

| Path | Recipe | What happens to the grade |
|---|---|---|
| **Shapeless crafting with a water bottle** | `orange_juice`, `lemon_juice`, `pear_juice` (`Category.RINSE`, `waterCraft`): fruit, sugar and `l2core` `PotionIngredient.of(Potions.WATER)` | the bottle is consumed and the juice is its own item: **the grade is dropped**. Whether a stamped bottle matches at all depends on how `PotionIngredient` compares (only `potion_contents`, or the whole component map). Step 3 |
| **Farmer's Delight cooking pot** | the teas, `bayberry_soup`, `bellini_cocktail` | no water ingredient: the pot is heated, the recipe asks for fruit, sugar, leaves or ice |
| **Water cauldron** | a full water cauldron + lemon slice → `lemonade_cauldron`; a full water cauldron + a jam bottle → `<fruit>_cauldron`; then heat, sugar, slime ball → jam and jello, taken out with a bottle or a bowl | the cauldron **block is replaced**, so our `purity` blockstate value is gone. Nothing drinkable comes out: jam and jello, both heated. The water is boiled on the way, which is our rule for purification anyway |
| **Create mixing** (`1.21.1-neoforge` has Create on `runClient`) | the `RINSE`, `BOIL` and `SOUP` juices mix 250 mB of `Fluids.WATER` with fruit, the boiled ones heated | the fluid ingredient matches the fluid, not its components, so any grade and possibly sea water mix into juice. Step 3 confirms |

Fruits Delight adds entries to vanilla's `CauldronInteraction.WATER` map (`CauldronRecipe.create`) and
checks `LayeredCauldronBlock.LEVEL == 3`. It never reads our `purity` property, and ours never reads
its cauldrons, so the two only meet where the water cauldron is replaced. Step 3 checks that a graded
full cauldron still takes a lemon slice.

## The drinks and foods

In `ThirstConfig` by id, like Kaleidoscope Cookery and Brewin' and Chewin': a `fruitsDelightDrinks` /
`fruitsDelightFoods` pair merged with `putMissing`. That reaches every node, matches nothing where the
mod is absent, and needs no code of the mod's. Proposed (thirst, quenched):

| Group | Items | Proposed |
|---|---|---|
| Juices and teas, a bottle | `hamimelon_juice`, `kiwi_juice`, `orange_juice`, `lemon_juice`, `pear_juice`, `hawberry_tea`, `mango_tea`, `peach_tea`, `lychee_cherry_tea`, `mangosteen_tea` | 8, 13, as upstream and as Farmer's Delight's juices |
| | `bayberry_soup` (a bottle, sweet and cooked) | 8, 13 |
| | `mango_milkshake` | 8, 12 |
| | `bellini_cocktail` (alcohol, Heal Aura and Nausea) | 5, 6, as Brewin' and Chewin's beer |
| Cold | `hamimelon_shaved_ice` | 8, 10 |
| | `hamimelon_popsicle`, `kiwi_popsicle` | 7, 9, as Farmer's Delight's melon popsicle |
| Jello, a bowl, eaten fast | the 22 `<fruit>_jello` | 3, 4 |
| Fruit | `hamimelon_slice` | 4, 5, as a melon slice |
| | `orange`, `lychee`, `pineapple_slice`, `kiwi`, `peach`, `mango`, `pear` | 2, 3, as an apple |
| | `orange_slice`, `lemon_slice`, `baked_pear` | 1, 2 |
| Bowls | `pear_with_rock_sugar` (a sweet soup) | 6, 8, as Farmer's Delight's fruit salad |
| | `fig_chicken_stew` | 4, 5, as Farmer's Delight's stews |
| | `mango_salad` | 4, 5, as Farmer's Delight's mixed salad |
| | `blueberry_custard` (a glass) | 2, 3, as Farmer's Delight's glow berry custard |
| Left out | the jams (block items in a bottle), `dried_persimmon`, cookies, muffins, pies, tarts, rolls, sheets, sticks, meals, `durian_flesh` and the other dry foods | — |

The ids come from the mod's generated `en_us.json`. `pineapple` is the whole block item; the slice is
what is eaten, so only `pineapple_slice` is listed. Step 3 confirms each id resolves.

## Status

| # | Item | Kind | Nodes | Status |
|---|---|---|---|---|
| 1 | The mod on the `runClient` classpath | build | `1.21.1-neoforge` | To do |
| 2 | Thirst values for the drinks and foods | data | all (config) | To do |
| 3 | What happens to a grade, per path | investigation | `1.21.1-neoforge` | To do |
| 4 | A graded or salty water bottle in the juice recipes | decision | `1.21.1-neoforge` | Open |
| 5 | Ask upstream to target `thirstwastaken2` | outreach | — | Optional |
| 6 | Changelog and player docs | docs | — | To do |
| 7 | Nothing crashes without the mod | test | `1.21.1-neoforge` | To do, only if step 4 adds code |

## 1. The mod on the `runClient` classpath

No integration row yet: nothing in steps 1 to 3 names a class of the mod. Like Farmer's Delight
(`deps.farmersdelight`), only a key and a `runClientMod` line:

- `deps.fruits_delight = "TWbuEFZt"` in `[neoforge."1.21.1"]`, pinned by Modrinth version id, and
  `ModrinthDep("fruits_delight", "fruits-delight", by_id=True, mirrors=())` in `MODRINTH_DEPS`.
- In `build.neoforge.gradle.kts`, `findProperty("deps.fruits_delight")?.let { runClientMod(listOf(
  "fruits-delight", "fruitsdelight"), "maven.modrinth:fruits-delight:$it") { isTransitive = false } }`.
- If step 4 decides on a mixin, the row, the gate and `compileOnly` come then, as in
  [Adding an integration](../../../AGENTS.md#adding-an-integration).

**Check:** `./gradlew ":1.21.1-neoforge:runClient" -Pagent=tools/agent/smoke/boot.jsonl` comes up with
the mod, and with `-PwithoutOptional=fruits-delight` without it.

## 2. Thirst values

`fruitsDelightDrinks` and `fruitsDelightFoods` in `ThirstConfig`, the table above, called from
`defaultDrinks`, `defaultFoods` and `sanitize` with `putMissing`, and a Javadoc that says why they are
listed (the upstream compat targets another mod id) and why the foods are scaled down from upstream.

**Check:** a unit or game test in the pattern of `brewinAndChewinDrinksAreMergedIntoAnOlderConfig`:
the defaults list the juices, leave the jams out, and a merge into an older config does not overwrite
a player's value. `runGametest` on one Fabric and one NeoForge node, since `ThirstConfig` is common.

## 3. Investigation: what happens to a grade

A throwaway agent script on `1.21.1-neoforge` with the mod, no code of ours. Record for each case what
comes out, as Brewin' and Chewin' step 3 did:

| Case | What to record |
|---|---|
| Orange juice crafted from a Dirty water bottle, from a Pure one, from a sea water bottle | accepted or refused, and whether the crafting grid gives back a glass bottle |
| Lemon slice on a full Dirty cauldron, a full sea water cauldron | lemonade cauldron or not |
| Create mixer with Dirty and sea water piped in (kiwi juice, `JUICE` has no water; `lemon_juice` has) | juice or not |
| Every id in step 2 | resolves; the log shows no "item does not exist" warning for them |
| Drinking each group | the value is restored, the tooltip shows it |

## 4. Decision: graded and salty water in the juice recipes

Options, once step 3 says what happens:

- **(a) Nothing.** A juice is its own item with its own value, and the bottle's grade is dropped,
  which is what tea and fermented drinks already do (Brewin' and Chewin' decision 7). Sea water makes
  lemon juice.
- (b) As (a), but **refuse sea water**: a mixin on the recipe's ingredient test so a salty bottle
  does not match `PotionIngredient.of(Potions.WATER)`, and nothing on the Create path.
- (c) The juice remembers the grade and rolls sickness. Not recommended, for the same reasons as
  Brewin' and Chewin' decision 7(b).

**Recommended: (a)**, unless step 3 finds that a stamped bottle is refused, in which case the fix is
a mixin like `KegBottleMixin` that compares a stamped bottle unstamped, plus (b) in the same place.
Either of those turns this into a real integration: a row in the integration table, a
`FruitsDelightPresence` gate, a mixin plugin, and step 7.

## 5. Optional: ask upstream to target `thirstwastaken2`

Fruits Delight lists Thirst Was Taken as an optional dependency on Modrinth. A data pack file in its
own jar (`data/fruitsdelight/thirstwastaken2/drinks/fruitsdelight.json`, see
[data-packs.md](../../docs/developers/data-packs.md)) would give its values with no class reference
either way. Our config still wins over it, so step 2 stays useful. Not needed for anything above.

## 6. Docs

`CHANGELOG.md` (Unreleased), a short section or page on the site listing Fruits Delight as supported
(values only on every node, NeoForge 1.21.1 in practice since that is the only build), and the
installation page's NeoForge row. Follow the `write-docs` skill. The root `AGENTS.md` gets a row in the
optional integrations table only if step 4 adds code; a values-only mod is covered by the "Drinks from
other mods" row.

## 7. Optional seam

Only if step 4 adds code: `checkOptionalSeam`, `checkLoaderSeam`, and
`tools/agent/smoke/boot.jsonl` with `-PwithoutOptional=fruitsdelight` on `1.21.1-neoforge`.

## Not planned

- **Jam and jello cauldrons keeping a grade.** Nothing drinkable comes out, and the jam is heated.
- **Fruits Delight's own Thirst compat.** It references `dev.ghen.thirst` classes; we do not provide
  them, and faking the upstream mod id would break the real upstream mod in the same pack.
- **Fabric and newer Minecraft versions**, until the mod publishes one.
