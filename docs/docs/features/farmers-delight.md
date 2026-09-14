# Farmer's Delight

With [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated)
installed, its drinks and meals restore thirst, its Cooking Pot boils water clean, and Nourishment
keeps the thirst bar full. Nothing needs to be set up. Without Farmer's Delight, none of this is
loaded.

## Drinks and meals

![Farmer's Delight drinks and bone broth in the hotbar, with the bone broth tooltip showing thirst and hunger](/screenshots/farmers-delight-drinks.png)

| Item | Thirst | Quenched |
|---|---|---|
| Apple cider, melon juice, hot cocoa | 8 | 13 |
| Melon popsicle | 7 | 9 |
| Milk bottle, fruit salad | 6 | 8 |
| Bone broth | 5 | 7 |
| Beef stew, chicken soup, vegetable soup, fish stew, pumpkin soup, baked cod stew, noodle soup, onion soup, mixed salad, tomato sauce | 4 | 5 |
| Tomato, glow berry custard | 2 | 3 |
| Pumpkin slice | 2 | 1 |
| Cabbage leaf | 1 | 2 |

Every value can be changed in the [config](/docs/configuration#drinks-and-foods).

## Boiling water in the Cooking Pot

![A murky water bowl cooking in the Cooking Pot, with a pure water bowl ready to serve](/screenshots/farmers-delight-cooking-pot.png)

A heated Cooking Pot turns a fresh water bottle or terracotta water bowl that is Dirty, Murky or Clean
into Pure water in one go. It takes ten seconds, the same as a furnace, but a furnace only raises water two grades.

- A bowl comes out ready to take.
- A bottle works like the pot's own drinks. The pot gives the empty bottle back when it starts, and
  serves the water once a glass bottle is placed in the container slot, the one with the faded bowl.
  On Minecraft 1.21 and 1.21.1 the bottle comes out ready to take instead.

Salt water is refused, the same as in a furnace. Both recipes appear in the pot's recipe book once a
water bottle or water bowl has been picked up.

## Nourishment

While Nourishment is active, the thirst bar does not drain, the same way it stops hunger. Drinking
still works as normal, and salt water still costs thirst.
