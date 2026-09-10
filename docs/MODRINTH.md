<p align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">
</p>

ThirstWasTaken2 is [a fork](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md) of the original
[Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by
[**ghen**](https://github.com/ghen-git). It adds a survival thirst bar, drinking, and water purity to
Minecraft and further extends the original mod.

## Features

- Thirst, quenched hydration and exhaustion
- Faster thirst loss from activity, hot biomes and the Nether
- Damage, disabled sprinting and disabled natural healing when dehydrated
- Drinking from water sources, potions, foods, water bowls and a reusable waterskin
- Four water-purity levels with negative effects from unsafe water
- Water purification using furnaces and campfires
- Two-row thirst and quenched sprites plus purity information in item tooltips
- Optional AppleSkin exhaustion underlay on the thirst bar
- Configurable HUD position and gameplay settings
- Mod Menu configuration screen

| Thirst bar | Running dry |
|---|---|
| ![Thirst bar above the hunger bar, part drained](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png) | ![An empty thirst bar with health down to two hearts](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/dehydration.png) |

The bar drains as time passes, and drains faster while running, in hot biomes and in the Nether. At
zero it takes health, blocks sprinting and stops natural healing.

## Items

| Item | Use |
|---|---|
| Clay Bowl | Smelt it to make a Terracotta Bowl |
| Terracotta Bowl | Collects still or flowing water |
| Terracotta Water Bowl | Holds one drink and remembers its purity level |
| Waterskin | Holds three drinks, preserves water purity and can be refilled |

![The ThirstWasTaken2 creative tab containing its bowls and waterskin](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/creative-tab.png)

A waterskin is worth the leather. Bowls and bottles hold one drink each; the waterskin holds three
and keeps them in a single slot.

![The waterskin recipe, three leather and one string in a crafting table](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/waterskin-recipe.png)

## Water purity

Water receives a purity level when collected in a bottle, bucket or terracotta bowl. Tooltips show
that level, along with how much thirst and quenched hydration a drink restores.

| Water purity | Hydration values |
|---|---|
| ![Water bottle tooltips showing Dirty, Slightly Dirty, Acceptable and Purified](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png) | ![A water bottle tooltip showing filled thirst droplets and outlined quenched droplets](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/item-tooltip.png) |

The biome the water sits in sets the grade.

| Where the water comes from | Usual grade |
|---|---|
| Mountains | Acceptable |
| Rivers | Slightly dirty |
| Most other biomes | Slightly dirty |
| Swamps, jungles, savannas and badlands | Dirty |
| Oceans and beaches | Salty |

Hot biomes, and mud, farmland or a composter within a couple of blocks, make water worse. Cold
biomes and high or deep water make it better.

Unsafe water can cause Nausea, Hunger or Poison. It still restores thirst. Salt water is the
exception: it restores nothing, causes Nausea, and boiling does not fix it.

Water can be purified in a furnace or on a campfire.

![Water bottles and bowls being purified over a campfire](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/boilling-water.png)

| Input | Output |
|---|---|
| Dirty | Acceptable |
| Slightly dirty | Purified |
| Acceptable | Purified |

## Configuration

Settings can be changed through Mod Menu or in `config/thirstwastaken2.json`.

Gameplay settings are controlled by the server. HUD settings are controlled by each client.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese are included.

## Links

- [Documentation](https://nighterezi.github.io/ThirstWasTaken2/)
- [Fork changes](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
- [Changelog](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/CHANGELOG.md)
- [Source code](https://github.com/Nighterezi/ThirstWasTaken2)
- [Issue tracker](https://github.com/Nighterezi/ThirstWasTaken2/issues)
- [Original mod](https://modrinth.com/mod/thirst-was-taken)