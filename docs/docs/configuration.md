---
outline: [2, 3]
---

# Configuration

Settings live in `config/thirstwastaken2.json`, written on first launch. They can also be changed in
game: through Mod Menu on Fabric, or the Mods list on NeoForge.

![The config screen: a live preview of a tooltip and the bars, and one button per page](/screenshots/config-screen.png)

- The screen has a live preview and a **Reset to Defaults** button on every page.
- **Done** saves, **Cancel** discards.
- A file edited by hand is read on the next start.

::: tip
Only the HUD and AppleSkin settings are read from each player's own file. Everything else comes from
the server.
:::

## Thirst Depletion

### thirstDepletionModifier

Default `1.2`, shown as `120%`. The base drain speed, before biome changes. `0` stops thirst draining.

### netherThirstDepletionModifier

Default `3.0`. The drain speed in the Nether and any dimension where water evaporates. Replaces the
biome speed.

### fireResistanceDehydrationPercent

Default `50`. How much of the normal drain applies under Fire Resistance.

### thirstDepletionInPeaceful

Default `false`. When off, thirst refills on its own on Peaceful.

### depletesWhenNauseous

Default `true`. Nausea adds extra drain.

### dehydrationHaltsHealthRegen

Default `true`. Stops natural healing until thirst is nearly full. See
[Running low](/docs/features/thirst-and-quenched#running-low).

### preventSprintingWhenThirsty

Default `true`. Stops sprinting at 6 thirst or below.

## Drinking

### canDrinkByHand

Default `true`. Sneak and use an empty hand on water to drink.

### drinkByHandNeedsBothHandsEmpty

Default `false`. When on, drinking by hand also needs the other hand empty.

### handDrinkingThirst

Default `3`. Thirst restored by one drink by hand.

### handDrinkingQuenched

Default `2`. Quenched restored by one drink by hand, before the grade cuts it. See
[quenchedPercentByGrade](#quenchedpercentbygrade).

### extraThirstConvertsToQuenched

Default `true`. Thirst past a full bar becomes quenched.

## Water Purity

### defaultPurity

Default `2`, Clean. The grade for water that has none, such as drinks from other mods.

### rainwaterPurity

Default `2`, Clean. The grade of rain in a cauldron.

### dripstonePurity

Default `3`, Pure. The grade of dripstone water in a cauldron. See
[cauldrons](/docs/features/water-purity#mixing-and-cauldrons).

### copperPotSecondsPerServing

Default `4`, from 1 to 100. Seconds each serving takes to boil in a
[Copper Hanging Pot](/docs/features/water-purity#copper-hanging-pot).

### ironPotSecondsPerServing

Default `6`, from 1 to 100. The same for the
[Iron Hanging Pot](/docs/features/water-purity#iron-hanging-pot).

### quenchedPercentByGrade

Default `[0, 50, 100, 100]`, one percentage per grade from Dirty to Pure, from 0 to 100. How much of a
drink's quenched water of that grade gives, from any container or by hand. See
[Drinking bad water](/docs/features/water-purity#drinking-bad-water).

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

### sicknessEasy, sicknessNormal and sicknessHard

The chances `REALISTIC` uses on each difficulty. Each has three lists with one value per grade, Dirty,
Murky and Clean:

- `poisoningChance`: percent, from 0 to 100.
- `upsetStomachChance`: percent, from 0 to 100, for the drinks that did not poison.
- `upsetStomachLevel`: 1 or 2.

The defaults are in [Drinking bad water](/docs/features/water-purity#drinking-bad-water). Peaceful has
no table: it only gives the taste.

## HUD

Read from each player's own file.

### thirstBarXOffset

Default `0`, from `-200` to `200`. Moves the bar sideways, in pixels.

### thirstBarYOffset

Default `0`. Moves the bar up or down.

## AppleSkin

Only used while AppleSkin is installed. The exhaustion strip follows AppleSkin's **Food Exhaustion
HUD Underlay** setting.

![The HUD and AppleSkin page, with the preview under its title](/screenshots/config-hud.png)

### appleskinQuenchedOverlay

Default `DIAMOND`. The quenched outline colour: `DIAMOND`, `ICE`, `GOLD`, `APPLESKIN` or `LEGACY`, the
blue outline of the original Thirst Was Taken. `OFF` hides the outline and the exhaustion strip.

### appleskinTooltipDroplets

Default `true`. Shows the thirst and quenched droplets in tooltips.

## Item values

### drinks and foods

Two lists in the file only. Each entry is an item id and its thirst and quenched:

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
