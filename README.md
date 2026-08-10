# Thirst Was Taken Fabric

[![Build](https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml/badge.svg)](https://github.com/Nighterezi/ThirstWasTakenFabric/actions/workflows/build.yml)

A Fabric port of **Thirst Was Taken** for Minecraft 26.2. The mod adds a persistent thirst bar,
hydration exhaustion, dehydration damage, drinkable water, watery foods and related survival mechanics.

## Credits

- Original mod and design: **ghen** and the original contributors — [ghen-git/Thirst-Mod](https://github.com/ghen-git/Thirst-Mod)
- Fabric port: **Nighter** — [Nighterezi/ThirstWasTakenFabric](https://github.com/Nighterezi/ThirstWasTakenFabric)

This port preserves credit to the original project and is not presented as the original author's official Fabric release.

## Requirements

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.156.0+26.2 or newer compatible release
- Java 25

## Features currently ported

- Persistent and client-synchronized thirst, quenched and exhaustion data
- Thirst depletion based on vanilla exhaustion
- Dehydration damage and low-thirst sprint prevention
- Drinking rain and shift-right-clicking a water source with an empty hand
- Hydration from drinks, common watery foods and the terracotta water bowl
- Thirst HUD
- `/thirst set` and `/thirst enable` administrator commands
- English, Vietnamese and the translations inherited from the original project

Minecraft automatically selects the matching language file from the client's language setting. Selecting
**Tiếng Việt (Việt Nam)** loads `vi_vn.json`; no server-side language setting is required.

Forge-specific integrations from the original 1.19.2 version are not loaded in this Fabric 26.2 port.

## Build

On Windows:

```bat
scripts\build.bat
```

The PowerShell equivalent is `powershell -ExecutionPolicy Bypass -File scripts/build.ps1` on systems
whose execution policy blocks local scripts.

On Linux or macOS:

```bash
./scripts/build.sh
```

The release JAR is written to `build/libs/`.
