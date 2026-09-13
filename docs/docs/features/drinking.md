# Drinking

## What is worth drinking

With AppleSkin installed, every item that restores thirst says so in its tooltip as two rows of
droplets rather than numbers, the way AppleSkin shows what food restores. Each droplet is worth two points. The upper row uses filled droplets for thirst; the lower row
uses outline droplets for quenched. A water bottle worth 6 thirst and 8 quenched therefore shows
three filled droplets above four outline droplets.

![A murky terracotta water bowl tooltip showing two thirst droplets and three quenched droplets](/screenshots/item-tooltip.png)

These are the values the mod ships with for vanilla and its own items:

| Item | Thirst | Quenched |
|---|---|---|
| Any potion, water bottles included | 6 | 8 |
| Milk bucket | 6 | 8 |
| Beetroot soup | 5 | 7 |
| Honey bottle | 4 | 6 |
| Melon slice | 4 | 5 |
| Terracotta water bowl | 4 | 5 |
| Waterskin, per drink | 4 | 5 |
| Apple, golden apple, enchanted golden apple, mushroom stew, rabbit stew | 2 | 3 |
| Carrot, golden carrot, beetroot, sweet berries, glow berries | 1 | 2 |

Thirst fills the bar, quenched fills the reserve behind it. Thirst past a full bar is not
wasted, it turns into extra reserve. Plain water follows vanilla food rules: a water bottle, water
bowl or waterskin cannot be used while the thirst bar is already full. Once even one point is
missing, drinking is allowed and any overflow can still become quenched. Potions and foods with
other uses are not blocked by this rule.

![Tooltips for a waterskin, a bottle of sea water, a clay bowl and an apple, which also shows AppleSkin's hunger rows](/screenshots/tooltips.png)

Dozens of items from Farmer's Delight, Farmer's Respite, Brewin' and Chewin', Collector's Reap and
Tough As Nails already have values too. None of those mods are required, the values just
sit unused until the item exists. For anything else, see
[keyword matching](/docs/configuration#enablekeywordmatching).

## Bowls

Everything the mod adds lives in its own creative tab. Water containers change their look with the
water inside them:

![Every item the mod adds: the clay and terracotta bowls, the water bowl at each grade and salty, the waterskin at each fill, and sea water in a bottle and a bucket](/screenshots/items.png)

The mod adds a bowl that survives being filled with water.

1. Three clay balls in a bowl shape, the same pattern as a wooden bowl, give four **clay bowls**.
2. Smelt a clay bowl into a **terracotta bowl**.
3. Hold the terracotta bowl and use it on water to scoop a **terracotta water bowl**. Flowing water
   works, you do not need a source block.

![Three clay balls in a bowl shape make four clay bowls](/screenshots/clay-bowl-recipe.png)

![A clay bowl firing into a terracotta bowl in a furnace](/screenshots/furnace-terracotta-bowl.png)

Drinking one leaves you holding the empty terracotta bowl again.

Every recipe the mod adds appears in the recipe book once you pick up an ingredient for it, so there
is nothing to look up outside the game.

There is also a crafting recipe, a terracotta bowl plus a water bucket, which returns the empty
bucket. Water made that way counts as dirty, because nothing tells the recipe where the bucket had
been. Scooping from the world is both cheaper and cleaner.

## Waterskin

Craft a reusable **waterskin** from three leather and one string:

![The waterskin recipe uses three leather and one string](/screenshots/waterskin-recipe.png)

It holds three drinks. Use it on water or a water cauldron to add one drink at a time. In the
inventory, right-click a waterskin with a water bottle to add one drink, or with a water bucket to
fill every remaining drink; the empty bottle or bucket is returned. Sneak and use a filled waterskin
on a block to pour away all of its water.

Mixing averages the grades of the drinks inside, rounded down, so adding one dirty drink drags the
whole waterskin down. Salt water is the exception: one salty drink makes all of it salt water. Each
drink restores 4 thirst and 5 quenched, and the empty waterskin is kept for refilling.

## Straight from the source

**Sneak and use an empty hand on water.** This is on by default and can be changed with
[canDrinkByHand](/docs/configuration#candrinkbyhand). It is worth a little less than a full bowl,
and it drinks the water exactly as it is, so a swamp puddle carries a swamp puddle's risks. Like
other plain water, it cannot be used while the thirst bar is full.

## Finding water

Water bottles turn up on their own, at acceptable or purified quality, one to three at a time:

- Abandoned mineshaft, simple dungeon, shipwreck supply, nether bridge and bastion chests
- Piglin bartering, though far more rarely than in a chest

That is enough to keep a player alive in the Nether, where filling anything from the ground is not
an option.

## Advancements

The mod has its own advancement tab. It is a guided tour of the water system rather than a
checklist, so most of it is earned by drinking.

| Advancement | How to earn it |
|---|---|
| Thirst Was Taken | Given when you start playing |
| Wet Your Whistle | Take your first drink of water |
| A Bitter Sip | Drink dirty water |
| Rolling Boil | Purify a container of water in a furnace |
| Crystal Clear | Drink water of the purest grade |
| Salt of the Earth | Drink sea water |
| Dry Heat | Drink in the Nether, or any dimension that boils water away |
