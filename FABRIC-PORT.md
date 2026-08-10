# Fabric port status

What made it into the Minecraft 26.2 Fabric version of [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod), and what has not yet.

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
- A dedicated **Thirst Was Taken** creative inventory tab collects every item from the mod. The Sand Filter joins it when Create Fly is installed.
- Water bottles appear in dungeon, mineshaft, shipwreck, nether bridge and bastion chests, and in Piglin barters.
- `/thirst` commands to check, set, or turn thirst off for a player.
- Full translations, including Vietnamese.

## New in the Fabric version

- A config screen in Mod Menu for every setting, no file editing needed.
- Settings are stored in `config/thirstwastaken.json` if you prefer to edit them by hand.
- The thirst bar can be moved around the screen.
- Redrawn droplets. Empty droplets now use the same dark shade as the empty hunger icons instead of a lighter grey, so the bar reads the same way the food bar does.
- Droplets empty in quarters. The original only had full, half and empty; the sheet now has five levels, and the droplet you are currently drinking away steps through all of them. Turn it off under `Smooth Droplet Drain` for the old three-step look.
- The dotted exhaustion strip behind the bar and the quenched outline on the droplets can each be turned off. The dotted strip is off by default.
- Optional setting requiring both hands to be empty before drinking by hand.
- Hand drinking restores 1 thirst and 1 quenched by default, reduced from 3 and 2 so free water does not fill both bars too quickly. Both values remain configurable.
- Support for other food mods works by item name, so none of them are required to be installed.
- The Create Sand Filter is back, for Create Fly.

## Not available yet

These need mods that have no Minecraft 26.2 Fabric release yet. Their items are already configured, so they will start working as soon as those mods update.

- Cold Sweat
- Farmer's Respite
- Brewin' and Chewin'
- Tough As Nails
- Supplementaries and Botania

Missing from the original mod:

- The Sand Filter has no Ponder scene.
- Jade does not show water purity.

## Good to know

- Everything except the HUD options is decided by the server. Changing them on a client that is joined to someone else's server will not affect that server.
- The Sand Filter only exists while Create Fly is installed. Removing Create from a world that already has one placed will leave a missing block behind.

---

Developers: see [CODEMAP.md](CODEMAP.md) and [CLAUDE.md](CLAUDE.md).
