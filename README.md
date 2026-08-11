<p align="center">
  <img src=".github/assets/banner.png" alt="ThirstWasTaken2" width="420">
</p>

<p align="center">
  <a href="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml"><img src="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-62B47A" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4" alt="Fabric">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT license"></a>
</p>

ThirstWasTaken2 is a fork of the original
[Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by
[**ghen**](https://github.com/ghen-git). It adds a survival thirst bar, drinking, and water purity to
Minecraft and further extends the original mod.

[Documentation](https://nighterezi.github.io/ThirstWasTaken2/) ·
[Fork changes](FORK-STATUS.md) · [Changelog](CHANGELOG.md)

## Features

- Thirst, quenched hydration and exhaustion
- Faster thirst loss from activity, hot biomes and the Nether
- Damage, disabled sprinting and disabled natural healing when dehydrated
- Drinking from water sources, rain, potions, foods and water bowls
- A reusable three-drink waterskin that preserves and mixes water purity
- Four water-purity levels with negative effects from unsafe water
- Water purification using furnaces and campfires
- Two-row thirst and quenched sprites plus purity information in item tooltips
- Optional AppleSkin exhaustion underlay on the thirst bar
- Configurable HUD position and gameplay settings
- Mod Menu configuration screen
- `/thirst` commands for server administrators

| Thirst bar | Water purity |
|---|---|
| ![Thirst bar above the hunger bar](docs/public/screenshots/thirst-bar.png) | ![Four water-purity levels shown in item tooltips](docs/public/screenshots/water-purity.png) |

## Requirements

| Component | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.156.0+26.2 |
| Java | 25 |
| Mod Menu | Optional, 20.0.1 tested |
| AppleSkin | Optional, 3.0.10+mc26.2 tested |
| Cloth Config | Optional, needed for AppleSkin's Mod Menu screen |

Install ThirstWasTaken2 and Fabric API on both the client and server. Put the downloaded JAR in the
`mods` folder.

See the [installation guide](https://nighterezi.github.io/ThirstWasTaken2/docs/installation) for
more details.

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

## Known limitations

- The Create Sand Filter is disabled because the Create Fly integration is currently broken.
- Jade does not currently display water purity.
- Cold Sweat, Farmer's Respite, Brewin' and Chewin', Tough As Nails, Supplementaries and Botania do
  not yet have compatible Minecraft 26.2 Fabric releases.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese are included.

## Build

```bash
./gradlew build
```

The JAR is created in `build/libs/`.

## License

ThirstWasTaken2 is available under the [MIT License](LICENSE).
