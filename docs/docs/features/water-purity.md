# Water Purity

Every container of water carries one of four grades, shown in its tooltip.

![Item tooltips reading Dirty, Slightly Dirty, Acceptable and Purified](/screenshots/water-purity.png)

## Where the grade comes from

A bottle, bucket or terracotta bowl is stamped at the moment it is filled, and keeps that stamp
until something changes it. Two things decide the grade: how high up the water is, and whether it is
moving.

| Where you fill it | Grade |
|---|---|
| A still pond or ocean at ordinary heights | Dirty |
| A stream at ordinary heights | Slightly dirty |
| Still water above y 100, or below y 48 | Slightly dirty |
| A stream above y 100, or below y 48 | Acceptable |

So the pond outside your door is the worst water in the game, and a mountain waterfall is drinkable
as it is. Both heights and the bonus for moving water are settings, under
[Water Purity](/docs/configuration#water-purity).

A container that was never stamped, such as a drink added by another mod, counts as acceptable.

Cauldrons remember. Pour a container in and the cauldron keeps the worse of the two grades, so
topping up clean water with dirty water spoils the batch. Draw from it and the bottle or bucket you
get back is stamped with what was in there.

## Drinking bad water

Dirty water still hydrates you. The risk is what comes with it: Nausea and Hunger together, or
Poison, rolled once per drink.

| Grade | Nausea and Hunger | Poison |
|---|---|---|
| Dirty | 100% | 30% |
| Slightly dirty | 50% | 10% |
| Acceptable | 5% | none |
| Purified | none | none |

Nausea lasts five seconds and the Hunger it comes with lasts thirty. Poison lasts ten seconds. By
default a drink that poisons you still fills the bar, so bad water is a trade rather than a waste.

## Cleaning it

### Cooking

Put a water bottle, a terracotta water bowl or a water bucket in a furnace or on a campfire.

| In | Out |
|---|---|
| Dirty | Acceptable |
| Slightly dirty | Purified |
| Acceptable | Purified |

A furnace takes ten seconds, a campfire thirty. Dirty water needs two passes to reach purified.

### Sand filter

::: warning Not available yet
The Sand Filter is broken and its Create Fly integration is turned off, so bulk purification is not
possible on this release. The description below is what it will do when support returns.
:::

With Create Fly installed, a Sand Filter can be crafted from six brass ingots, two iron bars and one
sand. Pump water through it and it comes out one grade cleaner. It is the only way to purify water
in bulk, and the only part of the mod that needs another mod to exist.

### Height and weather

Cheapest of all, and free: collect from a mountain stream instead of the pond next to your base.
Flowing water high up can already be acceptable or better before you cook it.
