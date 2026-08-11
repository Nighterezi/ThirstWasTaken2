<!--
Description for the ThirstWasTaken2 (Fabric) project page on Modrinth.
Paste the content below the marker into the Modrinth description editor.
Image links are absolute so they resolve outside this repository.
-->

# ThirstWasTaken2

**Water stops being scenery and starts being supplies.**

> A fork of **[Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken)** by **ghen**, rebuilt for
> Fabric and extended by **[Nighter](https://github.com/Nighterezi)**. The banner, icon and
> translations come from the original project.

ThirstWasTaken2 adds a second survival bar. It sits above hunger, drains while you play, and refills
from potions, watery foods, the rain, or a bowl you scooped yourself. Where that water came from
matters, because dirty water can make you sick.

![The thirst bar above the hunger bar, part drained, in a birch forest](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png)

## Why you might want it

- **Reads like the hunger bar.** Ten droplets, a reserve behind them, and the same shake when the
  reserve runs out.
- **The world decides how thirsty you get.** Hot and dry biomes drain you faster, cold and rainy
  ones slower, and the Nether is brutal.
- **Four grades of water,** decided by where you filled the bottle. Drinking badly can cost you.
- **Every number is a slider** in Mod Menu, and the file is right there if you prefer typing.
- **Nine languages**, picked from each player's own setting.
- **Nothing else is required.** Values for a dozen food mods ship unused until those mods exist.

## Tooltips you can read at a glance

Anything that restores thirst shows it as droplets instead of numbers. The water inside each droplet
is the hydration, the outline around it is the quenched reserve, so one row carries both values and
is only as long as the larger of the two.

| Item tooltip | The four grades of water |
|---|---|
| ![The tooltip of a water bottle, showing three filled droplets and one outline](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/item-tooltip.png) | ![Tooltips reading Dirty, Slightly Dirty, Acceptable and Purified](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png) |

## Water remembers where it came from

A bottle, bucket or bowl is stamped the moment you fill it. The pond outside your door is the worst
water in the game. A mountain stream is drinkable as it is.

| Where you fill it | Grade |
|---|---|
| A still pond or ocean at ordinary heights | Dirty |
| A stream at ordinary heights | Slightly dirty |
| Still water above y 100, or below y 48 | Slightly dirty |
| A stream above y 100, or below y 48 | Acceptable |

Dirty water still hydrates you. It might also hand you Nausea and Hunger, or ten seconds of Poison,
so it is a trade you make on purpose rather than a waste. Cook a container in a furnace or on a
campfire to clean it one or two grades, and cauldrons remember the worse of whatever you pour in.

## Ways to drink

Potions and water bottles are the obvious ones, but you are not stuck without glass.

The mod adds a **clay bowl**, smelted into a **terracotta bowl**, which scoops water straight from
the world, flowing water included. Drinking one leaves the empty bowl in your hand.

![The ThirstWasTaken2 creative tab, holding the clay bowl, terracotta bowl and terracotta water bowl](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/creative-tab.png)

With nothing in your hands at all, look straight up in the rain, or sneak and use an empty hand on
water. Hand drinking is off by default and drinks the water exactly as it is, swamp puddle risks
included.

| Drinking the rain | Running on empty |
|---|---|
| ![Rain falling over a forest, with the thirst bar part drained](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/drinking-in-rain.png) | ![An empty thirst bar with health down to two and a half hearts](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/dehydration.png) |

An empty bar costs half a heart every two seconds, ignores armour, and on Normal or Hard it will
kill you. Before that it stops you sprinting at 6 points and blocks natural healing until you are
nearly full.

Water bottles also turn up on their own, in mineshaft, dungeon, shipwreck, nether bridge and bastion
chests, and from Piglin bartering. That is often what keeps a player alive in the Nether.

## Configuration

`config/thirstwastaken2.json` is written on first launch. With Mod Menu installed, the same options
are editable in game from **Mods > ThirstWasTaken2 > Config**, and the screen saves when you close
it.

![The config screen, showing the depletion and drinking sections](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-1.png)

You can tune depletion speed inside and outside the Nether, the Fire Resistance discount, Peaceful
behaviour, sprint and healing penalties, rain and hand drinking, overflow into the reserve, default
purity, the mountain and cave heights, the flowing water bonus, every sickness chance, and the HUD:
the exhaustion strip, the quenched outline, quarter step droplet draining, and where the bar sits.

Only the HUD section is read from your own copy. Everything else comes from the machine running the
world, so on a dedicated server that is the server's file.

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
| Optional | Mod Menu for the settings screen |

Install it on the server and in single player.

**Create Fly is not supported yet.** The Sand Filter, the block that purifies water in bulk, is
broken and turned off. It will come back in a later release, and nothing else in the mod depends
on it.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese, all covering the config screen as well as the in game text.

## Links

- [Documentation](https://nighterezi.github.io/ThirstWasTaken2/)
- [Source code](https://github.com/Nighterezi/ThirstWasTaken2)
- [Issue tracker](https://github.com/Nighterezi/ThirstWasTaken2/issues)
- [Original mod by ghen](https://modrinth.com/mod/thirst-was-taken)
