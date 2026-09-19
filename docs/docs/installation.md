# Installation

## Download

Download the mod from [Modrinth](https://modrinth.com/mod/thirst-was-taken-2).

## Supported versions

Every Minecraft version and loader gets its own file. The suffix on the file name is the Minecraft
version, and NeoForge files end in `-neoforge`, for example `ThirstWasTaken2-1.0.7+1.21.11.jar` and
`ThirstWasTaken2-1.0.7+1.21.11-neoforge.jar`.

### Fabric

| Minecraft | File suffix | Fabric Loader | Fabric API | Java |
|---|---|---|---|---|
| 26.2 | `+26.2` | 0.19.5 or newer | 0.160.0+26.2 or newer | 25 |
| 26.1, 26.1.1, 26.1.2 | `+26.1.2` | 0.19.5 or newer | 0.155.3+26.1.2 or newer | 25 |
| 1.21.11 | `+1.21.11` | 0.19.5 or newer | 0.141.6+1.21.11 or newer | 21 |
| 1.21, 1.21.1 | `+1.21.1` | 0.19.5 or newer | 0.116.17+1.21.1 or newer | 21 |

Fabric API is required and must match the Minecraft version.

### NeoForge

| Minecraft | File suffix | NeoForge | Java |
|---|---|---|---|
| 26.2 | `+26.2-neoforge` | 26.2.0.88 or newer | 25 |
| 26.1, 26.1.1, 26.1.2 | `+26.1.2-neoforge` | 26.1.2.109 or newer | 25 |
| 1.21.11 | `+1.21.11-neoforge` | 21.11.45 or newer | 21 |
| 1.21.1 | `+1.21.1-neoforge` | 21.1.250 or newer | 21 |

NeoForge needs nothing else. The 1.21.1 file does not run on 1.21.

The Java version is the minimum on both loaders. The runtime Minecraft ships with is enough.

### Minecraft 1.21 and 1.21.1

- Sea water in a bottle or bucket looks like ordinary water. Its tooltip still reads Salty.
- The droplets in item tooltips have a shadow.

## Compatible mods

All optional except Fabric API. Versions are listed in the order 26.2, 26.1.x, 1.21.11, 1.21.1.

### Fabric

| Mod | Versions built with | What it adds |
|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.160.0+26.2, 0.155.3+26.1.2, 0.141.6+1.21.11, 0.116.17+1.21.1 | Required. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 20.0.2, 18.0.1, 17.0.0, 11.0.4 | A Config button for the [settings screen](/docs/configuration). |
| [AppleSkin](https://modrinth.com/mod/appleskin) | 3.0.10+mc26.2, 3.0.10+mc26.1.2, 3.0.8+mc1.21.11, 3.0.6+mc1.21 | The quenched outline on the thirst bar, droplet rows in tooltips, and the exhaustion strip. |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.2.155, 26.1.154, 21.11.153, 15.0.140 | AppleSkin's own settings screen. |
| [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated) | 26.2-3.6.26, 26.1-3.6.26, 1.21.11-3.6.16, 1.21.1-3.3.6 | Thirst from its [drinks and meals](/docs/features/farmers-delight), Pure water from the Cooking Pot, and no drain under Nourishment. |
| [Create Fly](https://modrinth.com/mod/create-fly) | 26.2-rc-2-6.0.9-1, 26.1.2-6.0.9-4 | The [Sand Filter](/docs/features/create). 26.2 and 26.1.2 only. |
| [Jade](https://modrinth.com/mod/jade) | 26.2.11, 26.1.11, 21.1.6, 15.10.6 | The [grade of the water](/docs/features/water-purity#checking-water-with-jade) under the crosshair. Client only. |

### NeoForge

The settings screen opens from the Config button in NeoForge's Mods list, with no extra mod.

| Mod | Versions built with | What it adds |
|---|---|---|
| [AppleSkin](https://modrinth.com/mod/appleskin) | 3.0.10+mc26.2, 3.0.9+mc26.1, 3.0.8+mc1.21.11, 3.0.9+mc1.21 | The same as on Fabric. |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.2.155, 26.1.154, 21.11.153, 15.0.140 | AppleSkin's own settings screen. |
| [Jade](https://modrinth.com/mod/jade) | 26.2.10, 26.1.10, 21.1.7, 15.10.6 | The same as on Fabric. Client only. |
| [Create](https://modrinth.com/mod/create) | 6.0.10 | The [Sand Filter](/docs/features/create). 1.21.1 only. |
| [Farmer's Delight](https://modrinth.com/mod/farmers-delight) | 1.21.1-1.3.4 | The same as on Fabric. 1.21.1 only. |

Other food mods usually work as they are. Drinks their mod marks as drinks restore thirst, and any
item can be given a value in [Configuration](/docs/configuration).

## Where the mod goes

Put the jar in the `mods` folder of the server and of every client. A client without the mod does
not see the bar.

## Languages

The mod follows each player's game language. Nine are included: English, French, Japanese, Korean,
Polish, Russian, Vietnamese, Simplified Chinese and Traditional Chinese.

![Item tooltips with the game set to Simplified Chinese](/screenshots/chinese-tooltips.png)

## First run

The first launch writes `config/thirstwastaken2.json` with the defaults. Existing worlds work, and
every player starts at full thirst.
