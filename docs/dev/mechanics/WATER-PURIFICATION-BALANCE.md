# Water purification balance

How every way of cleaning water in the mod compares, and why the hanging pots' numbers are what they
are. Read this before changing a boil time, a capacity or a purification recipe: each of them only
makes sense next to the others.

The unit throughout is a **serving**: one bottle or one bowl. A bottle restores 6 thirst and 8
quenched, a bowl 4 and 5 (`ThirstConfig.defaultDrinks()`); a thirst bar is 20. A bucket is three
servings, the same rate a cauldron uses.

## Every way to purify water

| Method | Time | Servings | Result | Fuel | Notes |
|---|---|---|---|---|---|
| Furnace, one bottle or bowl | 10 s | 1 | up two grades | yes | `SMELTING_TIME` in `ThirstRecipeProvider` |
| Furnace, one bucket | 10 s | 3 | up two grades | yes | a bucket is one item, so it is the furnace's best input |
| Furnace, dirty bucket to Pure | 20 s | 3 | Pure | yes | two passes: 0 → 2 → 3 |
| Smoker, one item | 5 s | 1 or 3 | up two grades | yes | `SMOKING_TIME`; half the furnace, as a smoker is for food. Also Sophisticated's Smoking upgrades |
| Campfire, four slots | 30 s | up to 12 (four buckets) | up two grades | no | `CAMPFIRE_TIME`; highest throughput of anything |
| Cooking Pot (Farmer's Delight) | 10 s | 1 | Pure | heat source below | only with Farmer's Delight installed |
| Teapot (Kaleidoscope Cookery) | 12 s | 4 teacups | safe tea, not water | heat source below, one tea bag | a teacup restores its fixed value whatever the water's grade, so Dirty water becomes four safe drinks. Fair for a tea bag and heat. Sea water is refused, so it never desalinates |
| **Copper Hanging Pot** | **4 s a serving, 12 s full** | **3** | **Pure** | no | needs a lit campfire below |
| **Iron Hanging Pot** | **6 s a serving, 18 s full** | **3** | **Pure** | no | needs a lit campfire below |

"Up two grades" follows `PURIFY_TABLE`: dirty becomes clean, murky and clean become Pure. Salt water
is never purified by any of them; that is distillation, on the [roadmap](ROADMAP.md).

## Where the hanging pot sits

The pot is not the fastest or the biggest producer, and should not be:

- **Against the campfire** it makes a quarter as much water, but always Pure and without taking the
  water out and putting it back for a second pass.
- **Against the furnace** a full copper pot (12 s) beats a dirty bucket's two passes (20 s plus fuel),
  and an iron pot (18 s) nearly matches it without fuel.
- **What it offers that nothing else does** is water drawn straight into bottles, bowls and
  waterskins, rain topping it up, and no fuel or second pass.

Boiling is timed per serving, the way a furnace times each item. Pouring more water in only adds the
new servings' time; what has boiled is kept, and water that is already Pure counts as boiled. The
water in a pot is one mixed batch with one grade, so, unlike a furnace, no serving can be taken out
Pure while the rest is still boiling.

## Copper and iron

The two pots differ in boil time only, based on the metals themselves:

| | Copper | Iron |
|---|---|---|
| Boil time | 4 s a serving (`COPPER_SECONDS_PER_SERVING`) | 6 s a serving (`IRON_SECONDS_PER_SERVING`) |
| Why | copper carries heat far better | iron carries heat worse |
| Heat source | lit campfire or soul campfire | the same, for now |
| Recipe | two sticks, a chain, five copper ingots | two sticks, a chain, five iron ingots |

### Under consideration: what makes iron worth it

A slower pot that costs iron needs a reason to exist. These are proposed but **not decided**:

- **More heat sources.** Iron boils over a lit campfire, a magma block, fire and lava, where copper
  only takes a campfire. A magma block gives a base a permanent boiling spot without a campfire. The
  pot only hangs from its frame over a campfire, so over magma it would stand on the block.
- **Heat retention.** Iron keeps boiling for about 5 seconds after its fire goes out, where copper
  stops at once.

### Set aside

- **Copper oxidising** like vanilla's copper blocks, boiling slower as it weathers, with honeycomb to
  stop it. It fits vanilla well, but needs three more textures and three more blocks per stage.

## Decisions already made

- **Capacity is one bucket, three servings.** The pot is smaller than a cauldron, so holding three
  buckets looked wrong next to one. It was nine servings at first.
- **A bucket is three servings, never nine.** A pot that turned one bucket into nine bottles would
  multiply water through a cauldron: three bottles into a cauldron give a bucket, and that bucket
  into the pot gives nine bottles. Water is infinite in the Overworld anyway, but not in the Nether.
- **Nothing can be poured into a pot where water evaporates**, as in the Nether. The water hisses
  away like a bucket emptied there, but the player keeps it.
