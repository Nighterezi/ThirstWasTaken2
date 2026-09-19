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
| 7 | Smoking recipes for purified water | data | To do |
| 6 | Waterskin and bowl as fluid containers | feature | To do |
| 5 | Drinking upgrade | feature | To do |
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
moved nothing. The neighbouring-block path goes through the same code but was not run.

## To do

### 7. Smoking recipes

**Problem.** Water is purified by `smelting` and `campfire_cooking` recipes. The Smelting and
Auto-Smelting upgrades use vanilla smelting recipes, so they should purify already. The Smoking
upgrades, and a vanilla Smoker, only read `smoking` recipes, and there are none.

**Approach.** Add `smoking` variants to the purification recipes in `src/datagen`, and run
`runDatagen` for every version (`checkDatagen` fails otherwise). It is plain data, so it needs no
Sophisticated code and also makes the vanilla Smoker work. Decide whether a smoker cooks faster, as it
does for food, and put the number in `WATER-PURIFICATION-BALANCE.md`.

**Test.** A gametest for the new recipes, in the style of the existing purification ones, and one real
check that the Smelting upgrade purifies a bottle inside a backpack. The component ingredient is the
part to watch.

### 6. Waterskin and bowl as fluid containers

**Problem.** The waterskin and the terracotta water bowl have no NeoForge fluid capability, so the Tank
upgrade, the Pump upgrade, Create and any other fluid mod cannot fill or empty them.

**Approach.** Register `Capabilities.FluidHandler.ITEM` for both in `RegisterCapabilitiesEvent`, in
`src/main/neoforge`. Neither needs Sophisticated:

- **Waterskin.** One serving is 250 mB, the size of a bottle. Filling mixes grades the way the waterskin
  already does (serving-weighted and rounding down, any salt makes it all salty). Draining hands out
  water stamped with the skin's grade.
- **Terracotta water bowl.** 250 mB, empty bowl to full bowl and back.

**Watch out for.** The Create mixins already give these items quality through Create's own item
filling and emptying. Once they have a capability, check that Create does not fill them twice, once
through its generic path and once through the capability. The Fabric nodes would want the same through
Fabric's Transfer API; that is separate work and not needed for Sophisticated.

**Test.** A gametest for the capability itself (fill, drain, grade mixing, salt), which runs without
any other mod, plus the Tank agent script with a waterskin.

### 5. Drinking upgrade

**What it is.** A new upgrade, the thirst version of the Feeding upgrade. It drinks from the backpack
when the thirst bar is low: bottles, waterskins, bowls and other drinks with a thirst value, and water
straight from a Tank upgrade in the same backpack (250 mB per drink).

**Settings,** mirroring Feeding:

- drink at: half a sip missing, the full value missing, or any time;
- the lowest grade it will drink (default Clean), so it never picks dirty or salt water on its own;
- a filter, and a larger one on an Advanced Drinking upgrade.

**Behaviour.** Prefer the cleanest water. Respect `ThirstManager.canDrinkWater`. Go through
`ThirstManager.drinkItem`, so sickness, advancements and the thirst values are the same as drinking by
hand. Give the empty container back into the backpack, as Feeding does.

**What it needs.** This is the first part that registers content, so the integration gets an
entrypoint of its own, a second `@Mod` class like `CreateEntrypoint`, reached only after
`SophisticatedPresence.isPresent()`.

- an item extending `UpgradeItemBase` with an `UpgradeType`, and a wrapper implementing
  `ITickableUpgrade` and `IFilteredUpgrade`;
- an `UpgradeContainerType` registered with `UpgradeContainerRegistry`, and a GUI tab registered with
  `UpgradeGuiManager` on the client, which means a client source directory as well;
- the `sophisticatedbackpacks:upgrade` item tag (and Sophisticated Storage's, if it is to fit there),
  a recipe behind a `neoforge:mod_loaded` condition, a texture, and lang keys in all nine languages.

**Open questions.** The recipe and its cost; whether a Tank of water should count at all, since it
makes a backpack an unlimited canteen as long as a pump fills it; and whether drinking should wait a
cooldown like Feeding's 100 ticks.

**Test.** An agent script like the Feeding one, with a dirty and a clean bottle to check it picks the
clean one and leaves the dirty one.

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
