# Drinking

## What is worth drinking

Every item that restores thirst says so in its tooltip, as `Hydration` and `Quenched`. These are the
values the mod ships with for vanilla and its own items:

| Item | Hydration | Quenched |
|---|---|---|
| Any potion, water bottles included | 6 | 8 |
| Beetroot soup | 5 | 7 |
| Melon slice | 4 | 5 |
| Terracotta water bowl | 4 | 5 |
| Apple, golden apple, enchanted golden apple, mushroom stew, rabbit stew | 2 | 3 |
| Carrot, golden carrot, beetroot, sweet berries, glow berries | 1 | 2 |

Hydration fills the bar, quenched fills the reserve behind it. Hydration past a full bar is not
wasted, it turns into extra reserve.

Dozens of items from Farmer's Delight, Farmer's Respite, Brewin' and Chewin', Collector's Reap,
Tough As Nails and Create already have values too. None of those mods are required, the values just
sit unused until the item exists. For anything else, see
[keyword matching](/docs/configuration#enablekeywordmatching).

## Bowls

The mod adds a bowl that survives being filled with water.

1. Three clay balls in a bowl shape, the same pattern as a wooden bowl, give four **clay bowls**.
2. Smelt a clay bowl into a **terracotta bowl**.
3. Hold the terracotta bowl and use it on water to scoop a **terracotta water bowl**. Flowing water
   works, you do not need a source block.

Drinking one leaves you holding the empty terracotta bowl again.

There is also a crafting recipe, a terracotta bowl plus a water bucket, which returns the empty
bucket. Water made that way counts as dirty, because nothing tells the recipe where the bucket had
been. Scooping from the world is both cheaper and cleaner.

## Straight from the source

Two ways to drink with nothing in your hands.

**Rain.** Look straight up while it is raining on you and you slowly take on water, a small amount
at a time. This is on by default.

**Sneak and use an empty hand on water.** Off by default. Turn it on with
[canDrinkByHand](/docs/configuration#candrinkbyhand). It is worth a little less than a full bowl,
and it drinks the water exactly as it is, so a swamp puddle carries a swamp puddle's risks.

## Finding water

Water bottles turn up on their own, at acceptable or purified quality, one to three at a time:

- Abandoned mineshaft, simple dungeon, shipwreck supply, nether bridge and bastion chests
- Piglin bartering, though far more rarely than in a chest

That is enough to keep a player alive in the Nether, where filling anything from the ground is not
an option.
