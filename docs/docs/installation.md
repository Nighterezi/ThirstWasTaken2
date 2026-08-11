# Installation

## Download

Open the latest green run of the
[build workflow](https://github.com/Nighterezi/ThirstWasTaken2/actions/workflows/build.yml) and
take `ThirstWasTaken2-<version>.jar` from its artifacts. Skip the `-sources` jar, it is for
developers. There is no installer, the jar is the whole mod.

## Supported versions

Every Minecraft version the mod supports has its own branch, and each branch has its own release
numbering. A release is named `<minecraft>-<mod>`, for example `26.2-1.0.0`.

That means the highest number is not always the newest build. If the 1.21.11 branch gets a fix that
the 26.2 branch already had, it ships as `1.21.11-1.0.1` while 26.2 is still on `26.2-1.0.0`. Read
the Minecraft column first, then take the highest release in that row.

| Release | Branch | Minecraft | Fabric Loader | Fabric API | Java | Status |
|---|---|---|---|---|---|---|
| `26.2-1.0.0` | `main` | 26.2 | 0.19.3 or newer | 0.156.0+26.2 or newer | 25 | Active |

A branch marked Active still gets fixes. One marked Frozen builds and runs, but new features land
only on the Active branches.

### Reading the table

- **Fabric Loader** is the number the launcher shows in the profile name. Newer is always fine.
- **Fabric API** must match the Minecraft version. The `+26.2` suffix is the Minecraft version it
  was built for, so `0.156.0+26.2` will not load on 1.21.11.
- **Java** is the minimum. Minecraft 26.2 already ships a Java 25 runtime, so the bundled runtime is
  enough unless you run a server with your own JDK.
- The mod never targets a snapshot. A row appears once the branch builds against a full release.

## Compatible mods

None of these are required. The mod loads and plays exactly the same without them, it only unlocks
the extra behaviour listed here when it finds one.

| Mod | Version tested | Minecraft | What it adds | If it is missing |
|---|---|---|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | 0.156.0+26.2 | 26.2 | Required. Events, networking and the HUD hooks the mod is built on. | The mod will not load. |
| [Mod Menu](https://modrinth.com/mod/modmenu) | 20.0.1 | 26.2 | A Config button in the Mods list that opens the [settings screen](/docs/configuration). | Edit `config/thirstwastaken2.json` by hand. |

::: warning Create Fly is not supported yet
The Sand Filter is broken on this release and the Create Fly integration is off. Installing Create
changes nothing: the block cannot be crafted and bulk purification is unavailable. Support is planned
for a later release.
:::

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
logs `ThirstWasTaken2 initialized for Minecraft 26.2`.

Thirst is stored on the player, so existing worlds work. Everyone who has never been tracked before
starts at full thirst.
