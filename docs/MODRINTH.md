<p align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">
</p>

Water stops being scenery. A second bar sits above hunger and empties as the day goes on, faster
while running and faster still in the Nether, so every trip out needs something to drink along the
way.

| Thirst bar | Water purity |
|---|---|
| ![Thirst bar above the hunger bar, part drained](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png) | ![Water bottle tooltips showing Dirty, Slightly Dirty, Acceptable and Purified](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png) |

## Running out

The bar has two parts, mirroring hunger and saturation. Filled droplets are thirst itself. Outlined
droplets on the second row are quenched hydration, a reserve that drains first and holds the bar
steady while it lasts.

Natural healing slows as soon as the bar is not full. Sprinting stops once it drops to three
droplets. At zero, thirst starts taking health.

![An empty thirst bar with health down to two hearts](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/dehydration.png)

## Drinking

Almost anything wet works. Potions, soups, juices, milk, honey and drinks from other mods all restore
thirst, and a plain water bottle is the simplest of them. Sneaking and using an empty hand on water drinks
straight from the source, which costs nothing and is rarely clean.

Every container remembers where its water came from, and every tooltip says so.

![A water bottle tooltip showing filled thirst droplets and outlined quenched droplets](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/item-tooltip.png)

## Not all water is safe

Fresh water is graded by the biome it came from.

| Where the water comes from | Usual grade |
|---|---|
| Mountains | Clean |
| Rivers | Murky |
| Most other biomes | Murky |
| Swamps, jungles, savannas and badlands | Dirty |

Hot biomes, and mud, farmland or a composter within a couple of blocks, make water worse. Cold
biomes and water that is high up or deep underground make it better.

Dirty water still quenches thirst, but it can bring Nausea, Hunger or Poison with it.

Oceans and beaches are not a grade of fresh water at all. Sea water has its own icon and its own
tooltip line, restores nothing, causes Nausea, and no fire will make it drinkable.

## Making it safe

A furnace or a campfire raises the grade of anything drinkable, bottles, buckets and bowls alike.

![Water bottles and bowls being purified over a campfire](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/boilling-water.png)

| Into the fire | Out of the fire |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

A cauldron left out in the rain fills with clean water. A pointed dripstone dripping into one fills
it with pure water, slowly, and without any fuel.

## Carrying it

Clay is the early answer. Three clay balls make four Clay Bowls, a furnace turns each one into a
Terracotta Bowl, and that bowl scoops from any water, including the flowing kind a glass bottle
refuses.

![The ThirstWasTaken2 creative tab containing its bowls and waterskin](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/creative-tab.png)

Bowls and bottles hold one drink each. The waterskin holds three in the same slot, keeps their
purity, and mixes what is poured into it, which makes three leather and a piece of string an easy
trade.

![The waterskin recipe, three leather and one string in a crafting table](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/waterskin-recipe.png)

Every recipe shows up in the recipe book, and the mod has an advancement tab that walks through the
water system as it is discovered.

## Settings

Everything is adjustable through Mod Menu or `config/thirstwastaken2.json`: how fast the bar drains,
how harsh dirty water is, where the bar sits on screen, and which items count as a drink.

The server decides the gameplay settings. Each player decides their own HUD settings.

AppleSkin is supported. With it installed and its exhaustion underlay switched on, the thirst bar
gets the same underlay the hunger bar has.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese.

## Credits

ThirstWasTaken2 is [a fork](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
of [Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by
[**ghen**](https://github.com/ghen-git), rebuilt for Fabric and extended since.

- [Documentation](https://nighterezi.github.io/ThirstWasTaken2/)
- [Changelog](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/CHANGELOG.md)
- [What changed from the original](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
- [Source code](https://github.com/Nighterezi/ThirstWasTaken2)
- [Report a problem](https://github.com/Nighterezi/ThirstWasTaken2/issues)
