# Fork status

ThirstWasTaken2 is a fork of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) by
[ghen](https://github.com/ghen-git), originally a Forge mod for Minecraft 1.19.2. It was rebuilt for
Fabric on Minecraft 26.2, then improved and changed. This page lists what carried over, what the fork
changes, and what is still missing.

## Carried over from the original

- Thirst and quenched bars that drain as you play, faster when you sprint, fight or take damage.
- Hotter, drier biomes and the Nether dry you out quicker; Fire Resistance and Fire Protection slow it down.
- Riding a mount does not make you thirsty.
- Running out of water hurts you, and stops you from sprinting and from healing naturally.
- On Peaceful, thirst slowly refills instead of draining.
- Drink from potions, modded drinks, watery foods and the water bowl.
- Drink straight from a water source by sneaking with an empty hand, or from the rain by looking up.
- Four levels of water purity. Dirty water can make you nauseous or poison you.
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

### 2. Tooltips show droplets, and no longer need AppleSkin

The original printed `Hydration: +6, Quenched: +8` as plain text, and only drew a graphical version
when AppleSkin was installed. Here the droplet row is part of the mod and always there.

![The tooltip of a water bottle, showing filled droplets and an outline droplet](docs/public/screenshots/item-tooltip.png)

The water inside each droplet is the hydration, the outline around it is the quenched, so one row
carries both values. A drink worth 6 hydration and 8 quenched reads as three filled droplets followed
by one outline-only droplet.

### 3. New droplet sprites

Empty droplets use the same dark shade as the empty hunger icons instead of the original's lighter
grey, so the bar reads the same way the food bar directly below it does. The sheet also went from
three frames to five, so the droplet you are currently drinking away steps through quarters as
exhaustion builds instead of jumping straight from full to half to empty.

| Original, three frames | ThirstWasTaken2, five frames |
|---|---|
| ![The original droplet sprites: empty, half and full](docs/public/screenshots/droplets-original.png) | ![The fork's droplet sprites: empty, quarter, half, three quarter and full](docs/public/screenshots/droplets-fork.png) |

### 4. The quenched outline is always on

Same story as the tooltips. The original only drew the lighter reserve outline over the droplets when
AppleSkin was present; here it is simply part of the bar.

### Smaller additions

- A config screen in Mod Menu for every setting, no file editing needed.
- Settings are stored in `config/thirstwastaken2.json` if you prefer to edit them by hand.
- The thirst bar can be moved anywhere on the screen.
- An optional setting requiring both hands to be empty before drinking by hand.
- A dedicated **ThirstWasTaken2** creative inventory tab collecting every item from the mod.

  ![The ThirstWasTaken2 creative inventory tab, collecting every item from the mod](docs/public/screenshots/creative-tab.png)

- The fork adds Vietnamese alongside the other bundled languages. Both the config screen and in-game
  text automatically follow each player's Minecraft client language, with no separate language setting.

## Not available yet

- **The Create Sand Filter.** The Create Fly integration is broken and is turned off for now, so the block cannot be crafted and bulk purification is unavailable. It will come back in a later release.
- **Jade** does not show water purity.

These need mods that have no Minecraft 26.2 Fabric release yet. Their items are already configured, so they will start working as soon as those mods update.

- Cold Sweat
- Farmer's Respite
- Brewin' and Chewin'
- Tough As Nails
- Supplementaries and Botania

## Good to know

- Everything except the HUD options is decided by the server. Changing them on a client that is joined to someone else's server will not affect that server.

---

Developers: see [CODEMAP.md](CODEMAP.md) and [CLAUDE.md](CLAUDE.md).
