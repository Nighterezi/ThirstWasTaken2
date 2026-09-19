# Sophisticated Backpacks integration plan

What ThirstWasTaken2 does and still has to do with
[Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks) on NeoForge. Almost every
upgrade that matters lives in Sophisticated Core, which Sophisticated Storage shares, so the work
targets Core and covers both. How the finished parts work is in
[src/main/sophisticated/AGENTS.md](../../src/main/sophisticated/AGENTS.md); this file is the order of
work and what each step needs.

Written on 2026-09-19 from Sophisticated Core `1.21.1-1.5.1.2341` and Sophisticated Backpacks
`1.21.1-3.26.3.2158`, the versions `1.21.1-neoforge` compiles against.

## Status

| # | Item | Kind | Status |
|---|---|---|---|
| 3 | Tank upgrade keeps water quality | bug | **Done** on `1.21.1-neoforge` |
| 1 | Feeding upgrade restores thirst | bug | **Done** on `1.21.1-neoforge` |
| 2 | Alchemy upgrade drinks through the mod | bug | **Done** on `1.21.1-neoforge` |
| 4 | Pump upgrade keeps water quality | bug | **Done** on `1.21.1-neoforge` |
| 7 | Smoking recipes for purified water | data | **Done**, every version |
| 6 | Waterskin and bowl as fluid containers | feature | **Done**, every NeoForge version |
| 5 | Drinking upgrade | feature | **Done** on `1.21.1-neoforge` |
| 8 | Newer NeoForge nodes (1.21.11, 26.1, 26.2) | port | To do |
| 9 | Changelog and player docs | docs | To do |

The order is the one agreed on: the bugs first, the Tank before the others because it let any water
turn Clean, then the cheapest feature, then the rest. All four bugs are fixed on `1.21.1-neoforge`.

## Done

### 3. Tank upgrade

The Tank upgrade moved water through handlers that know nothing of quality, so everything came out as
plain water and read as `defaultPurity`: a dirty bucket went in and Clean bottles came out, and sea
water bottles were refused outright. `TankUpgradeWrapperMixin` wraps the one method the tank finds
container handlers through, and `WaterQualityFluidHandler` carries the grade across. `WaterFluids`
moved to `src/main/neoforge` so Create and Sophisticated share it.

Checked in a real client with `tools/agent/sophisticated-tank.jsonl`, and once with the mixin off to
confirm the bug. The first version duplicated water (the last sea-water bottle was poured in forever);
the salt case now watches for that.

### 1. Feeding upgrade

The Feeding upgrade finishes eating with `Item.finishUsingItem`, past the mod's hook on
`ItemStack.finishUsingItem`, so fed food never restored thirst. `FeedingUpgradeWrapperMixin` calls
`ThirstManager.drinkItem` on that one call. Checked with `tools/agent/sophisticated-feeding.jsonl`:
seven melon slices took thirst from 4 to 20, and with the mixin off it stayed at 4.

### 2. Alchemy upgrade

The Alchemy upgrade drinks and eats what its filters name, on a condition, and finished through
`Item.finishUsingItem` the same way, so a potion drunk from a backpack gave no thirst.
`AlchemyUpgradeWrapperMixin` calls `ThirstManager.drinkItem` where the upgrade finishes an item, for
drinking and eating only, since a splash potion is thrown.

Decided: **the Alchemy upgrade never drinks plain water.** Water stays the player's choice, and the
Drinking upgrade (item 5) is where drinking from a backpack belongs. The first version of this plan
said a water bottle in its filter was drunk for free; that was wrong. Sophisticated's own potion
definition skips any potion without effects, water included. The mixin refuses plain water at the
condition check anyway, so a definition another mod adds cannot drink it either.

Checked with `tools/agent/sophisticated-alchemy.jsonl`: a Fire Resistance potion took thirst from 4 to
10, and dirty water was left alone. With the mixin off the potion was drunk and thirst stayed at 4.

