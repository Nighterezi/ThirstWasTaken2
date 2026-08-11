<!--
Description for the ThirstWasTaken2 project page on Modrinth.
Image links are absolute so they work outside this repository.

Modrinth summary:
  Adds a survival thirst bar, drinking, and water purity.
-->

<p align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">
</p>

ThirstWasTaken2 is a fork of the original
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
- `/thirst` commands for server administrators

| Thirst bar | Water purity |
|---|---|
| ![Thirst bar above the hunger bar](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png) | ![Four water-purity levels shown in item tooltips](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png) |

## Items

| Item | Use |
|---|---|
| Clay Bowl | Smelt it to make a Terracotta Bowl |
| Terracotta Bowl | Collects still or flowing water |
| Terracotta Water Bowl | Stores drinkable water and its purity level |

![The ThirstWasTaken2 creative tab containing the clay bowl, terracotta bowl and terracotta water bowl](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/creative-tab.png)

## Water purity

Water receives a purity level when collected in a bottle, bucket or terracotta bowl.

| Water source | Purity |
|---|---|
| Still water at ordinary heights | Dirty |
| Flowing water at ordinary heights | Slightly dirty |
| Still water above y 100 or below y 48 | Slightly dirty |
| Flowing water above y 100 or below y 48 | Acceptable |

Unsafe water can cause Nausea, Hunger or Poison. It still restores thirst.

Water can be purified in a furnace or on a campfire.

![Water bottles and bowls being purified over a campfire](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/boilling-water.png)

| Input | Output |
|---|---|
| Dirty | Acceptable |
| Slightly dirty | Purified |
| Acceptable | Purified |

## Requirements

| Component | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | Required |
| Java | 25 |
| Mod Menu | Optional, 20.0.1 tested |
| AppleSkin | Optional, 3.0.10+mc26.2 tested |
| Cloth Config | Optional, needed for AppleSkin's Mod Menu screen |

Install ThirstWasTaken2 and Fabric API on both the client and server.

## Configuration

Settings can be changed through Mod Menu or in `config/thirstwastaken2.json`.

Gameplay settings are controlled by the server. HUD settings are controlled by each client.

## Commands

| Command | Description |
|---|---|
| `/thirst query <player>` | Show thirst and quenched values |
| `/thirst set <players> <thirst> <quenched>` | Set thirst and quenched values |
| `/thirst enable <players> <true/false>` | Enable or disable thirst |

These commands require game master permission.

## Current integration limitations

- The Create Sand Filter is disabled because the Create Fly integration is currently broken.
- Jade does not currently display water purity.
- Cold Sweat, Farmer's Respite, Brewin' and Chewin', Tough As Nails, Supplementaries and Botania do
  not yet have compatible Minecraft 26.2 Fabric releases. Their items are already configured and
  will be supported when compatible versions become available.

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
