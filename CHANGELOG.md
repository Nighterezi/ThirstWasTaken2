# Changelog

All notable changes to ThirstWasTaken2 are documented in this file.

## [Unreleased]

### Added

- Copper Hanging Pot. It holds a bucket of water, three servings, and boils it Pure when placed on a
  lit campfire, 4 seconds for each serving. Water cannot be poured into it in the Nether. Crafted from two sticks, a chain and five copper ingots.
- Iron Hanging Pot. It works like the Copper Hanging Pot, in dark cast iron, but boils slower: 6
  seconds for each serving. Crafted from two sticks, a chain and five iron ingots.
- With Jade installed, looking at a hanging pot shows the grade of the water inside.

### Changed

- The license changed from MIT to the GNU General Public License v3.0.

### Notes

- The Copper and Iron Hanging Pots are adapted from the campfire cauldron in Dehydration by Globox1997.
- Existing worlds and config files need no changes.

<details>
<summary>Configuration file details</summary>

- New keys `copperPotSecondsPerServing`, default `4`, and `ironPotSecondsPerServing`, default `6`:
  how long each serving in that hanging pot takes to boil.

</details>

## [1.0.6] - 2026-09-16

### Added

- A NeoForge version for every supported Minecraft version: 26.2, 26.1, 26.1.1, 26.1.2, 1.21.11 and
  1.21.1. It has the same features as the Fabric version, with the config screen opened from the
  NeoForge mods list. The NeoForge file has `-neoforge` after the Minecraft version in its name.
- AppleSkin and Jade work on NeoForge too.

### Fixed

- The thirst bar vanished on the death screen. It now stays on screen with the hunger bar, showing
  the thirst the player died with.

### Notes

- Create Fly and Farmer's Delight have no NeoForge release, so the Sand Filter and the Farmer's
  Delight support are Fabric only for now.
- On NeoForge, Minecraft 1.21 is not covered. The 1.21.1 file is for 1.21.1 only, where the Fabric
  one covers both.
- A world made with the Fabric version and then opened with the NeoForge version, or the other way
  round, starts every player at full thirst. Everything else in the world is unchanged.
- Nothing changes for existing Fabric worlds or config files.

## [1.0.5] - 2026-09-14

### Added

- With Jade installed, looking at water, a waterlogged block or a water cauldron shows its grade, or
  Salty for sea water. It can be turned off in Jade's plugin settings.
- Drinks from other mods now restore thirst without a config entry, as long as their mod marks them
  as drinks.
- Farmer's Delight support. Its milk bottle, hot cocoa, bone broth, onion soup, glow berry custard and
  tomato now restore thirst, alongside the drinks and meals already covered.
- With Farmer's Delight, the Cooking Pot boils a fresh water bottle or water bowl pure in one pass.
- With Farmer's Delight, Nourishment stops the thirst bar from draining.
- The Sand Filter is back, now with Create Fly on Minecraft 26.1.2 and 26.2. Water pumped through it comes out one
  grade cleaner.
- With Create Fly, water keeps its grade through pumps, pipes, Spouts, Basins and Item Drains.

### Changed

- Built-in thirst values and water grades for Farmer's Respite, Brewin' and Chewin', Collector's Reap
  and Tough As Nails were removed. Farmer's Delight is the only food mod with built-in values for now.

### Notes

- Jade is optional and only needed on the client.
- Create Fly is optional and must be installed on both the client and the server. The other Minecraft
  versions ignore it for now.
- Existing config files get the new Farmer's Delight values automatically. Values for the removed
  mods stay in existing config files until deleted by hand.

<details>
<summary>Configuration file details</summary>

- New `enableDrinkTagMatching`: `true` by default.
- New `drinkTagValue`: `[6, 8]` by default.

</details>

## [1.0.4] - 2026-09-13

### Added

- Support for Minecraft 1.21 and 1.21.1.
- The quenched outline on the thirst bar now comes in four colours: Diamond, Ice, Gold and
  AppleSkin's own gold. Turning it off also hides the exhaustion strip.
- A setting to hide the thirst and quenched droplets in item tooltips.
- The config screen is split into pages, shows a live preview of the thirst bar and a drink's
  tooltip, and has Cancel and Reset to Defaults buttons.

### Changed

- Chests and Piglin bartering that a data pack replaced now get the mod's water bottles as well.
- The quenched outline and the tooltip droplets now appear only with AppleSkin installed, the same
  way AppleSkin shows saturation and food values. The outline is cyan by default.

### Fixed

- Mineshaft chests held no water bottles in worlds created with the Villager Trade Rebalance
  experiment.
- The quenched outline had stray pixels outside the droplet at a quarter, half and three quarters
  full.
- Low thirst did not stop sprinting. Players can no longer sprint at 6 thirst or below, as intended.
- Dehydration damage knocked players back. It now hurts without knockback, like drowning.
- Drinking by hand did not work on deep water or a waterfall when no block was within reach.

### Notes

- On Minecraft 1.21 and 1.21.1, sea water in bottles and buckets looks like ordinary water, and
  the droplets in tooltips have a shadow. The Salty tooltip line and the sea-coloured bowl are
  unchanged.
- No action is needed for existing worlds or config files.

