# Changelog

All notable changes to ThirstWasTaken2 are documented in this file.

## [1.0.1] - 2026-08-11

### Added

- A reusable three-drink waterskin with purity-aware mixing, cauldron filling and bottle or bucket
  transfers.
- Environment-sampled contamination, persistent salinity and four code-generated water-bowl
  sprites that visually match their purity.
- Optional AppleSkin integration that shows thirst exhaustion behind the thirst bar whenever
  AppleSkin's exhaustion-underlay option is enabled.

### Changed

- Water quality now starts from biome type and receives small temperature, altitude, flow, mud and
  agriculture modifiers instead of treating all high, deep or flowing water as automatically clean.
- Ocean water no longer hydrates, cannot be desalinated by cooking, and remains salty through
  waterskin, cauldron and filter transfers.
- The creative-tab water bowl and tab icon now use the Purified sprite, and purity tooltip colors
  match the original mod's palette exactly.
- Filled waterskins now expand with a cleaner silhouette and consistent leather shading across all
  three serving levels; the empty sprite is unchanged.
- Item hydration tooltips now use two distinct sprite rows: filled droplets for thirst on top and
  outline droplets for quenched underneath.
- Salty-water tooltips now use plain wording without an em dash.

## [1.0.0] - 2026-08-11

Initial release of ThirstWasTaken2, a Fabric fork of
[Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) for Minecraft 26.2.

### Added

- Rebuilt the original Forge mod for Fabric on Minecraft 26.2.
- Thirst, quenched hydration and exhaustion systems, including faster dehydration while sprinting,
  fighting, taking damage, or travelling through hot and dry environments.
- Dehydration penalties: damage, disabled sprinting and disabled natural health regeneration when
  out of water.
- Peaceful-mode thirst regeneration and protection from dehydration while riding a mount.
- Drinking from potions, supported modded drinks, watery foods, water bowls, water sources and rain.
- Four water-purity levels, with nausea and poison risks from unsafe water.
- Water purification in furnaces and on campfires for bottles, bowls and buckets.
- Purity-aware cauldrons that retain the quality of water poured into them.
- Clay bowls, terracotta bowls and terracotta water bowls, including support for collecting flowing
  water with a terracotta bowl.
- Water bottles in dungeon, mineshaft, shipwreck, Nether fortress and bastion loot, as well as
  Piglin bartering.
- `/thirst` commands for querying and setting hydration or enabling and disabling thirst per player.
- Built-in droplet tooltips for food and drink hydration values; AppleSkin is no longer required.
- A five-stage droplet sprite set for smoother exhaustion feedback.
- A permanently visible quenched reserve outline on the thirst bar.
- A Mod Menu configuration screen covering every setting.
- JSON configuration at `config/thirstwastaken2.json` for manual editing.
- Configurable thirst-bar positioning anywhere on the screen.
- An option to require both hands to be empty before drinking directly from water.
- A dedicated ThirstWasTaken2 creative inventory tab.
- Vietnamese localization alongside English, French, Japanese, Korean, Polish, Russian, Simplified
  Chinese and Traditional Chinese.

### Changed from the original

- Drinking directly from a water source now restores **1 thirst** and **1 quenched**, reduced from
  3 thirst and 2 quenched. Both values remain configurable.
- Empty thirst droplets now use the same dark shade as empty hunger icons.
- Server gameplay settings are authoritative; only HUD settings are controlled by each client.

### Known issues and unavailable integrations

- The Create Sand Filter is disabled because Create Fly integration is not yet available for this
  Minecraft/Fabric version.
- Jade does not currently display water purity.
- Cold Sweat, Farmer's Respite, Brewin' and Chewin', Tough As Nails, Supplementaries and Botania
  integrations are awaiting compatible Minecraft 26.2 Fabric releases.
