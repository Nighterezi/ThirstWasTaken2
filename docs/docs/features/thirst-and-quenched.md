# Thirst and Quenched

![The thirst bar above the hunger bar, part drained](/screenshots/thirst-bar.png)

## The two numbers

Thirst runs from 0 to 20 and is drawn as ten droplets, so one droplet is two points. New players
start full.

Quenched is a reserve that sits on top of it, exactly like saturation sits on top of hunger. It is
drawn as a lighter outline over the droplets and starts at 5. Anything that would cost you thirst
eats the reserve first, which is why a good drink lasts longer than a bad one even when both fill
the bar.

When the reserve runs out the droplets shake, the same warning the hunger bar gives.

## What drains it

Thirst uses the same exhaustion vanilla already tracks for hunger. Sprinting, jumping, swimming,
mining, attacking and taking damage all count. Every 4 points of exhaustion spends one point of
quenched, or one point of thirst once the reserve is empty.

Sitting on a horse, a boat or a minecart costs nothing. Creative and spectator players are ignored
entirely.

On top of that, the world you are in scales the cost:

| Where you are | Effect |
|---|---|
| A hot or dry biome | Drains faster |
| A cold or rainy biome | Drains slower |
| The Nether, or any dimension where water evaporates | A flat, much heavier rate |

Two potion effects push back. Fire Resistance halves the drain, and Fire Protection on your armour
slows it further, down to a quarter of normal at the deepest. Nausea does the opposite and adds a
steady extra drain while it lasts. Hunger does not, because the exhaustion it already causes is
counted once rather than twice.

On Peaceful the bar refills on its own instead of draining, unless the server turns that off.

Every one of these rates is a setting. See [Configuration](/docs/configuration#thirst-depletion).

## Running low

At 6 points or below you cannot start sprinting, the same cut-off vanilla uses for hunger.

Natural healing stops until thirst is nearly full. At 19 it starts again, though the quick
saturation healing only fires an eighth as often. Health you did not regain does not cost you any
food, so a dehydrated player does not quietly starve as well.

## Hitting zero

![An empty thirst bar with health down to two and a half hearts](/screenshots/dehydration.png)

An empty bar costs half a heart every two seconds. The damage ignores armour, and the death message
reads `died from dehydration`.

Difficulty decides how far it goes. On Easy it stops once you are down to five hearts. On Normal and
Hard it will kill you.
