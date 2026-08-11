<!--
Description for the ThirstWasTaken2 (Fabric) project page on Modrinth.
Paste everything below the marker into the Modrinth description editor.
Image links are absolute so they resolve outside this repository.

Modrinth "Summary" field, kept in sync with fabric.mod.json and the docs site:

  Adds a survival thirst bar, drinking, and water purity.

Re-shoot before publishing, these still show the old name:
  config-1.png, creative-tab.png
-->

<p align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">
</p>

<p align="center"><strong>Water stops being scenery and starts being supplies.</strong></p>

> A fork of **[Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken)** by **ghen**, rebuilt for
> Fabric and extended by **[Nighter](https://github.com/Nighterezi)**. The banner, icon and
> translations come from the original project.

ThirstWasTaken2 adds a second survival bar. It sits above hunger, drains while you play, and refills
from potions, watery foods, the rain, or a bowl you scooped yourself. Where that water came from
matters, because dirty water can make you sick.

## A bar that reads like hunger

Ten droplets, a lighter reserve outline behind them, and the same shake when that reserve runs out.
It drains for the same reasons hunger does, and faster in hot or dry biomes. The Nether runs at
triple speed, which Fire Resistance and Fire Protection both cut back.

![The thirst bar above the hunger bar, part drained, in a birch forest](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png)

An empty bar takes half a heart every two seconds, ignores armour, and on Normal or Hard it will kill
you. Before that it stops you sprinting and blocks natural healing.

![An empty thirst bar with health down to two and a half hearts](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/dehydration.png)

## Water remembers where it came from

Every bottle, bucket and bowl is stamped the moment you fill it.

![Tooltips reading Dirty, Slightly Dirty, Acceptable and Purified](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png)

| Where you fill it | Grade |
|---|---|
| A still pond or ocean at ordinary heights | Dirty |
| A stream at ordinary heights | Slightly dirty |
| Still water above y 100, or below y 48 | Slightly dirty |
| A stream above y 100, or below y 48 | Acceptable |

The pond outside your door is the worst water in the game. A mountain stream is drinkable as it is.
Drink something dirty and you risk Nausea with Hunger behind it, or Poison. Bad water still fills the
bar, so it stays a trade rather than a wasted item.

## Boiling it clean

Put a water bottle, a terracotta water bowl or a water bucket in a furnace or on a campfire.

| In | Out |
|---|---|
| Dirty | Acceptable |
| Slightly dirty | Purified |
| Acceptable | Purified |

A furnace takes ten seconds, a campfire thirty. Dirty water needs two passes to reach purified.
Cauldrons remember the worse of whatever you pour in, so you cannot launder bad water by mixing it.

## Plenty of ways to drink

The mod adds a clay bowl, smelted into a terracotta bowl, which scoops water straight out of the
world, flowing water included. Drinking one leaves the empty bowl in your hand.

![The creative tab, holding the clay bowl, terracotta bowl and terracotta water bowl](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/creative-tab.png)

With nothing in your hands, look straight up in the rain. Or turn on hand drinking and sneak onto any
water source, which drinks it exactly as it is, swamp puddle risks included.

![Rain falling over a forest, with the thirst bar part drained](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/drinking-in-rain.png)

Water bottles also turn up in mineshaft, dungeon, shipwreck, nether bridge and bastion chests, and
from Piglin bartering. That is often what keeps a player alive in the Nether.

## Tooltips you can read at a glance

The water inside each droplet is the hydration, the outline around it is the quenched reserve, so one
row carries both values.

![The tooltip of a water bottle, showing three filled droplets and one outline](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/item-tooltip.png)

The original mod only drew this, and the quenched outline on the bar, when AppleSkin was installed.
Here both are part of the mod, with or without AppleSkin.

## Every number is a slider

Over thirty settings, editable in game from **Mods > ThirstWasTaken2 > Config** with Mod Menu
installed, or straight from `config/thirstwastaken2.json`.

![The config screen, showing the depletion and drinking sections](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-1.png)

Only the bar position is read from your own copy. Everything else comes from the machine running the
world, so on a dedicated server that is the server's file.

## Nine languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese, covering the config screen as well as the in-game text. Each player sees the language their
game is already set to.

## Commands

| Command | Action |
|---|---|
| `/thirst query <player>` | Show current thirst and quenched |
| `/thirst set <players> <thirst> <quenched>` | Set both values |
| `/thirst enable <players> <true/false>` | Turn thirst ticking on or off |

These need game master permission.

## Requirements

| | |
|---|---|
| Minecraft | 26.2 |
| Loader | Fabric Loader 0.19.3 or newer |
| Java | 25 |
| Required | Fabric API |
| Optional | Mod Menu, for the settings screen |

Install it on the server and on every client that joins. A player without the mod will not see the
bar at all. Thirst is stored on the player, so existing worlds work.

## Links

- [Documentation](https://nighterezi.github.io/ThirstWasTaken2/)
- [What this fork changes](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
- [Source code](https://github.com/Nighterezi/ThirstWasTaken2)
- [Issue tracker](https://github.com/Nighterezi/ThirstWasTaken2/issues)
- [Original mod by ghen](https://modrinth.com/mod/thirst-was-taken)
