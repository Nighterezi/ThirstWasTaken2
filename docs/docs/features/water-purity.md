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

- Drinking it costs thirst, causes eight seconds of Nausea and 30 seconds of Parched II.
- It cannot be boiled clean.
- One salty drink makes a whole waterskin, cauldron or hanging pot salty.

Parched makes thirst drain faster, the way Hunger does for food, and turns the thirst bar the colour
of dry sand.

![The thirst bar in dry sand colours while Parched](/screenshots/parched-hud.png)

## Mixing and cauldrons

- A waterskin takes the average grade of its drinks, rounded down.
- A cauldron keeps the worse grade of what it holds and what is poured in.

A cauldron also fills on its own:

| How it filled | Grade |
|---|---|
| Rain | Clean |
| A pointed dripstone dripping into it | Pure |

Neither improves water already in the cauldron.

## Drinking bad water

Fresh water always quenches thirst. The grade sets the risk, and harder difficulties make it worse.
Pure water is always safe.

Bad water fills the thirst bar but does not last. Dirty water gives no quenched and Murky water half,
so thirst starts dropping again soon after, the way rotten flesh gives almost no saturation.

Dirty and Murky water taste bad: every drink gives seven seconds of Nausea, even on Peaceful. Then each
drink can make the player ill, with at most one illness at a time.

| Chance per drink | Dirty | Murky | Clean |
|---|---|---|---|
| Peaceful | none | none | none |
| Easy | 15% Poisoning, 50% Upset Stomach I | 5% Poisoning, 30% Upset Stomach I | 5% Upset Stomach I |
| Normal | 25% Poisoning, 50% Upset Stomach II | 10% Poisoning, 40% Upset Stomach I | 2% Poisoning, 10% Upset Stomach I |
| Hard | 33% Poisoning, 45% Upset Stomach II | 20% Poisoning, 46% Upset Stomach II | 5% Poisoning, 15% Upset Stomach I |

Drinking again while ill rolls again. The same illness lasts longer, up to twice its time, and Upset
Stomach I becomes II. A worse one adds its effects. A milder one does nothing.

The chances can be changed per difficulty, or the old Nausea and Poison brought back, with
[sicknessPreset](/docs/configuration#sicknesspreset).

### Upset Stomach

The common one. It never hurts on its own.

- Thirst drains faster, twice as fast at level II.
- The screen warps now and then, about once a minute at level I and twice at level II.
- Food fills less saturation, and drinks less quenched: three quarters at level I, half at level II.
- The thirst bar turns green while it lasts.

It lasts 45 seconds on Easy, 60 on Normal and 90 on Hard.

![The thirst bar in green while the player has Upset Stomach](/screenshots/upset-stomach-hud.png)

### Poisoning

A bad batch. It comes with Upset Stomach, and milk cures it.

| | Easy | Normal | Hard |
|---|---|---|---|
| Poison | 10 seconds | 20 seconds | 30 seconds |

Poison stops at half a heart, so Poisoning never kills.

## Cleaning fresh water

Put a water bottle, terracotta water bowl or water bucket in a furnace, a smoker or on a campfire.

![A dirty water bottle comes out of the furnace clean](/screenshots/furnace-clean-water.png)

| In | Out |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

A furnace takes ten seconds, a smoker five, a campfire thirty. Other ways to clean water:

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
- A waterskin fills up from it in one go, as far as the pot has water. Sneak to pour all of it in.
- Each serving takes 4 seconds.
- Adding water only adds that water's time. Putting the fire out pauses the boil.
- The water changes colour with its grade.
- It mixes like a cauldron, and salt water never boils clean.
- Rain fills it slowly.
- Water cannot be poured into it in the Nether.
- Breaking it drops the pot. The water is lost.

### Iron Hanging Pot

![An Iron Hanging Pot of water boiling over a campfire](/screenshots/iron-hanging-pot.png)

Works like the Copper Hanging Pot but boils slower: 6 seconds a serving.

![Two sticks and a chain across the top, five iron ingots in a U below, make an Iron Hanging Pot](/screenshots/iron-hanging-pot-recipe.png)
