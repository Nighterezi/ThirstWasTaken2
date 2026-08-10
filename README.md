<p align="center">
  <img src=".github/assets/banner.png" alt="Thirst Was Taken" width="352">
</p>

<h1 align="center">Thirst Was Taken Fabric</h1>

<p align="center">
  A lightweight thirst and hydration mod for Minecraft 26.2 on Fabric.
</p>

<p align="center">
  <a href="https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml"><img src="https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-62B47A" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4" alt="Fabric">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT license"></a>
</p>

> A Fabric port of the original [Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by ghen.

> [View the Fabric port status, implemented changes, and remaining work](FABRIC-PORT.md).

Thirst Was Taken adds a survival thirst system designed to fit naturally into modpacks. Movement and other exhausting activities consume hydration, drinks and watery foods restore it, and severe dehydration can slow or damage the player.

## Highlights

- Persistent thirst, quenched hydration, and exhaustion data
- A client-synchronized thirst HUD
- Dehydration damage and sprint prevention at low hydration
- Hydration from potions, drinks, watery foods, and water bowls
- Shift-right-click a water source with an empty hand to drink
- Rain drinking when looking upward
- Extra dehydration in the Nether
- Administrator commands for setting or enabling thirst
- Client language detection with 9 bundled translations

## Screenshots

Thirst sits above the hunger bar and drains as you play. The reserve behind it is drawn as a lighter
outline on the droplets.

![The thirst bar above the hunger bar, part drained](docs/public/screenshots/thirst-bar.png)

Anything that restores thirst shows it as droplets instead of numbers. The water inside each droplet
is the hydration, the outline around it is the quenched, so one row carries both values.

| Item tooltip | The four grades of water |
|---|---|
| ![The tooltip of a water bottle](docs/public/screenshots/item-tooltip.png) | ![Tooltips reading Dirty, Slightly Dirty, Acceptable and Purified](docs/public/screenshots/water-purity.png) |

Look straight up in the rain to drink, or run the bar to empty and find out what dehydration costs.

| Drinking the rain | Running on empty |
|---|---|
| ![Rain falling over a forest](docs/public/screenshots/drinking-in-rain.png) | ![An empty thirst bar and two and a half hearts](docs/public/screenshots/dehydration.png) |

Every setting has a widget in Mod Menu, and the mod's items have their own creative tab.

| Settings | Creative tab |
|---|---|
| ![The config screen](docs/public/screenshots/config-1.png) | ![The Thirst was Taken creative tab](docs/public/screenshots/creative-tab.png) |

## Install

Install the following on both the client and server:

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.156.0+26.2 or another compatible 26.2 release
- Java 25

Download the JAR from a successful [GitHub Actions build](https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml), then place it in the `mods` folder.

## Commands

| Command                                     | Action |
|---------------------------------------------|---|
| `/thirst query <player>`                    | Show current thirst and quenched levels |
| `/thirst set <players> <thirst> <quenched>` | Set thirst and quenched levels |
| `/thirst enable <players> <true/false>`     | Enable or disable thirst ticking |

These commands require game master permission.

## Languages

Minecraft automatically uses the language selected by each client. No server-side language option is needed.

- English
- French
- Japanese
- Korean
- Polish
- Russian
- Vietnamese
- Simplified Chinese
- Traditional Chinese

Translation files are located in `src/main/resources/assets/thirstwastaken/lang/`.

## Build

```bash
./gradlew build
```

The ready-to-use JAR is created in `build/libs/`.

## Credits

- Original author: [ghen](https://github.com/ghen-git)
- Original source: [ghen-git/Thirst-Mod](https://github.com/ghen-git/Thirst-Mod)
- Fabric port: [Nighter](https://github.com/Nighterezi)
- Fabric port source: [Nighterezi/ThirstWasTakenFabric](https://github.com/Nighterezi/ThirstWasTakenFabric)

The banner, icon and translations originate from the original project. The screenshots were taken in
this port.

## License

[MIT](LICENSE). See the original project and its contributors in the credits above.
