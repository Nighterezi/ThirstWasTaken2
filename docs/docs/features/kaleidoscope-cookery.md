# Kaleidoscope Cookery

With Kaleidoscope Cookery installed, its teas and soups restore thirst, and water keeps its grade in
the Stockpot and the Teapot.

::: warning Supported versions
Thirst from teas and soups works wherever Kaleidoscope Cookery is installed. Water keeping its grade
works on NeoForge, Minecraft 1.21.1, with
[Kaleidoscope Cookery](https://modrinth.com/mod/kaleidoscope-cookery) 1.5.1. On Fabric only the teas and
soups count for now. Use
[Kaleidoscope Cookery Refabricated](https://modrinth.com/mod/kaleidoscope-cookery-refabricated) there:
the official Fabric build stopped at 1.0.1 and has no Teapot.
:::

![A Teapot on a lit Stove among teacups in a cherry grove, with Jade naming its water Clean](/screenshots/kaleidoscope-teapot.png)

## Teas and soups

![Kaleidoscope Cookery teas in the hotbar, with the Sakura Fubuki tooltip showing its thirst and quenched droplets](/screenshots/kaleidoscope-teas.png)

| Item | Thirst | Quenched |
|---|---|---|
| Clay Pot Milk Tea | 8 | 12 |
| Butter Tea | 6 | 10 |
| Wheat Aroma Oolong Tea, Tieguanyin, Biluochun, Dong Ding Oolong Tea, Sakura Fubuki, Flower Tea | 6 | 9 |
| Pork Bone Soup | 5 | 7 |
| Seafood Miso Soup, Fearsome Thick Soup, Mutton and Radish Soup, Wild Mushroom Rabbit Soup, Pufferfish Soup, Borscht, Beef Meatball Soup, Chicken and Mushroom Stew, Laba Congee, Donkey Soup, Tomato Beef Brisket Soup | 4 | 5 |
| Mystery Tea | 3 | 3 |
| Beef Noodle, Lamb Hui Noodles, Udon Noodle | 3 | 4 |
| Tomato | 2 | 3 |

Tea is brewed from boiled water, so a cup is safe whatever water went into the Teapot. Dishes eaten
straight off a placed block are solid food and restore no thirst. Every value can be changed in the
[config](/docs/configuration#drinks-and-foods).

## Water in the Stockpot and the Teapot

- A bucket of water poured into a Stockpot or a Teapot and taken back out keeps its grade. Without
  this, both handed back Clean water whatever went in.
- A Teapot picked up with water in it keeps the grade when it is placed again.
- An empty Teapot dipped into water grades it where it lies, the way filling a bucket there does.
- Water dripping into a Teapot from pointed dripstone is Pure, as it is in a cauldron.
- The Teapot refuses sea water, from a bucket or straight from the sea. Tea brewed from it would come
  out safe, which would make the sea drinkable.
- The Stockpot takes sea water, and a bucket taken back out is still sea water.

With Jade installed, looking at a Stockpot or a Teapot of water shows its grade under the crosshair.