<details>
<summary>Configuration file details</summary>

- New `appleskinQuenchedOverlay`: `DIAMOND` (default), `ICE`, `GOLD`, `APPLESKIN` or `OFF`.
- New `appleskinTooltipDroplets`: `true` by default.

</details>

## [1.0.3] - 2026-09-12

### Added

- Milk buckets and honey bottles restore thirst.
- An advancement tab for the mod, from the first drink of water to drinking in the Nether.
- Rain and pointed dripstones fill cauldrons with a quality of their own. Rain is clean and
  dripstone water is pure, where both used to fall back to the default quality.

### Changed

- Sea water is no longer a grade of fresh water. Bottles, buckets and bowls of it look different
  from fresh ones, its tooltip reads Salty where fresh water shows a grade, and it shows no
  thirst droplets, because it restores nothing.
- The grades of fresh water are now Dirty, Murky, Clean and Pure, in all nine languages, with
  tooltip colours that are easier to tell apart.
- A waterskin now averages the grades of the drinks inside it, rounded down. One salty drink still
  turns all of it into sea water, and cauldrons still keep the worse of the two.
- The clay bowl now says it has to be smelted before it can hold water.
- Thirst uses less server time and memory and sends far fewer updates, which matters most on busy
  servers.
- The partly drained droplet and AppleSkin's exhaustion strip behind the thirst bar now move in
  small steps instead of every tick.
- Renamed the "hydration" wording to "thirst" everywhere it named the bar's value, so the mod uses
  one word for it. Two config keys changed with it: `handDrinkingHydration` is now
  `handDrinkingThirst` and `extraHydrationConvertsToQuenched` is now `extraThirstConvertsToQuenched`.
  Custom values for those two settings reset to their defaults on first launch; every other setting
  is untouched. The `ThirstApi.hydration(...)` methods were also renamed to `ThirstApi.thirstValues(...)`.

### Fixed

- Recipes added by the mod never appeared in the recipe book, so the clay bowl, the bowls, the
  waterskin and the purification recipes had to be looked up outside the game.
- Water from structure chests and Piglin bartering could not be boiled.
- Sea water could report a grade, so water from a frozen ocean read as the cleanest water in the
  game while still being undrinkable.

### Notes

- Servers and players must run the same version. A mismatch shows the wrong blocks in the world.
- Existing worlds and config files need no action. Sea water left in a cauldron in an older world
  becomes fresh water of that cauldron's grade; sea water in containers is unaffected. Two settings,
  `rainwaterPurity` and `dripstonePurity`, are added to the config file on first launch.

## [1.0.2] - 2026-09-10

### Added

- Support for Minecraft 26.1 and 1.21.11, alongside 26.2.

### Changed

- Redesigned water droplet sprites on the thirst HUD and item tooltips.
- Redesigned waterskin sprites across all filling stages.
- A filled terracotta water bowl no longer stacks, matching water bottles and buckets. One bowl is
  one drink, so the waterskin and the three it holds are worth carrying again.

### Removed

- All Create support. The Sand Filter and its recipe are gone. The Sand Filter never worked, so nothing playable is lost.

### Notes

- Create support is planned to return in a future release.
- No action is needed for existing worlds or config files.
- Each Minecraft version has its own download, named after it, for example
  `ThirstWasTaken2-1.0.2+1.21.11.jar`.

## [1.0.1] - 2026-08-11

### Added

- A waterskin that holds three drinks, mixes water of different purity, fills from cauldrons and
  trades water with bottles and buckets.
- Filled waterskins can now be emptied by sneaking and using them on a block.
- Water is now graded by its surroundings when it is collected, and salt water stays salty wherever
  it is moved.
- Water bowls now look different at each purity level.
- Optional AppleSkin integration that shows thirst exhaustion behind the thirst bar whenever
  AppleSkin's exhaustion-underlay option is enabled.

### Changed

- Drinking directly from water with an empty hand is now enabled by default.
- Water quality now starts from the biome and is adjusted by temperature, altitude, whether the water
  flows, and nearby mud, farmland or composters, instead of treating all high, deep or flowing water
  as clean.
- Ocean water no longer hydrates and cannot be made drinkable by cooking. It stays salty through
  waterskins, cauldrons and the sand filter.
- The creative tab icon and its water bowl now show purified water, and purity tooltip colors match
  the original mod.
- Filled waterskins are easier to read at a glance, so the three serving levels can be told apart.
  The empty waterskin is unchanged.
- Item tooltips now show thirst as filled droplets, with quenched as outlined droplets on a
  second row.
- Plain water now follows vanilla food behaviour and cannot be consumed while the thirst bar is
  full. Potions and foods with other uses remain available.

### Removed

- Outdoor rain drinking and its `canDrinkRain` configuration option.

## [1.0.0] - 2026-08-11

Initial release of ThirstWasTaken2, a Fabric fork of
[Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) for Minecraft 26.2.

### Added

- Rebuilt the original Forge mod for Fabric on Minecraft 26.2.
- Thirst, quenched and exhaustion systems, including faster dehydration while sprinting,
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
- `/thirst` commands for querying and setting thirst or enabling and disabling thirst per player.
- Built-in droplet tooltips for food and drink thirst values; AppleSkin is no longer required.
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