### 4. Pump upgrade

Three problems, one of them caused by item 3:

- water collected from the world carried no grade, so a pump by a swamp filled the tank with water
  that read Clean, and one by the sea collected fresh water;
- buckets in a player's hand lost their grade going in, because the pump looks up their handler
  itself rather than through the Tank upgrade;
- once the Tank fix stamped the water in the tanks, the pump could not pump it out at all: it asks the
  backpack for a stack with no components, and the backpack only hands out matching ones.

`PumpUpgradeWrapperMixin` samples world water at the source (through `SampledWater`, moved to
`src/main/neoforge` and shared with Create, and only once the tanks have room), wraps buckets in hand,
and builds the pump-out request from the water in the tank. A pump filter set to water now takes water
of any grade (`FluidFilterLogicMixin`), since a filter made from a plain bucket refused graded water.

Checked with `tools/agent/sophisticated-pump.jsonl`: a plains pool gave the same grade as a bottle
filled from it by hand, an ocean pool gave salt water, a filtered pump still collected, and buckets
went in and out with their grades. Without the mixins every case lost its grade or, pumping out,
moved nothing. The neighbouring-block path, through a Create Fluid Tank next to the player, was run
afterwards and keeps the grade both ways too.

### 7. Smoking recipes

Water was purified by `smelting` and `campfire_cooking` recipes only, so the Smoking upgrades, and a
vanilla Smoker, could not purify it. The datagen now writes nine `smoking` recipes per version next to
the smelting ones, at 100 ticks, half a furnace, as a smoker is for food. They count for the
`boil_water` advancement, since a smoker credits the player the same way a furnace does, and
`PurificationGameTest` checks them, salt water included. This is plain data, so it covers every
version and both loaders, not just the nodes with Sophisticated.

Checked with `tools/agent/sophisticated-cooking.jsonl`: a purity-0 bottle came out of both the
Smoking and the Smelting upgrade with `water_purity: 2`.

### 6. Waterskin and bowl as fluid containers

The waterskin (three servings) and the terracotta bowls (one) now carry NeoForge's item fluid
capability, a serving being 250 mB, so the Tank and Pump upgrades, Create and any other fluid mod can
fill and empty them. It needs no Sophisticated, and it covers every NeoForge version: 1.21.1 through
`IFluidHandlerItem`, 1.21.11 and later through the transfer API, each a thin handler in a source
directory of its own over shared rules (`WaterContainerFluids`). Whole servings only, and a container
takes more water only of the grade it holds; the details are in `purity/AGENTS.md`. The Sophisticated
wrapper leaves these items alone, since their handler already carries the grade.

`ContainerFluidGameTest` runs one set of assertions against both APIs on all four NeoForge nodes, and
caught two bugs in the transfer handler on the way. In a client on 1.21.1: waterskins and bowls went
through the Tank upgrade with their grades, the Pump filled a waterskin in hand, and Create's Spout
filled a waterskin on a Depot once (750 mB, not twice) while an Item Drain emptied a bowl.

Not done: Fabric's Transfer API. No Fabric integration needs it yet.

### 5. Drinking upgrade

A new upgrade, the thirst version of the Feeding upgrade, in a basic and an Advanced tier. It drinks
from the backpack when the thirst bar is low: bottles, waterskins, bowls and other drinks with a thirst
value, and water from a Tank upgrade in the same backpack, 250 mB at a time. The cleanest water goes
first, nothing below the lowest grade (Clean by default) and never salt water. Everything goes through
the same code as drinking by hand. The Advanced tier adds the "drink at" and lowest-grade buttons and a
larger filter, as Advanced Feeding does.

Decided: the recipe mirrors Feeding's (a waterskin, two glass bottles and an ender pearl around an
upgrade base; the Advanced tier from the basic with a diamond, two gold and three redstone); a Tank of
water counts, held to the same lowest grade, so a Pump by an untreated lake does not make a free
canteen; and the Feeding upgrade's cooldowns, 100 ticks and 10 while still thirsty. Potions other than
water, milk and ominous bottles are never drunk.

