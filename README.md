<div align="center">

<img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">

[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png)](https://modrinth.com/mod/thirst-was-taken-2)
[![ghpages](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/ghpages_64h.png)](https://nighterezi.github.io/ThirstWasTaken2/)
[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_64h.png)](https://discord.gg/YwD9Xv7Beu)

<br>

A thirst mod for Fabric and NeoForge. It adds a survival thirst bar, drinking, and water purity to Minecraft. Available in 9 languages, with built-in support for AppleSkin, Jade, Farmer's Delight, Mod Menu and drinks from other mods.

</div>

## Features

- A thirst bar with a quenched reserve, drained faster by activity, heat and the Nether.
- Four water grades plus salty sea water. Unsafe water can make players sick.
- Clean water by boiling it in a furnace, on a campfire, or in a Copper or Iron Hanging Pot.
- Terracotta bowls and a three-drink waterskin for early game drinking.
- Optional support for AppleSkin, Jade, Farmer's Delight, Create Fly and Create.

| Thirst bar | Water grades |
|---|---|
| ![The thirst bar above the hunger bar](docs/public/screenshots/thirst-food-bars.png) | ![A water bottle tooltip stepping through every grade](docs/public/screenshots/water-tooltips.gif) |

| Iron Hanging Pot | Config screen |
|---|---|
| ![An Iron Hanging Pot boiling water over a campfire](docs/public/screenshots/iron-hanging-pot.png) | ![The config screen with its live preview](docs/public/screenshots/config-screen.png) |

Every feature is explained on the [documentation site](https://nighterezi.github.io/ThirstWasTaken2/).

## Requirements

There is one download per Minecraft version, named after it, for example
`ThirstWasTaken2-1.0.7+1.21.11.jar`. The `+1.21.1` download also runs on Minecraft 1.21.

| Component | Minecraft 26.2 | Minecraft 26.1.x | Minecraft 1.21.11 | Minecraft 1.21.1 |
|---|---|---|---|---|
| Java | 25 | 25 | 21 | 21 |
| Fabric Loader | 0.19.5 or newer | 0.19.5 or newer | 0.19.5 or newer | 0.19.5 or newer |
| Fabric API | 0.160.0+26.2 | 0.155.3+26.1.2 | 0.141.6+1.21.11 | 0.116.17+1.21.1 |
| Mod Menu | Optional, 20.0.2 | Optional, 18.0.1 | Optional, 17.0.0 | Optional, 11.0.4 |
| AppleSkin | Optional, 3.0.10+mc26.2 | Optional, 3.0.10+mc26.1.2 | Optional, 3.0.8+mc1.21.11 | Optional, 3.0.6+mc1.21 |
| Cloth Config | Optional, needed for AppleSkin's Mod Menu screen | Same | Same | Same |
| Jade | Optional, 26.2.11 | Optional, 26.1.11 | Optional, 21.1.6 | Optional, 15.10.6 |
| Farmer's Delight Refabricated | Optional, 26.2-3.6.26 | Optional, 26.1-3.6.26 | Optional, 1.21.11-3.6.16 | Optional, 1.21.1-3.3.6 |
| Farmer's Delight (NeoForge only) | Not supported | Not supported | Not supported | Optional, 1.21.1-1.3.4 |
| Create Fly | Optional, 26.2-rc-2-6.0.9-1 | Optional, 26.1.2-6.0.9-4 | Not supported | Not supported |
| Create (NeoForge only) | Not supported | Not supported | Not supported | Optional, 6.0.10 |

Download ThirstWasTaken2 from [Modrinth](https://modrinth.com/mod/thirst-was-taken-2) and put it in the
`mods` folder on both the client and the server. Fabric also needs Fabric API. NeoForge files end in
`-neoforge`.

See the [installation guide](https://nighterezi.github.io/ThirstWasTaken2/docs/installation) for
more details.

## Configuration

Settings can be changed in game, through Mod Menu on Fabric or the Mods list on NeoForge, or in
`config/thirstwastaken2.json`.

Gameplay settings are controlled by the server. HUD settings are controlled by each client.

## Commands

| Command | Description |
|---|---|
| `/thirst query <player>` | Show thirst and quenched values |
| `/thirst set <players> <thirst> <quenched>` | Set thirst and quenched values |
| `/thirst enable <players> <true/false>` | Enable or disable thirst |

These commands require game master permission.

## Known limitations

- On Minecraft 1.21 and 1.21.1, sea water in bottles and buckets looks like ordinary water. Its
  tooltip still reads Salty.
- Builder's Tea does not restore thirst yet.

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

ThirstWasTaken2 is available under the [GNU General Public License v3.0](LICENSE). See
[CREDITS.md](CREDITS.md) for credits.
