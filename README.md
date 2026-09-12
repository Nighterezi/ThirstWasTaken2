<p align="center">
  <img src=".github/assets/banner.png" alt="ThirstWasTaken2" width="420">
</p>

<p align="center">
  <a href="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml"><img src="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <a href="https://modrinth.com/mod/thirst-was-taken-2"><img src="https://img.shields.io/badge/Modrinth-thirst--was--taken--2-00AF5C?logo=modrinth&logoColor=white" alt="Modrinth"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2%20%7C%2026.1%20%7C%201.21.11-62B47A" alt="Minecraft 26.2, 26.1 and 1.21.11">
  <img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4" alt="Fabric">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT license"></a>
</p>

ThirstWasTaken2 is a fork of the original
[Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by
[**ghen**](https://github.com/ghen-git). It adds a survival thirst bar, drinking, and water purity to
Minecraft and further extends the original mod.

[Modrinth](https://modrinth.com/mod/thirst-was-taken-2) ·
[Documentation](https://nighterezi.github.io/ThirstWasTaken2/) ·
[Fork changes](FORK-STATUS.md) · [Changelog](CHANGELOG.md)

## Features

- Thirst, quenched and exhaustion
- Faster thirst loss from activity, hot biomes and the Nether
- Damage, disabled sprinting and disabled natural healing when dehydrated
- Drinking from water sources, potions, foods, water bowls and a reusable waterskin
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

There is one download per Minecraft version, named after it, for example
`ThirstWasTaken2-1.0.3+1.21.11.jar`.

| Component | Minecraft 26.2 | Minecraft 26.1.x | Minecraft 1.21.11 |
|---|---|---|---|
| Java | 25 | 25 | 21 |
| Fabric Loader | 0.19.3 or newer | 0.19.3 or newer | 0.19.3 or newer |
| Fabric API | 0.160.0+26.2 | 0.155.3+26.1.2 | 0.141.6+1.21.11 |
| Mod Menu | Optional, 20.0.1 tested | Optional, 18.0.0 tested | Optional, 17.0.0 tested |
| AppleSkin | Optional, 3.0.10+mc26.2 tested | Optional, 3.0.10+mc26.1.2 tested | Optional, 3.0.8+mc1.21.11 tested |
| Cloth Config | Optional, needed for AppleSkin's Mod Menu screen | Same | Same |

Download ThirstWasTaken2 from [Modrinth](https://modrinth.com/mod/thirst-was-taken-2) and install it along with Fabric API on both the client and server. Put the downloaded JAR in the
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

- Create is not supported in this release. The Sand Filter and Builder's Tea were removed and will
  return in a future release.
- Jade does not currently display water purity.
- Cold Sweat, Farmer's Respite, Brewin' and Chewin', Tough As Nails, Supplementaries and Botania do
  not yet have compatible Fabric releases on Minecraft 26.x. Their items are already configured and
  start working as soon as those mods are available.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese are included.

## Build

```bash
./gradlew buildAndCollect
```

One JAR per supported Minecraft version is created in `build/libs/`. To build a single version, use
`./gradlew ":1.21.11:build"`.

## License

ThirstWasTaken2 is available under the [MIT License](LICENSE).
