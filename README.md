<p align="center">
  <img src=".github/assets/banner.png" alt="ThirstWasTaken2" width="420">
</p>

<p align="center">
  A survival thirst and hydration mod for Minecraft on Fabric.
</p>

<p align="center">
  <a href="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml"><img src="https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-62B47A" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4" alt="Fabric">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="MIT license"></a>
</p>

> A fork of **[Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod)** by
> **[ghen](https://github.com/ghen-git)**, rebuilt for Fabric and extended by
> **[Nighter](https://github.com/Nighterezi)**. The banner, icon and translations come from the
> original project. See [what this fork changes](FORK-STATUS.md).

ThirstWasTaken2 adds a survival thirst system designed to fit naturally into modpacks. Movement and
other exhausting activities consume hydration, drinks and watery foods restore it, and severe
dehydration can slow or damage the player.

**[Documentation](https://nighterezi.github.io/ThirstWasTaken2/)** ·
**[Fork status](FORK-STATUS.md)** · **[Code map](CODEMAP.md)**

## Highlights

- Persistent thirst, quenched hydration, and exhaustion data
- Four grades of water purity, with dirty water that can make you sick
- Dehydration damage and sprint prevention at low hydration
- Hydration from potions, drinks, watery foods, and water bowls
- Shift-right-click a water source with an empty hand to drink
- Rain drinking when looking upward
- Extra dehydration in the Nether

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
| ![The config screen](docs/public/screenshots/config-1.png) | ![The ThirstWasTaken2 creative tab](docs/public/screenshots/creative-tab.png) |

## Supported versions

Every Minecraft version has its own branch. Releases are named `<minecraft>-<mod>`, so a higher mod
number does not always mean a newer Minecraft: `1.21.11-1.0.1` can ship after `26.2-1.0.0` when a
branch catches up later. Pick the row that matches your Minecraft version, not the highest number.

| Release | Branch | Minecraft | Fabric Loader | Fabric API | Java | Optional mods (tested build) | Status |
|---|---|---|---|---|---|---|---|
| `26.2-1.0.0` | [`main`](https://github.com/Nighterezi/ThirstWasTaken2/tree/main) | 26.2 | 0.19.3+ | 0.156.0+26.2 | 25 | Mod Menu `20.0.1` | Active |

Install the mod and Fabric API on **both** the client and the server. Mod Menu is never required, the
config screen falls back to the JSON file without it. Full requirements and the per-mod notes are in
the [installation guide](https://nighterezi.github.io/ThirstWasTaken2/docs/installation).

> [!NOTE]
> **Create Fly is not supported yet.** The Sand Filter is currently broken and will come back in a
> later release. Nothing else in the mod depends on it.

Download the JAR from a successful
[GitHub Actions build](https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml),
then place it in the `mods` folder.

## Commands

| Command                                     | Action |
|---------------------------------------------|---|
| `/thirst query <player>`                    | Show current thirst and quenched levels |
| `/thirst set <players> <thirst> <quenched>` | Set thirst and quenched levels |
| `/thirst enable <players> <true/false>`     | Enable or disable thirst ticking |

These commands require game master permission.

## Supported languages

Nine translations ship with the mod. Each client sees the one its game is already set to, so there is
nothing to configure on the server.

English · French · Japanese · Korean · Polish · Russian · Vietnamese · Simplified Chinese ·
Traditional Chinese

Translation files live in `src/main/resources/assets/thirstwastaken2/lang/`.

## Build

```bash
./gradlew build
```

The ready-to-use JAR is created in `build/libs/`.

## License

[MIT](LICENSE), the same licence as the original project credited at the top of this page.
