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

## Checking water with Jade

![Jade showing Murky for the river water under the crosshair](/screenshots/jade-water.png)

With [Jade](https://modrinth.com/mod/jade) installed, looking at water shows its grade under the block
name, or Salty for sea water. This works on water in the world, waterlogged blocks and water
cauldrons, and the grade shown is the one a bottle filled there gets. It can be turned off in Jade's
plugin settings.

## Salt water

Oceans and beaches give salt water. It has its own icon and its own tooltip line, so it can be told
apart from fresh water at a glance, and it shows no thirst droplets because it restores nothing. On
Minecraft 1.21 and 1.21.1 only a bowl of it gets its own icon; see [Installation](/docs/installation).

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

![A dirty water bottle comes out of the furnace clean](/screenshots/furnace-clean-water.png)

| In | Out |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

A furnace takes ten seconds and a campfire takes thirty. Dirty water needs two passes to become pure.
With Farmer's Delight, the [Cooking Pot](/docs/features/farmers-delight#boiling-water-in-the-cooking-pot)
makes bottles and bowls pure in one pass. With Create Fly on Minecraft 26.1.2 and 26.2, the
[Sand Filter](/docs/features/create#sand-filter) cleans water pumped through it.

## Copper Hanging Pot

The Copper Hanging Pot boils a large batch of water at once. Craft it from two sticks, an iron chain
(a chain on Minecraft 1.21 and 1.21.1) and five copper ingots:

| | | |
|---|---|---|
| Stick | Chain | Stick |
| Copper Ingot | | Copper Ingot |
| Copper Ingot | Copper Ingot | Copper Ingot |

Place it on a campfire and it hangs from a wooden frame. It can also stand on any solid block, but it
only boils over a lit campfire or soul campfire.

- It holds nine servings. A bucket adds or takes three, a bottle or a bowl one.
- A waterskin takes one serving. Sneak and use a waterskin to pour all of it in.
- Over a lit campfire, everything in the pot becomes Pure after 30 seconds, whatever its grade and
  however full it is. The time is set by [hangingPotBoilSeconds](/docs/configuration#hangingpotboilseconds).
- Adding water starts the boil over. Putting the fire out pauses it.
- The water changes colour with its grade, so a finished pot is easy to spot.
- It mixes like a cauldron: it keeps the worse grade, and one salty drink makes the whole pot salty.
  Salt water does not boil clean.
- Rain fills it slowly with rainwater, like a cauldron.
- Breaking it drops the pot. The water inside is lost.
