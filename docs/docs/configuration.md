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
[Cold Sweat](/docs/features/cold-sweat#climate).

## Water

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
