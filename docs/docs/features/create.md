# Create

With Create installed, the mod adds the Sand Filter and keeps water grades intact as water moves
through pipes, pumps, spouts and drains. Without Create, none of this is loaded.

::: warning Supported versions
- Fabric, Minecraft 26.1.2 and 26.2: [Create Fly](https://modrinth.com/mod/create-fly), the Fabric port of Create.
- NeoForge, Minecraft 1.21.1: [Create](https://modrinth.com/mod/create) 6.0.10.

Every other version ignores Create.
:::

![A Create Fly pipe network pumping water through a Sand Filter between two fluid tanks](/screenshots/create-sand-filter.png)

## Sand Filter

The Sand Filter raises water one grade as it passes through. Pump water into the top and pump it out
of the bottom. Pipes can touch the sides, but nothing flows through them.

| In | Out |
|---|---|
| Dirty | Murky |
| Murky | Clean |
| Clean | Pure |
| Pure | Pure |

It filters 10 mB a tick, a bucket every five seconds, and holds one bucket on each side. Salt water
passes through unchanged. Only water can be pumped in.

Filtered water of a different grade cannot join water already waiting in the bottom. The filter waits
until that water is pumped out.

A comparator reads how full the bottom is. Engineer's Goggles show both sides and their grades.

![Engineer's Goggles showing Murky water entering the Sand Filter and Clean water leaving it](/screenshots/create-sand-filter-goggles.png)

### Recipe

Crafted in a column, top to bottom: any sand, a Nozzle, a Fluid Tank. The recipe unlocks once a
Nozzle has been picked up.

![Sand, a Nozzle and a Fluid Tank in a column make a Sand Filter](/screenshots/sand-filter-recipe.png)

## Water grades in Create

Water keeps its grade everywhere Create moves it:

- A pump or Hose Pulley grades water from the world the same way a bottle would. A cauldron gives the
  grade it holds.
- Pouring a bottle or bucket into a Basin, Item Drain or tank keeps its grade.
- Filling a bottle or bucket from a Spout or by hand gives it the grade of the water it came from.

Water of different grades does not mix in one tank or pipe. Keep sources of different grades on
separate pipes, or filter them to the same grade first.
