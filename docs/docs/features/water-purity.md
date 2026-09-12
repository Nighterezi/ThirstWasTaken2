# Water quality

Water comes in two kinds. **Fresh water** has a grade, from dirty to pure. **Salt water** has no
grade at all: it cannot be cleaned, and it never quenches thirst. Every container remembers which of
the two it holds, and says so in its tooltip.

![Water bottle tooltips showing the grades of fresh water](/screenshots/water-purity.png)

## The four grades

From worst to best: **Dirty**, **Murky**, **Clean**, **Pure**. The grade is set once, when the water
is collected or drunk from the world, and it travels with the container after that.

| Where the water comes from | Usual grade |
|---|---|
| Swamp or mangrove swamp | Dirty |
| Jungle, savanna or badlands | Dirty |
| Most other biomes | Murky |
| River | Murky |
| Mountain | Clean |
| Cold peaks | Pure |

Very hot biomes make water worse and very cold biomes make it better. Water above y 100 or below
y 32 is a little cleaner, and so is flowing water, so a waterfall is not automatically safe. Mud,
mangrove roots, farmland or a composter within two blocks make water worse.

Modpacks can add biomes to `thirstwastaken2:stagnant_water` without changing code. Water that carries
no grade of its own, such as an unknown modded drink, uses
[defaultPurity](/docs/configuration#defaultpurity).

## Salt water

Oceans and beaches give salt water. It has its own icon and its own tooltip line, so it can be told
apart from fresh water at a glance, and it shows no hydration droplets because it restores nothing.

Drinking it costs thirst instead of restoring it and causes five seconds of Nausea. A furnace or a
campfire will not take it, so there is no way to make it drinkable. One salty drink poured into a
waterskin or a cauldron turns everything in there into salt water.

## Mixing and cauldrons

A waterskin averages the grades of the drinks inside it, by how many there are, and rounds down. Two
pure drinks and one dirty drink come out clean, so one good mouthful cannot rescue a bad batch.

A cauldron keeps the worse of what it holds and what is poured in. Water drawn back out into a
bottle, bucket or waterskin keeps that grade.

A cauldron also fills on its own, and each way of filling has a grade of its own.

| How the cauldron filled | Grade |
|---|---|
| Rain | Clean, set by [rainwaterPurity](/docs/configuration#rainwaterpurity) |
| A pointed dripstone dripping into it | Pure, set by [dripstonePurity](/docs/configuration#dripstonepurity) |

Rain is free and needs nothing built, so it is good but not the best water in the game. A dripstone
has to be placed under a water source with the cauldron below it, and it fills slowly, but the water
has been through the stone and comes out as clean as boiling would make it. Neither improves what is
already in the cauldron: rain falling into dirty water leaves it dirty.

## Drinking bad water

Fresh water always quenches thirst, whatever its grade. The risk is what changes.

| Grade | Nausea and Hunger | Poison |
|---|---|---|
| Dirty | 100% | 30% |
| Murky | 50% | 10% |
| Clean | 5% | none |
| Pure | none | none |

Nausea lasts five seconds, Hunger lasts thirty seconds and Poison lasts ten seconds. A longer-term
infection system is not part of this release.

## Cleaning fresh water

Put a fresh water bottle, terracotta water bowl or water bucket in a furnace or on a campfire.

| In | Out |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

A furnace takes ten seconds and a campfire takes thirty. Dirty water needs two passes to become pure.
