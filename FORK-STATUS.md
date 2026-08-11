# Fork status

ThirstWasTaken2 is a fork of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) by
[ghen](https://github.com/ghen-git), originally a Forge mod for Minecraft 1.19.2. It was rebuilt for
Fabric on Minecraft 26.2 and has kept growing since. This page lists what carried over, what the fork
adds on top, and what is still missing.

## What works

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
- A dedicated **ThirstWasTaken2** creative inventory tab collects every item from the mod.
- Water bottles appear in dungeon, mineshaft, shipwreck, nether bridge and bastion chests, and in Piglin barters.
- `/thirst` commands to check, set, or turn thirst off for a player.
- Nine languages, all covering the config screen as well as the in-game text.

## New in this fork

- A config screen in Mod Menu for every setting, no file editing needed.
- Settings are stored in `config/thirstwastaken2.json` if you prefer to edit them by hand.
- The thirst bar can be moved around the screen.
- Redrawn droplets. Empty droplets now use the same dark shade as the empty hunger icons instead of a lighter grey, so the bar reads the same way the food bar does.
- Droplets empty in quarters. The original only had full, half and empty; the sheet now has five levels, and the droplet you are currently drinking away steps through all of them. Turn it off under `Smooth Droplet Drain` for the old three-step look.
- The dotted exhaustion strip behind the bar and the quenched outline on the droplets can each be turned off. The dotted strip is off by default.
- Optional setting requiring both hands to be empty before drinking by hand.
- Hand drinking restores 1 thirst and 1 quenched by default, reduced from 3 and 2 so free water does not fill both bars too quickly. Both values remain configurable.
- Item tooltips show droplets instead of a line of numbers. The original printed `Hydration: +6, Quenched: +8`; the port draws the same droplets the bar uses, with the water level inside each droplet and the quenched outline around it, so one row of droplets carries both values. A drink worth 6 hydration and 8 quenched reads as three filled droplets followed by one outline-only droplet.
- Support for other food mods works by item name, so none of them are required to be installed.

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