Checked with three agent scripts: `sophisticated-drinking.jsonl` (clean bottles drunk and the dirty one
left, a Pure tank before a Clean bottle, sea water refused, the Advanced settings obeyed, honey and cider
drunk but not milk or potions, water before honey), `sophisticated-drinking-craft.jsonl` (all three
recipes in a crafting table, the upgrade in a Sophisticated Storage chest) and
`sophisticated-drinking-tab.jsonl` (the buttons clicked, the tab in all nine languages). The agent client
gained `client.click`, `client.slot(s)`, `client.language` and `client.textWidth` for them. The details
are in `src/main/sophisticated/AGENTS.md`.

## To do

### 8. Newer NeoForge nodes

**Problem.** Only `1.21.1-neoforge` sets `deps.sophisticated_core`. Sophisticated has releases for
1.21.11, 26.1 and 26.2, but from 1.21.11 its tanks move fluid through NeoForge's transfer API
(`ResourceHandler<FluidResource>`, `ItemAccess`, transactions) instead of `IFluidHandler`. The
`SophisticatedPresence` marker is the `IFluidHandler` generation's `SwapEmptyFluidContainerHandler`, so
those nodes would skip the whole integration even if they compiled it.

**Approach.**

- Split the gate per upgrade, so the Feeding mixin can apply where the Tank mixin cannot. The Feeding
  code is the same on every branch (`Item.finishUsingItem` at the same place), so its mixin should port
  unchanged.
  The Alchemy mixin should too, except for one line: `UseAnim` became `ItemUseAnimation` in 1.21.2, so
  its drink-or-eat check needs a Stonecutter version comment. Check that `tick` and `applyTo` still
  make the two calls it wraps.
- Write the Tank and Pump fixes again against the transfer API in a source directory of their own
  that only those nodes compile, the way `src/main/create` and `src/main/createfly` split one feature.
  Check first how the transfer API's bucket and bottle handlers treat data components; the rule
  may be different from `IFluidHandler`'s.
- The mod's own waterskin and bowls already speak the transfer API there (item 6), and
  `src/main/neoforge-transfer` is where the other transfer-API code belongs. The per-generation source
  directory is chosen in `build.neoforge.gradle.kts`.
- The Drinking upgrade (item 5) drinks from the tanks through `IStorageFluidHandler` and `FluidStack`,
  and its wrapper names `UseAnim`, which 1.21.2 renamed, so it moves with the Tank code rather than
  with Feeding.
- Add `deps.sophisticated_core` and `deps.sophisticated_backpacks` to each node's table in
  `stonecutter.properties.toml`. `update_mc_deps.py` already knows both keys.

### 9. Changelog and player docs

When the parts above are ready for a release: a CHANGELOG entry under `[Unreleased]`, a line in the
installation page's list of supported mods, and a Modrinth and CurseForge mention. Use the
`write-docs` skill, which keeps the plain, non-technical style those pages need. Say which Minecraft
versions have it, since for a while only 1.21.1 will.

## Testing, for every item

- The gametests run without Sophisticated. They prove the node still loads without it, and must keep
  passing: `./gradlew ":1.21.1-neoforge:runGametest"`.
- Everything that needs Sophisticated is checked in a real client with an agent script and a backpack
  template from `tools/agent/sophisticated-pack`. `/sbp template give` builds the backpack, opening it
  once unpacks the template, and `/sbp template create` plus `export` write what is left as SNBT under
  the world's `datapacks/`, so the result is read as text.
- Run each check once with the new mixin left out of the config as well, to show the bug was real and
  that the check can tell the two apart.
- A `level.dat` copied out of the Farmer's Delight world keeps Nourishment on its player, which cancels
  exhaustion. Clear effects at the start of a script.
