# Fabric port status

This document tracks the Minecraft 26.2 Fabric port of the original [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) mod.

## Ported systems

| System | Status | Notes |
|---|---|---|
| Player thirst data | Complete | Persistent and synchronized with the owning client |
| Thirst exhaustion | Complete | Mirrors vanilla food exhaustion with a Nether multiplier |
| Quenched hydration | Complete | Absorbs exhaustion before the main thirst level drops |
| Dehydration damage | Complete | Uses the vanilla starvation damage source |
| Thirst HUD | Complete | Uses the Fabric HUD API and the Minecraft 26.2 render pipeline |
| Sprint restriction | Complete | Prevents sprinting at critically low thirst |
| Drink consumption | Complete | Supports potions, drinks, watery foods, and the water bowl |
| Water source drinking | Complete | Shift-right-click water with an empty hand |
| Rain drinking | Complete | Look upward while standing in rain |
| Commands | Complete | Includes level controls and per-player enable state |
| Custom bowls | Complete | Clay bowl, terracotta bowl, and terracotta water bowl |
| Languages | Complete | All original translations plus Vietnamese |

## Changes made for Fabric

- Replaced Forge capabilities with Fabric Data Attachments.
- Replaced Forge event subscribers with Fabric lifecycle and interaction events.
- Replaced Forge GUI overlays with the Fabric HUD Element API.
- Updated item registration and consumable components for Minecraft 26.2.
- Updated item model and recipe resource paths for Minecraft 26.2.
- Split common and client code into separate source sets.
- Changed the Java package to `com.thirstwastaken`.
- Changed the mod ID and resource namespace to `thirstwastaken`.
- Added Vietnamese and completed command translations for every bundled language.
- Added GitHub Actions builds and Dependabot updates.
- Added dedicated server startup validation.

## Not yet ported

- Four-level water purity data and sickness effects
- Campfire and furnace water purification
- Biome temperature and humidity modifiers
- Fire protection and fire resistance dehydration modifiers
- Cold Sweat integration
- Farmer's Delight, Farmer's Respite, and Brewin' and Chewin' integration
- Create integration and the Sand Filter
- AppleSkin overlays and hydration tooltips
- Jade and Tough As Nails integration
- Structure chest loot and Piglin water trades
- Compatibility mixins for the other mods supported by the original Forge release

## Original water purity reference

The original Forge mod uses four water purity levels. This image is retained as a reference for the future purity port.

<p align="center">
  <img src=".github/assets/water-purity-original.png" alt="The four water purity levels from the original mod" width="275">
</p>

## Verification

- `./gradlew build` succeeds with Java 25.
- The dedicated Fabric server reaches the ready state on Minecraft 26.2.
- GitHub Actions builds and uploads the release JAR.
