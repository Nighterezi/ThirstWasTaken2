---
outline: [2, 3]
---

# Configuration

Settings live in `config/thirstwastaken2.json`, written on first launch. They can also be changed in
game: through Mod Menu on Fabric, or the Mods list on NeoForge.

![The config screen, with a tab for each group of settings on the left](/screenshots/config/config-screen.png)

- Pick a group from the tabs on the left, or type in the search box to find a setting on any page.
- A setting you have changed is shown in amber. The arrow button next to it puts it back.
- **Reset to Defaults** resets the whole page, **Done** saves, **Cancel** discards.
- A file edited by hand is read on the next start.

## Thirst

### thirstDepletionModifier

Default `1.2`, shown as `120%`. The base drain speed, before biome changes. `0` stops thirst draining.

### thirstDepletionInPeaceful

Default `false`. When off, thirst refills on its own on Peaceful.

### preventSprintingWhenThirsty

Default `true`. Stops sprinting at 6 thirst or below.

### dehydrationHaltsHealthRegen

Default `true`. Stops natural healing until thirst is nearly full. See
[Running low](/docs/features/thirst-and-quenched#running-low).

### coldSweatClimate

Default `true`. With Cold Sweat installed, the drain follows the temperature Cold Sweat measures around
the player, hearths and shade included, instead of the biome's. Does nothing without Cold Sweat. See
[Cold Sweat](/docs/integrations/cold-sweat#climate).

## Water

![The Water page scrolled down to the new settings: quenched by grade, sea water, rain and dripstone](/screenshots/config/config-water.png)

### sicknessPreset

Default `REALISTIC`. How bad water makes players ill.

- `REALISTIC`: Upset Stomach or Poisoning, by difficulty and grade. See
  [Drinking bad water](/docs/features/water-purity#drinking-bad-water).
- `CLASSIC`: the Nausea and Poison from before, the same on every difficulty.

| Grade | Nausea | Nausea lasts | Poison, 10 seconds |
|---|---|---|---|
| Dirty | 100% | 12 seconds | 30% |
| Murky | 50% | 8 seconds | 10% |
| Clean | 5% | 5 seconds | none |

### defaultPurity

Default `2`, Clean. The grade for water that has none, such as drinks from other mods.

### canDrinkByHand

Default `true`. Sneak and use an empty hand on water to drink.

### quenchedPercent

Default `[0, 50, 100, 100]`. How much of a drink's quenched water of each grade gives, Dirty first,
then Murky, Clean and Pure. Bad water fills the bar but does not keep it full. Shown as four sliders.

### enableSeaWater

Default `true`. Ocean and beach water is salty. Off, it is graded like any other water.

### seaWaterNauseaSeconds and seaWaterParchedSeconds

Default `8` and `30`. How long a drink of sea water gives Nausea and Parched. `0` gives none.

### enableRainCollection

Default `true`. Rain fills hanging pots, and rain in a cauldron gets `rainwaterPurity`. Off, pots
ignore rain and rain in a cauldron has no grade, so it counts as `defaultPurity`.

### rainwaterPurity and dripstonePurity

Default `2`, Clean, and `3`, Pure. The grade of collected rain and of water a pointed dripstone drips
into a cauldron.

## AppleSkin

These settings are client-side and only used while AppleSkin is installed. The exhaustion strip
follows AppleSkin's **Food Exhaustion HUD Underlay** setting.

![The AppleSkin page, with a live preview of the thirst bar and a drink's tooltip](/screenshots/config/config-appleskin.png)

### appleskinQuenchedOverlay

Default `DIAMOND`. The quenched outline colour: `DIAMOND`, `ICE`, `GOLD`, `APPLESKIN` or `LEGACY`, the
blue outline of the original Thirst Was Taken. `OFF` hides the outline and the exhaustion strip.

### appleskinTooltipDroplets

Default `true`. Shows the thirst and quenched droplets in tooltips.

## Item values

### drinks and foods

Two lists of item ids and their thirst and quenched. The **Item Values** page shows one row per item,
with a box for each number, a switch that puts the item in `itemBlacklist`, and a row to add an item.
In the file they look like this:

```json
"drinks": {
  "minecraft:potion": [6, 8],
  "thirstwastaken2:terracotta_water_bowl": [4, 5]
}
```

Ids for mods that are not installed are ignored. Add entries to support another mod.

### itemBlacklist

Empty by default. Items listed here restore nothing.

### enableDrinkTagMatching

Default `true`. Items their mod marks as drinks restore `drinkTagValue`. Items in `drinks` or `foods`
keep their own value.

### drinkTagValue

Default `[6, 8]`, the same as a water bottle.

### enableKeywordMatching

Default `false`. Guesses a value from the item id, so a `strawberry_juice` from any mod counts as a
drink. Guesses can be wrong, but it covers a large modpack quickly.

### drinkKeywords, soupKeywords and fruitKeywords

Words matched against the item id, separated by `|`. Matches are worth `keywordDrinkValue`,
`keywordSoupValue` or `keywordFruitValue`. Drinks are checked first, then soups, then fruit.

### keywordBlacklist

Words that stop a guess, so `melon_seed` is not treated as fruit. Only applies to guesses.

## Mod items

![The Mod Items page: one switch per item, under a note that changes apply after /reload](/screenshots/config/config-mod-items.png)

For a modpack that brings its own canteen or pot. A switch that is off stops the item being crafted
and hides it from the creative tab. Items that already exist keep working, and the item stays in the
game, so worlds that hold one still load.

Changes apply after `/reload`, or on rejoining a singleplayer world. A dedicated server reads the file
only on start, so it needs a restart. Recipe viewers such as JEI and EMI still list a switched-off
item.

### enableBowls

Default `true`. The Clay Bowl, the Terracotta Bowl and the filled bowl, together, since one is no use
without the others. Off also removes boiling water in a bowl.

### enableWaterskin

Default `true`. The Waterskin.

### enableCopperCanteen

Default `true`. The Copper Canteen.

### enableIronFlask

Default `true`. The Iron Flask, and cleaning its water in a furnace.

### enableCopperHangingPot and enableIronHangingPot

Default `true`. The Copper Hanging Pot and the Iron Hanging Pot, one switch each.

## Containers

![The Containers page: canteen and flask capacity, boiling in hand, and boil times](/screenshots/config/config-containers.png)

### copperCanteenCapacity and ironFlaskCapacity

Default `4` and `6`, from `1` to `6`. How many drinks each holds when full. One that already holds
more keeps its water but takes no more.

### enableBoilingInHand

Default `true`. Holding use with a Copper Canteen or Iron Flask on a lit campfire boils its water.

### copperCanteenBoilSeconds and ironFlaskBoilSeconds

Default `3` and `4`. Seconds each drink takes to boil over a campfire.

### copperHangingPotBoilSeconds and ironHangingPotBoilSeconds

Default `4` and `6`. Seconds each drink in a hanging pot takes to boil.
