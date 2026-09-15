# Fork status

ThirstWasTaken2 is a fork of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) by
[ghen](https://github.com/ghen-git), originally a Forge mod for Minecraft 1.19.2. It was rebuilt for
Fabric on Minecraft 26.2, 26.1.x, 1.21.11 and 1.21.1 then improved and changed. It is published on
[Modrinth](https://modrinth.com/mod/thirst-was-taken-2). This page lists what carried over, what the fork
changes, and what is still missing.

## Carried over from the original

- Thirst and quenched bars that drain as you play, faster when you sprint, fight or take damage.
- Hotter, drier biomes and the Nether dry you out quicker; Fire Resistance and Fire Protection slow it down.
- Riding a mount does not make you thirsty.
- Running out of water hurts you, and stops you from sprinting and from healing naturally.
- On Peaceful, thirst slowly refills instead of draining.
- Drink from potions, modded drinks, watery foods and the water bowl.
- Optionally drink straight from a water source by sneaking with an empty hand.
- Four grades of fresh water. Dirty water can make you nauseous or poison you, and sea water is a
  kind of its own that never quenches thirst.
- Purify water in a furnace or on a campfire, in bottles, bowls and buckets.
- Cauldrons remember how clean the water poured into them was.
- Clay bowl, terracotta bowl and terracotta water bowl. The clay bowl must be smelted before it can scoop water; hold the resulting terracotta bowl and use it on any water block, including flowing water.
- Water bottles appear in dungeon, mineshaft, shipwreck, nether bridge and bastion chests, and in Piglin barters.
- `/thirst` commands to check, set, or turn thirst off for a player.

## New in this fork

### 1. Hand drinking is worth a third of what it was

Drinking straight from a water source restores **1 thirst and 1 quenched**, down from the original's
3 and 2. Free, unlimited water refilling both bars that fast made every other drink pointless. Both
values are still configurable.

### 2. Tooltip droplets can be turned off

As in the original, tooltips only show what an item restores when AppleSkin is installed. Thirst
uses a top row of filled droplets, while quenched uses a lower row of outline droplets. A drink worth
6 thirst and 8 quenched reads as three filled droplets above four outline droplets. Here the rows can
be turned off to keep tooltips short, and the outline droplets take the colour of the quenched outline.

### 3. New droplet sprites

Empty droplets use the same dark shade as the empty hunger icons instead of the original's lighter
grey, so the bar reads the same way the food bar directly below it does. The sheet also went from
three frames to five, so the droplet you are currently drinking away steps through quarters as
exhaustion builds instead of jumping straight from full to half to empty.

| Original, three frames | ThirstWasTaken2, five frames |
|---|---|
| ![The original droplet sprites: empty, half and full](docs/public/screenshots/droplets-original.png) | ![The fork's droplet sprites: empty, quarter, half, three quarter and full](docs/public/screenshots/droplets-fork.png) |

### 4. The quenched outline has its own colours

As in the original, the quenched outline over the thirst bar and the dithered exhaustion strip behind
it only appear when AppleSkin is installed. Without AppleSkin, thirst and quenched work the same, and
the bar shows droplets only. The outline no longer follows AppleSkin's saturation option. It has its
own setting instead: Diamond, Ice, Gold, AppleSkin's own gold, or Off, which also hides the strip.

### Smaller additions

- Plain water follows vanilla food rules and cannot be consumed while the thirst bar is full. Drinks
  and foods with other uses are unaffected.
- Looking up in the rain no longer restores thirst, and the old setting for it has been removed.
- A config screen in Mod Menu for every setting, no file editing needed.
- Settings are stored in `config/thirstwastaken2.json` if you prefer to edit them by hand.
- The thirst bar can be moved anywhere on the screen.
- An optional setting requiring both hands to be empty before drinking by hand.
- Milk buckets and honey bottles restore thirst, like any other drink.
- Every recipe the mod adds appears in the recipe book, and the mod has an advancement tab of its
  own covering the water system.
- A cauldron filled by rain holds clean water, and one filled by a pointed dripstone holds pure
  water. Both grades are settings.
- With Jade installed, looking at water shows its grade before anything is collected.
- A dedicated **ThirstWasTaken2** creative inventory tab collecting every item from the mod.

  ![The ThirstWasTaken2 creative inventory tab, collecting every item from the mod](docs/public/screenshots/creative-tab.png)

- The fork adds Vietnamese alongside the other bundled languages. Both the config screen and in-game
  text automatically follow each player's Minecraft client language, with no separate language setting.

## Not available yet

- **Create on other Minecraft versions.** The Sand Filter is back with Create Fly, on Minecraft 26.1.2 and 26.2
  only. Builder's Tea does not restore thirst yet.

These need mods that have no Fabric release on Minecraft 26.x yet. Their items are already configured, so they will start working as soon as those mods update.

- Cold Sweat
- Farmer's Respite
- Brewin' and Chewin'
- Tough As Nails
- Supplementaries and Botania

## Good to know

- Everything except the HUD options is decided by the server. Changing them on a client that is joined to someone else's server will not affect that server.

---

Developers: see [AGENTS.md](AGENTS.md).
