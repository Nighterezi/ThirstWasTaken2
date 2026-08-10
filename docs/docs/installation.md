# Installation

## Download

Open the latest green run of the
[build workflow](https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml) and
take `ThirstWasTakenFabric-<version>.jar` from its artifacts. Skip the `-sources` jar, it is for
developers. There is no installer, the jar is the whole mod.

## Requirements

| Thing | Version |
|---|---|
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.156.0+26.2 or newer |
| Java | 25 |

## Where the mod goes

Put the jar in the `mods` folder of both the **server** and every **client** that joins it.

The server decides how fast thirst drains and what the water is worth. The client draws the bar. A
player without the mod installed will not see the bar at all, so on a public server it belongs in
the pack rather than being optional.

## Mod Menu

Mod Menu is optional. With it installed, Thirst Was Taken gets a Config button in the Mods list that
opens the settings screen described in [Configuration](/docs/configuration). Without it, edit the
config file by hand.

## Create Fly

Also optional. If [Create Fly](https://modrinth.com/mod/create-fly) is present, the Sand Filter
block is registered and can be crafted. See [Water Purity](/features/water-purity#sand-filter).

Removing Create from a world that already has a Sand Filter placed leaves a missing block behind, so
break them first.

## Languages

Each client sees the mod in whatever language their game is set to. Nine are bundled: English,
French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional Chinese.
There is nothing to configure on the server.

## First run

Start the game or the server once. The mod writes `config/thirstwastaken.json` with its defaults and
logs `Thirst Was Taken Fabric initialized for Minecraft 26.2`.

Thirst is stored on the player, so existing worlds work. Everyone who has never been tracked before
starts at full thirst.
