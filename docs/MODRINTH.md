## Thirst and Quenched
Your body requires water just like food. The system mirrors vanilla hunger:
* **Thirst Bar:** Depletes as you run, jump, mine, and fight.
* **Quenched Buffer:** Works like saturation, and drains first before your thirst bar drops.
* **Environment:** Hot biomes like deserts and the Nether deplete thirst faster. Fire Resistance and Fire Protection reduce heat drain.
* **Dehydration:** Low thirst prevents sprinting and natural health regeneration. Empty thirst causes steady damage.

<div align="center">
  <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/thirst-bar.png" alt="Thirst Bar" width="600">
</div>

## Water Quality and Purification
Water is graded based on where you collect it:

| Grade | Common Sources | Effects |
| :--- | :--- | :--- |
| **Dirty** | Swamps, stagnant pools | High chance of Poison, Nausea, and Hunger |
| **Murky** | Standard rivers, lakes, caves | Moderate chance of Nausea or Hunger |
| **Clean** | Mountain rivers, deep aquifers, boiled water | Safe to drink, high hydration |
| **Pure** | Glaciers, rain cauldrons, refined drinks | Completely safe, maximum hydration |
| **Salty** | Oceans and beaches | Cannot quench thirst; worsens dehydration |

* **Boiling:** Smelt water bottles, bowls, or buckets in a furnace or over a campfire to raise their purity grade.
* **Hanging Pots:** Hang a Copper or Iron Hanging Pot over a lit campfire to boil a bucket of water into Pure water.
* **Rain and Dripstone:** Cauldrons placed under open rain or pointed dripstone automatically fill with clean water.

<div align="center">
<table>
  <tr>
    <th align="center">Furnace</th>
    <th align="center">Iron Hanging Pot</th>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/furnace-clean-water.png" alt="Purifying Water" width="380"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/iron-hanging-pot.png" alt="Iron Hanging Pot boiling water over a campfire" width="280"></td>
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
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/clay-bowl-recipe.png" width="280"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/waterskin-recipe.png" width="280"></td>
  </tr>
</table>
</div>

## Mod Compatibility

Thirst Was Taken 2 connects seamlessly with popular survival mods out of the box:

### Fabric

<table>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/appleskin">AppleSkin</a></b><br>
      Displays your Quenched reserve directly on the HUD (Gold, Diamond, Ice, or Classic outline). Tooltips show exact thirst values for every drink and food item.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/hud-appleskin.gif" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/jade">Jade (WAILA)</a></b><br>
      Shows the purity grade of water sources, waterlogged blocks, and cauldrons directly under your crosshair.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/jade-water.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/farmers-delight-refabricated">Farmer's Delight</a></b><br>
      Soups, stews, and drinks restore thirst. The Cooking Pot purifies water to Pure grade. The Nourishment effect pauses thirst depletion.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/farmers-delight-cooking-pot.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/create-fly">Create Fly (26.2, 26.1.2)</a></b><br>
      Add Sand Filter to purify dirty water by one grade. Water also keeps its purity grade through pipes, pumps, tanks, drains, and spouts.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/create-sand-filter-goggles.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/modmenu">Mod Menu</a></b><br>
      Offers an in-game settings screen with a real-time HUD preview to customize drain rates, sickness chances, and HUD positioning.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-screen.png" width="100%">
    </td>
  </tr>
</table>

<details>
<summary><b>NeoForge</b></summary>

<table>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/appleskin">AppleSkin</a></b><br>
      Displays your Quenched reserve directly on the HUD (Gold, Diamond, Ice, or Classic outline). Tooltips show exact thirst values for every drink and food item.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/hud-appleskin.gif" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/jade">Jade (WAILA)</a></b><br>
      Shows the purity grade of water sources, waterlogged blocks, cauldrons, and hanging pots directly under your crosshair.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/jade-water.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b><a href="https://modrinth.com/mod/create">Create (1.21.1)</a></b><br>
      Add Sand Filter to purify dirty water by one grade. Water also keeps its purity grade through pipes, pumps, tanks, drains, and spouts.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/create-sand-filter-goggles.png" width="100%">
    </td>
  </tr>
  <tr>
    <td width="55%">
      <b>Settings screen</b><br>
      Open it from the Config button in NeoForge's Mods list, with a real-time HUD preview to customize drain rates, sickness chances, and HUD positioning.
    </td>
    <td width="45%">
      <img src="https://raw.githubusercontent.com/Nighterezi/ThirstWasTaken2/main/docs/public/screenshots/config-screen.png" width="100%">
    </td>
  </tr>
</table>

</details>

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

* **Original Mod:** Based on [Thirst Was Taken](https://modrinth.com/mod/thirst-was-taken) by [**ghen**](https://github.com/ghen-git).
* **License:** Licensed under the [MIT License](https://github.com/Nighterezi/ThirstWasTaken2/blob/main/LICENSE).