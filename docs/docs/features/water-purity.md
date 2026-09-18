# Water quality

Fresh water has a grade, from Dirty to Pure. Sea water is Salty, has no grade, and never quenches
thirst. Every container shows which one it holds in its tooltip.

![A water bottle tooltip stepping through Dirty, Murky, Clean, Pure and Salty](/screenshots/water-tooltips.gif)

## The four grades

From worst to best: **Dirty**, **Murky**, **Clean**, **Pure**. Water gets its grade where it is
collected and keeps it.

| Where the water comes from | Usual grade |
|---|---|
| Swamp or mangrove swamp | Dirty |
| Jungle, savanna or badlands | Dirty |
| Most other biomes | Murky |
| River | Murky |
| Mountain | Clean |
| Cold peaks | Pure |

- Hot biomes make water worse, cold biomes make it better.
- Water above y 100 or below y 32 is a little cleaner. So is flowing water.
- Mud, mangrove roots, farmland or a composter within two blocks make water worse.

Modpacks can add biomes to the `thirstwastaken2:stagnant_water` tag. Water with no grade of its own
uses [defaultPurity](/docs/configuration#defaultpurity).

## Checking water with Jade

![Jade showing Murky for the river water under the crosshair](/screenshots/jade-water.png)

With [Jade](https://modrinth.com/mod/jade) installed, looking at water, a waterlogged block, a water
cauldron or a hanging pot shows its grade, or Salty. It can be turned off in Jade's plugin settings.

## Salt water

Oceans and beaches give salt water. It has its own icon and tooltip line. On Minecraft 1.21 and
1.21.1 only the bowl has its own icon.

- Drinking it costs thirst and causes five seconds of Nausea.
- It cannot be boiled clean.
- One salty drink makes a whole waterskin, cauldron or hanging pot salty.

## Mixing and cauldrons

- A waterskin takes the average grade of its drinks, rounded down.
- A cauldron keeps the worse grade of what it holds and what is poured in.

A cauldron also fills on its own:

| How it filled | Grade |
|---|---|
| Rain | Clean, set by [rainwaterPurity](/docs/configuration#rainwaterpurity) |
| A pointed dripstone dripping into it | Pure, set by [dripstonePurity](/docs/configuration#dripstonepurity) |

Neither improves water already in the cauldron.

## Drinking bad water

Fresh water always quenches thirst. The grade sets the risk.

| Grade | Nausea and Hunger | Poison |
|---|---|---|
| Dirty | 100% | 30% |
| Murky | 50% | 10% |
| Clean | 5% | none |
| Pure | none | none |

Nausea lasts 5 seconds, Hunger 30 seconds and Poison 10 seconds.

## Cleaning fresh water

Put a water bottle, terracotta water bowl or water bucket in a furnace or on a campfire.

![A dirty water bottle comes out of the furnace clean](/screenshots/furnace-clean-water.png)

| In | Out |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

A furnace takes ten seconds, a campfire thirty. Other ways to clean water:

- A [Hanging Pot](#copper-hanging-pot) boils a whole bucket Pure.
- The Farmer's Delight [Cooking Pot](/docs/features/farmers-delight#boiling-water-in-the-cooking-pot)
  makes bottles and bowls Pure in one pass.
- The Create [Sand Filter](/docs/features/create#sand-filter) cleans water pumped through it.

## Copper Hanging Pot

![A Copper Hanging Pot of water boiling over a campfire](/screenshots/copper-hanging-pot.png)

Placed on a lit campfire or soul campfire, it boils water into Pure water. It can also stand on any
solid block, but only boils over a fire.

![Two sticks and a chain across the top, five copper ingots in a U below, make a Copper Hanging Pot](/screenshots/copper-hanging-pot-recipe.png)

On Minecraft 1.21 and 1.21.1 the recipe uses a chain instead of an iron chain.

- Holds three servings, like a cauldron. A bucket fills or empties it. A bottle or bowl adds or takes
  one.
- A waterskin adds one serving. Sneak to pour all of it in.
- Each serving takes 4 seconds, set by
  [copperPotSecondsPerServing](/docs/configuration#copperpotsecondsperserving).
- Adding water only adds that water's time. Putting the fire out pauses the boil.
- The water changes colour with its grade.
- It mixes like a cauldron, and salt water never boils clean.
- Rain fills it slowly.
- Water cannot be poured into it in the Nether.
- Breaking it drops the pot. The water is lost.

### Iron Hanging Pot

![An Iron Hanging Pot of water boiling over a campfire](/screenshots/iron-hanging-pot.png)

Works like the Copper Hanging Pot but boils slower: 6 seconds a serving, set by
[ironPotSecondsPerServing](/docs/configuration#ironpotsecondsperserving).

![Two sticks and a chain across the top, five iron ingots in a U below, make an Iron Hanging Pot](/screenshots/iron-hanging-pot-recipe.png)
