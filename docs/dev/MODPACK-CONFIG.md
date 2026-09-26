# Config for modpack makers: plan

Settings a modpack maker needs without opening `config/thirstwastaken2.json`. This file sets the order
of work, what each step needs and how each one is checked. Once a step is built, how it works goes in
the `AGENTS.md` of the area it touches.

Written on 2026-09-26.

## What is left out, on purpose

**No config sync from server to client.** Each player keeps their own config file, and a sync
would overwrite it. On a dedicated server the server's file decides what drinking does. A client's
tooltips and AppleSkin read that client's own file, except for the data pack values, which
`DrinkValuesPayload` already syncs. A pack that changes item values ships the same config to server
and clients, or uses a data pack (`data/<ns>/thirstwastaken2/drinks/`), which does sync.

## Order of work

| Step | What | State |
|---|---|---|
| 1 | Edit item values in the config screen | done, not committed |
| 2 | Switch off the mod's own items | done, not committed |
| 3 | Expose the hard-coded numbers, and switches for single mechanics | planned |

## Step 1: item values in the config screen (done)

The Item Values page lists every id in `drinks`, `foods` and `itemBlacklist`, below its two switches
and the Open button. The Open button is now only for the keyword patterns. Each row has:

- icon and name;
- a thirst and a quenched box, 0 to `ThirstData.MAX`;
- a switch, which adds the id to `itemBlacklist` or removes it;
- reset: the mod's value for a default id, removal for any other id.

Rows are grouped by mod, under a heading that opens and closes the group, with the mod's name and
how many of its items are listed. Only vanilla starts open. The header search also matches a mod's
name or namespace, and then shows all of that mod's items.

A row at the top adds an item. It completes the id from the item registry, and a new item gets
`drinkTagValue`. Ids of mods that are not installed are hidden, with a note counting them. The header
search finds item rows by id or name. `sanitize()` now clamps every item value into `0..ThirstData.MAX`
and drops entries of the wrong length.

How it works: [src/client/java/com/thirstwastaken2/client/AGENTS.md](../../src/client/java/com/thirstwastaken2/client/AGENTS.md),
"Item values". Code: `client/config/ItemValueRows`.

Checked: `build` on `26.3.x` and `1.21.1`; compiled on `26.1.x`, `26.2.x`, `1.21.11`, `26.3.x-neoforge`,
`1.21.1-neoforge`; `checkLang`, `checkVersionSeam`; a screenshot of the page on 26.3.

Since then `26.1.x-neoforge`, `26.2.x-neoforge` and `1.21.11-neoforge` compile too.

**Still to check by hand:** typing into the boxes in game (the agent client cannot type). Test the
responder that refuses an invalid key: a letter, a value over 20, then Cancel and Done.

## Step 2: switch off the mod's own items (done)

A pack that brings its own canteen or pot switches ours off, and players can no longer get it. Items
stay registered: registries must match between server and client, and removing an item would break
worlds that hold it. "Off" means it cannot be crafted and does not show in the creative tab. A stack
that already exists keeps working.

Built as planned:

- Six config booleans, all `true`: `enableBowls` (clay, terracotta and water bowl together),
  `enableWaterskin`, `enableCopperCanteen`, `enableIronFlask`, `enableCopperHangingPot`,
  `enableIronHangingPot`. `ThirstConfig.isItemEnabled(String id)` maps an item id to its field, `true`
  for any other id.
- A **Mod Items** page (`mod_items`, icon `copper_canteen.png`) with the six switches, under a note that
  a change needs `/reload` or rejoining, and a restart on a dedicated server. Lang keys in all nine files.
- The load condition `thirstwastaken2:item_enabled {"item": id}`, registered through
  `Loader.registerResourceConditions()`: `platform/ItemEnabledCondition` on Fabric (forked on `test`'s
  parameter), `neoforge/ItemEnabledCondition` in `CONDITION_SERIALIZERS` on NeoForge.
- Datagen puts it on every recipe and unlock of the mod's items, the bowl and flask purification
  recipes and the Cooking Pot bowl recipe included, and on nothing for bottles or buckets.
  `neoForgeConditions` moves its `condition` key to `type`.
- The creative tab leaves out a switched-off item. It is filled on the client, so it follows the
  client's own config, and only when the tab is built again, on the next join.

How it works: the "Mod Items" sections of [src/datagen/java/AGENTS.md](../../src/datagen/java/AGENTS.md)
and [src/client/java/com/thirstwastaken2/client/AGENTS.md](../../src/client/java/com/thirstwastaken2/client/AGENTS.md),
and the `registerResourceConditions` rows in
[platform/AGENTS.md](../../src/main/java/com/thirstwastaken2/platform/AGENTS.md). Player docs:
`docs/docs/configuration.md`, "Mod items".

**The gametest does not reload data.** A reload in the middle of the run would reload it under every
other test. `ModItemsGameTest` reads the recipe files the server loaded and decodes their conditions
with the loader's own codec under a changed config instead (`gametest/platform/LoadConditions`, one per
loader). That covers registration, id, field, the NeoForge translation and the config read; the
skipping itself is the loader's. `CreativeTabGameTest.tabLeavesOutASwitchedOffItem` covers the tab.

Checked: `runDatagen` on all five Fabric nodes (47 files each); `runGametest` on `26.3.x`, `1.21.1` and
`1.21.1-neoforge`, all passing, the four new tests included; `checkLang`, `checkVersionSeam`,
`checkLoaderSeam`, `checkOptionalSeam`, `checkApiSurface`; `checkNeoForgeResources` on `1.21.1-neoforge`,
`1.21.11-neoforge` and `26.3.x-neoforge`; `build` on `26.3.x-neoforge`; the rest compiled.

**Still to do:**

- `checkDatagen` compares with git, so it only passes once the regenerated `src/main/generated/` is
  committed.
- By hand: switch the waterskin off on the Mod Items page, `/reload`, and check its recipe is gone from
  the recipe book and the crafting grid; rejoin and check the creative tab.
- Recipe viewers are left for later. Without a recipe, JEI and EMI still list the item. To hide it
  they would need an integration of their own, or the `c:hidden_from_recipe_viewers` tag, which is data
  and cannot read the config, so that route needs a generated data pack.

## Step 3: numbers and single mechanics

Constants a pack may want to change. Each one becomes a config field clamped in `sanitize()`, with a
widget on the Water page or a new page:

| Now | Where |
|---|---|
| Quenched share by grade, `{0, 50, 100, 100}` | `WaterPurity.QUENCHED_PERCENT` |
| Parched and Nausea length after sea water | `WaterPurity.PARCHED_TICKS`, `SALT_NAUSEA_TICKS` |
| Grade of rain and dripstone water | `WaterPurity.RAINWATER_PURITY`, `DRIPSTONE_PURITY` |
| Canteen and flask capacity, boil time | `ThirstItems` (`4`, `WaterskinItem.MAX_CAPACITY`, `*_BOIL_TICKS`) |

Switches for single mechanics: sea water, rain collection, boiling in hand, hanging pots. These are for
packs where another mod already covers that mechanic.

Before building, check each constant for three things:

- Does it reach a cached value? `WaterPurity.INFO` may not hold config values.
- Does it reach a recipe? The flask's furnace recipes are one per fill level up to `MAX_CAPACITY`, so a
  bigger flask needs more generated recipes and cannot be a plain setting.
- Does it reach a sync? Capacity is stored on the stack.

Write down which constants stay fixed and why.
