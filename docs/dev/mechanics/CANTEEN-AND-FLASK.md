# Copper Canteen and Iron Flask

Two carried water containers next to the waterskin, crafted from scratch rather than upgraded from
one, that boil their own water. Implemented; this file records what was decided and why, and where
the code went. The numbers sit next to every other purification method in
[WATER-PURIFICATION-BALANCE.md](WATER-PURIFICATION-BALANCE.md).

## What they are

| | Waterskin | Copper Canteen | Iron Flask |
|---|---|---|---|
| Id | `waterskin` | `copper_canteen` | `iron_flask` |
| Capacity | 3 servings | 4 servings | 6 servings |
| Boil over a lit campfire, holding use | no | 3 s a serving, 12 s full | 4 s a serving, 24 s full |
| Boil result | | Pure | Pure |
| Furnace | no | no | yes, up two grades, 10 s whatever the fill |
| Sprite | four fill states | one (`copper_canteen.png`) | one (`iron_flask.png`) |
| Fill shown by | sprite and item bar | item bar | item bar |

- **Copper boils faster, iron holds more.** The same reasoning as the pots: copper carries heat better.
- **Only the flask goes in a furnace.** The canteen's edge is speed on the fire; the flask's is
  capacity and a way to clean water unattended.
- **The hanging pots are mostly decorative now.** They keep working unchanged; they are just no longer
  the main way to boil water.
- **Salt water is never purified**, on a campfire or in a furnace.

### Recipes

```
Copper Canteen          Iron Flask
[   ][ L ][   ]         [   ][ N ][   ]
[ C ][   ][ C ]         [ I ][   ][ I ]
[ C ][ C ][ C ]         [ I ][ I ][ I ]

L = leather (the strap)               N = #c:nuggets/iron (the cap)
C = #c:ingots/copper                  I = #c:ingots/iron
```

The waterskin's shape, a U with one item on top, so a player who knows one guesses the others. The top
row keeps them clear of the pots (`SKS` on top), the cauldron and the bucket.

## How boiling works

Holding use on a block repeats the use every 4 ticks (vanilla's right-click delay), as long as the
item is not being used. `WaterskinItem.useOn` takes each repeat as one step of `BOIL_STEP_TICKS`:

- It returns `CONSUME` on a lit campfire (soul campfires too) whenever the vessel holds water, even
  Pure water, so a player still holding use when the boil finishes does not start drinking. Salt
  water is refused with a message. Empty, unlit or anywhere else passes, and the vessel drinks as
  usual. `CONSUME` rather than `SUCCESS`: a hand swinging at the fire every 4 ticks reads as punching it.
- Progress shows as a percentage on the action bar, a cloud of steam every third step, and the
  brewing sound when it is done.
- **Progress lives on the server, per player, not on the stack.** A component written every step
  re-syncs the held stack, and the client plays the re-equip animation for each changed stack in the
  hand. `WaterskinItem.BOILING` is a `WeakHashMap<Player, Boil>`, server thread only.
- Letting go keeps the progress. It applies to the same stack object only while it holds no more water
  and the same grade as at the last step, so adding water restarts it and drinking keeps it (the rest
  may then be done already). Moving the stack to another slot can lose it; that is harmless.
- The Nether is allowed: the vessel is sealed, and evaporation only applies to pouring.
- **Neither item has a campfire-cooking recipe.** Vanilla's campfire puts anything that has one into
  its slots, which would swallow the use.
- No advancement for a campfire boil. `boil_water` is met by the furnace recipes, the flask's included.

## Where the code went

- `item/WaterskinItem` is the one class for all three, with `capacity`, `boilTicksPerServing` and
  whether the sprite follows the fill. Callers ask `WaterskinItem.is(stack)`, `capacity(stack)` and
  `hasRoom(stack)`, never `stack.is(ThirstItems.WATERSKIN)`. `CAPACITY` stays the waterskin's own 3;
  `MAX_CAPACITY` bounds the servings component; a bucket is always `BUCKET_SERVINGS`, three.
- `item/ThirstItems`: the two registrations, which read capacity and boil time from the config
  (`copperCanteenCapacity`, `ironFlaskCapacity`, `copperCanteenBoilSeconds`, `ironFlaskBoilSeconds`,
  and `enableBoilingInHand`). Capacity goes up to `MAX_CAPACITY` and no further, because the flask's
  furnace recipes are generated one per fill level up to it.
- `purity/ThirstComponents`: `water_servings` widened to `0..6`, which keeps every saved stack valid.
- Every waterskin call site switched to the helpers: `WaterContainers`, `WaterInteractions`,
  `WaterPurity`, `HangingPotInteractions`, `ThirstTooltip`, `ThirstApi.thirstValues`, and the three
  loader fluid registrations (Fabric `WaterContainerStorage`, both NeoForge `WaterContainerCapabilities`).
  No API signature changed.
- Datagen: the two shaped recipes, 18 `purify_water_iron_flask_<servings>_<grade>_smelting` recipes
  with one shared unlock, flat item models, and the flask recipes added to `boil_water`.
- Lang: two item names and three `thirstwastaken2.message.*` lines, in all nine files.
- No version fork: `useOn`, `CampfireBlock.isLitCampfire` and `InteractionResult.CONSUME` are the same
  on every node, and the action bar goes through `Vanilla.sendOverlayMessage`.

## Tests

`CanteenGameTest`: capacities, the single sprite, a full boil and one step short of it, copper
quicker than iron over a soul campfire, kept and restarted progress, salt, unlit and the waterskin
not boiling, the furnace taking every flask fill level and neither the canteen nor a campfire
recipe, and both crafting recipes. `CreativeTabGameTest` lists the two items.

What a gametest cannot reach, the real held right click, the action bar and the crafting screen, is
[tools/agent/gameplay/canteen.jsonl](../../../tools/agent/gameplay/canteen.jsonl). It also captures the
two filled crafting grids that `copper-canteen-recipe.png` and `iron-flask-recipe.png` are made from.

## Sprites

Both start at row 1 and end at row 14, like the empty waterskin, so the cork, the cap and the bodies
line up in a hotbar. The waterskin still swells to row 15 as it fills; that is its fill display.
