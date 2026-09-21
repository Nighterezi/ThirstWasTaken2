<div align="center">

<br>

[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/modrinth_64h.png)](https://modrinth.com/mod/thirst-was-taken-2)
[![curseforge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/available/curseforge_64h.png)](https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken-2)
[![ghpages](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/documentation/ghpages_64h.png)](https://nighterezi.github.io/ThirstWasTaken2/)

</div>

## Thirst and Quenched
Your body requires water just like food. The system mirrors vanilla hunger:
* **Thirst Bar:** Depletes as you run, jump, mine, and fight.
* **Quenched Buffer:** Works like saturation, and drains first before your thirst bar drops.
* **Environment:** Hot biomes like deserts and the Nether deplete thirst faster. Fire Resistance and Fire Protection reduce heat drain.
* **Dehydration:** Low thirst prevents sprinting and natural health regeneration. Empty thirst causes steady damage.

<div align="center">
<table>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-food-bars.png" alt="The thirst bar above the hunger bar" width="372"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/water-tooltips.gif" alt="A water bottle tooltip showing each grade" width="272"></td>
  </tr>
</table>
</div>

## Water Quality and Purification
Water is graded based on where you collect it:

| Grade | Common Sources | Effects |
| :--- | :--- | :--- |
| **Dirty** | Swamps, stagnant pools | High chance of Poison and Nausea |
| **Murky** | Standard rivers, lakes, caves | Moderate chance of Nausea |
| **Clean** | Mountain rivers, deep aquifers, boiled water | Safe to drink, high hydration |
| **Pure** | Glaciers, rain cauldrons, refined drinks | Completely safe, maximum hydration |
| **Salty** | Oceans and beaches | Cannot quench thirst; makes you Parched |

* **Boiling:** Smelt water bottles, bowls, or buckets in a furnace or over a campfire to raise their purity grade.
* **Hanging Pots:** Hang a Copper or Iron Hanging Pot over a lit campfire to boil a bucket of water into Pure water.
* **Rain and Dripstone:** Cauldrons placed under open rain or pointed dripstone automatically fill with clean water.

<div align="center">
<table>
  <tr>
    <th align="center">Iron Hanging Pot</th>
    <th align="center">Furnace</th>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/iron-hanging-pot.png" alt="Iron Hanging Pot boiling water over a campfire" width="300"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/furnace-clean-water.png" alt="Purifying Water" width="380"></td>
  </tr>
</table>
</div>

## Early Game Gear and Drinking
You do not need glass bottles to stay hydrated:
* **Terracotta Bowls:** Mold clay into bowls and fire them in a furnace to scoop water early on.
* **Waterskin:** Holds 3 servings of water in a single slot. Intelligently mixes water grades.
* **Drink by Hand:** Sneak and right-click any fresh water block to drink directly without a container.

<div align="center">
<table>
  <tr>
    <th align="center">Clay Bowl</th>
    <th align="center">Waterskin</th>
  </tr>
  <tr>
    <td align="center"><img alt="Three clay balls in a bowl shape make four Clay Bowls" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/clay-bowl-recipe.png" width="280"><br><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/furnace-terracotta-bowl.png" alt="Firing a Clay Bowl into a Terracotta Bowl" width="280"></td>
    <td align="center"><img alt="Three leather and a string make a Waterskin" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/waterskin-recipe.png" width="280"></td>
  </tr>
</table>
</div>

## Mod Compatibility

Thirst Was Taken 2 integrates seamlessly with popular mods out of the box:

<table>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/appleskin">AppleSkin</a></b><br>
      <i>Fabric and NeoForge</i><br><br>
      Displays your Quenched reserve directly on the HUD, with multiple styles available to choose from. Tooltips show exact thirst values for food and drinks.
    </td>
    <td width="45%">
      <img alt="The thirst bar with AppleSkin, cycling through the Diamond, Ice, Gold, AppleSkin and Legacy quenched outlines" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/hud-appleskin.gif" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/jade">Jade (WAILA)</a></b><br>
      <i>Fabric and NeoForge</i><br><br>
      Shows the purity grade of water sources, waterlogged blocks, and cauldrons directly under your crosshair.
    </td>
    <td width="45%">
      <img alt="Jade showing the water grade under the crosshair" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/jade-water.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://nighterezi.github.io/ThirstWasTaken2/docs/features/farmers-delight">Farmer's Delight</a></b><br>
      <i>Fabric: <a href="https://modrinth.com/mod/farmers-delight-refabricated">Farmer's Delight Refabricated</a><br>
      NeoForge: <a href="https://modrinth.com/mod/farmers-delight">Farmer's Delight</a> on 1.21.1</i><br><br>
      Soups, stews, and drinks restore thirst. The Cooking Pot purifies water to Pure grade. The Nourishment effect pauses thirst depletion.
    </td>
    <td width="45%">
      <img alt="A Farmer's Delight Cooking Pot boiling water bottles to Pure" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/farmers-delight-cooking-pot.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://nighterezi.github.io/ThirstWasTaken2/docs/features/create">Create</a></b><br>
      <i>Fabric: <a href="https://modrinth.com/mod/create-fly">Create Fly</a> on 26.1.2 and 26.2 (no 26.3 build)<br>
      NeoForge: <a href="https://modrinth.com/mod/create">Create</a> on 1.21.1</i><br><br>
      Adds a Sand Filter to purify dirty water by one grade. Water also keeps its purity grade through pipes, pumps, tanks, drains, and spouts.
    </td>
    <td width="45%">
      <img alt="Engineer's Goggles showing Murky water entering the Sand Filter and Clean water leaving it" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/create-sand-filter-goggles.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/sophisticated-backpacks">Sophisticated Backpacks</a></b><br>
      <i>NeoForge on 1.21.1, 1.21.11, 26.1.2 and 26.2 (no 26.3 build)</i><br><br>
      Adds the Drinking Upgrade, which drinks from your backpack when you get thirsty, the cleanest water first. Water keeps its purity grade in the Tank and Pump Upgrades. Also works with <a href="https://modrinth.com/mod/sophisticated-storage">Sophisticated Storage</a>.
    </td>
    <td width="45%">
      <img alt="A backpack of water with the Advanced Drinking Upgrade's settings open" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/sophisticated-drinking-upgrade.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://nighterezi.github.io/ThirstWasTaken2/docs/features/supplementaries">Supplementaries</a></b><br>
      <i>Fabric and NeoForge: <a href="https://modrinth.com/mod/supplementaries">Supplementaries</a> on 1.21.1</i><br><br>
      Water keeps its purity grade in Jars, Goblets and Faucets, and sea water stays sea water. A Jar or a Goblet of water can be drunk straight from the block, and the water inside is coloured by its grade. Faucets fill and empty hanging pots and grade the water they draw from a lake.
    </td>
    <td width="45%">
      <img alt="Five jars of water side by side, brown, grey blue, blue, cyan and turquoise, with Jade naming the middle one Clean" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/supplementaries-jars.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://nighterezi.github.io/ThirstWasTaken2/docs/configuration">Settings screen</a></b><br>
      <i>Fabric and NeoForge</i><br><br>
      Configure the mod in game with a live HUD preview. Fabric requires <a href="https://modrinth.com/mod/modmenu">Mod Menu</a>; NeoForge uses the Config button in its Mods list.
    </td>
    <td width="45%">
      <img alt="The ThirstWasTaken2 settings screen with its live HUD preview" src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-screen.png" width="100%">
    </td>
  </tr>
</table>

## Version Support

| Minecraft version | Mod version | Support status |
|---|---|---|
| 26.3 | Latest | Active |
| 26.2 | Latest | Active |
| 26.1.x | Latest | Active |
| 1.21.11 | Latest | Active |
| 1.21.1 | Latest | Active |
| 1.21 (Fabric only) | Latest | Active |

## Quick FAQ

**Is the mod required on both client and server?**  
Yes. The mod needs to be installed on both sides for packet synchronization and HUD display.

**Does it work in Peaceful mode?**  
Yes. In Peaceful mode, thirst naturally regenerates over time.

**Can I use this in a modpack?**  
Yes. You are free to include Thirst Was Taken 2 in any public or private modpack.

**Supported languages:**  
English, Vietnamese, Simplified Chinese, Traditional Chinese, French, Japanese, Korean, Polish, and Russian.

![Item tooltips in Simplified Chinese](https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/chinese-tooltips.png)

Want to improve or add a translation? [Open a pull request](https://github.com/Nighterezi/ThirstWasTaken2).

## Credits

* Based on [Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by [ghen](https://github.com/ghen-git), under the MIT License.
* Hanging Pot models adapted from [Dehydration](https://github.com/Globox1997/Dehydration) by [Globox1997](https://github.com/Globox1997), under the GPL-3.0.
* Parched effect icon inspired by [Yet Another Thirst](https://modrinth.com/mod/yet-another-thirst) by [minhnh303](https://github.com/minhnh303), redrawn from scratch.
* Licensed under the [GPL-3.0](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/LICENSE) from 1.0.7. See [CREDITS.md](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/CREDITS.md).

## Community

<div align="center">

[![discord](https://cdn.modrinth.com/data/cached_images/ed9e22abe916888bc68150b8ad3b2afa4adbc241.png)](https://discord.com/invite/YwD9Xv7Beu)

</div>
