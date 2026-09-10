
# Installation

## Download

Download the mod from **[Modrinth](https://modrinth.com/mod/thirst-was-taken-2)** (recommended).

Alternatively, open the latest green run of the
[build workflow](https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml) and
take `ThirstWasTaken2-<version>.jar` from its artifacts. Skip the `-sources` jar, it is for
developers. There is no installer, the jar is the whole mod.

## Supported versions

One release covers every supported Minecraft version, and they all share the same mod version. The
Minecraft version is the suffix on the file name, for example `ThirstWasTaken2-1.0.2+1.21.11.jar`.
Take the file whose suffix matches the game.

| Minecraft | File suffix | Fabric Loader | Fabric API | Java |
|---|---|---|---|---|
| 26.2 | `+26.2` | 0.19.3 or newer | 0.160.0+26.2 or newer | 25 |
| 26.1, 26.1.1, 26.1.2 | `+26.1.2` | 0.19.3 or newer | 0.155.3+26.1.2 or newer | 25 |
| 1.21.11 | `+1.21.11` | 0.19.3 or newer | 0.141.6+1.21.11 or newer | 21 |

### Reading the table

- **Fabric Loader** is the number the launcher shows in the profile name. Newer is always fine.
- **Fabric API** must match the Minecraft version. The `+26.2` suffix is the Minecraft version it
  was built for, so `0.160.0+26.2` will not load on 1.21.11.
- **Java** is the minimum. Minecraft ships a matching runtime, so the bundled one is enough unless
  you run a server with your own JDK.
- The mod never targets a snapshot. A row appears once the mod builds against a full release.

## Compatible mods

None of these are required. The mod loads and plays exactly the same without them, it only unlocks
the extra behaviour listed here when it finds one.

Versions tested are listed per Minecraft version, in the order 26.2, 26.1.x, 1.21.11.

| Mod | Versions tested | What it adds | If it is missing |
|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.160.0+26.2, 0.155.3+26.1.2, 0.141.6+1.21.11 | Required. Events, networking and the HUD hooks the mod is built on. | The mod will not load. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 20.0.1, 18.0.0, 17.0.0 | A Config button in the Mods list that opens the [settings screen](/docs/configuration). | Edit `config/thirstwastaken2.json` by hand. |
| [AppleSkin](https://modrinth.com/mod/appleskin) | 3.0.10+mc26.2, 3.0.10+mc26.1.2, 3.0.8+mc1.21.11 | A dithered thirst-exhaustion strip controlled by AppleSkin's HUD-underlay option. | The thirst bar renders normally without the strip. |
| [Cloth Config](https://modrinth.com/mod/cloth-config) | 26.2.155, 26.1.154, 21.11.153 | AppleSkin's configuration screen inside Mod Menu. | AppleSkin still works, but its Config button is unavailable. |

Anything not listed simply coexists. Food mods usually work without a patch: an item whose name
contains a drink, soup or fruit keyword picks up hydration on its own, and the per-item values in
[Configuration](/docs/configuration) cover whatever the keywords miss.

## Where the mod goes

Put the jar in the `mods` folder of both the **server** and every **client** that joins it.

The server decides how fast thirst drains and what the water is worth. The client draws the bar. A
player without the mod installed will not see the bar at all, so on a public server it belongs in
the pack rather than being optional.

## Languages

Each client sees the mod in whatever language their game is set to. Nine are bundled: English,
French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional Chinese.
There is nothing to configure on the server.

## First run

Start the game or the server once. The mod writes `config/thirstwastaken2.json` with its defaults and
logs `ThirstWasTaken2 initialized for Minecraft` followed by the version it was built for.

Thirst is stored on the player, so existing worlds work. Everyone who has never been tracked before
starts at full thirst.
