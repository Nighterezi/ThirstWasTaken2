<p align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/.github/assets/banner.png" alt="ThirstWasTaken2 banner" width="420">
</p>

Water stops being scenery. A second bar sits above hunger and empties as the day goes on, faster
while running and faster still in the Nether, so every trip out needs something to drink along the
way.

![The thirst bar above the food bar, part drained, with the quenched outline over it](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png)

## Running out

The bar has two parts, mirroring hunger and saturation. Filled droplets are thirst itself. Quenched is
a reserve that drains first and holds the bar steady while it lasts.

Natural healing slows as soon as the bar is not full. Sprinting stops once it drops to three
droplets. At zero, thirst starts taking health.

![An empty thirst bar with health down to two and a half hearts](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/dehydration.png)

## Better with AppleSkin

AppleSkin is optional and fully supported. With it installed, quenched is drawn as an outline over
the droplets, the way AppleSkin outlines saturation on the hunger bar, and the thirst bar gets the
same exhaustion underlay. The outline comes in Diamond, Ice, Gold or AppleSkin's own gold, or can be
turned off.

![The thirst and food bars without AppleSkin, then with each quenched outline colour](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/hud-appleskin.png)

Tooltips show how much thirst and quenched an item restores, right next to AppleSkin's hunger rows.

![Tooltips for a waterskin, a bottle of sea water, a clay bowl and an apple](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/tooltips.png)

## Drinking

Almost anything wet works. Potions, soups, juices, milk, honey and drinks from other mods all restore
thirst, and a plain water bottle is the simplest of them. Sneaking and using an empty hand on water drinks
straight from the source, which costs nothing and is rarely clean.

## Not all water is safe

Every container remembers where its water came from, and its tooltip says how clean it is.

![Water bottle tooltips for Dirty, Murky, Clean and Pure water](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-purity.png)

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

| Into the fire | Out of the fire |
|---|---|
| Dirty | Clean |
| Murky | Pure |
| Clean | Pure |

![A dirty terracotta water bowl comes out of the furnace clean](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/furnace-clean-water.png)

![Water bottles and bowls being purified over a campfire](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/boilling-water.png)

A cauldron left out in the rain fills with clean water. A pointed dripstone dripping into one fills
it with pure water, slowly, and without any fuel.

## Carrying it

Clay is the early answer. Three clay balls in a bowl shape make four Clay Bowls.

![Three clay balls in a bowl shape make four clay bowls](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/clay-bowl-recipe.png)

A furnace turns each one into a Terracotta Bowl, and that bowl scoops from any water, including the
flowing kind a glass bottle refuses.

![A clay bowl firing into a terracotta bowl in a furnace](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/furnace-terracotta-bowl.png)

Bowls and bottles hold one drink each. The waterskin holds three in the same slot, keeps their
purity, and mixes what is poured into it, which makes three leather and a piece of string an easy
trade.

![The waterskin recipe, three leather and one string in a crafting table](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/waterskin-recipe.png)

Every container shows what it holds at a glance.

![Every item the mod adds: the clay and terracotta bowls, the water bowl at each grade and salty, the waterskin at each fill, and sea water](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/items.png)

Every recipe shows up in the recipe book, and the mod has an advancement tab that walks through the
water system as it is discovered.

## Settings

Everything is adjustable through Mod Menu or `config/thirstwastaken2.json`: how fast the bar drains,
how harsh dirty water is, where the bar sits on screen, and which items count as a drink. The screen
previews the thirst bar and a tooltip as settings change, and every page has a reset button.

![The config screen with its live preview and one button per page](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-screen.png)

The server decides the gameplay settings. Each player decides their own HUD and AppleSkin settings.

## Languages

English, French, Japanese, Korean, Polish, Russian, Vietnamese, Simplified Chinese and Traditional
Chinese. Each player sees the mod in their own game language.

| Tooltips | Settings |
|---|---|
| ![Item tooltips in Simplified Chinese](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/chinese-tooltips.png) | ![The HUD and AppleSkin settings page in Simplified Chinese](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/chinese-config.png) |

## Credits

ThirstWasTaken2 is [a fork](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
of [Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by
[**ghen**](https://github.com/ghen-git), rebuilt for Fabric and extended since.

- [Documentation](https://nighterezi.github.io/ThirstWasTaken2/)
- [Changelog](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/CHANGELOG.md)
- [What changed from the original](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/FORK-STATUS.md)
- [Source code](https://github.com/Nighterezi/ThirstWasTaken2)
- [Report a problem](https://github.com/Nighterezi/ThirstWasTaken2/issues)
