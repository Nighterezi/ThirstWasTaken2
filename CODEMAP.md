# Code map

How ThirstWasTaken2 (Fabric 26.2) is put together, and where to change what.

This page is the overview. Each area also carries its own `CLAUDE.md` with the rules and traps local
to it, sitting next to the code it describes:

| File | Covers |
|---|---|
| [src/main/java/com/thirstwastaken2/CLAUDE.md](src/main/java/com/thirstwastaken2/CLAUDE.md) | common code: init order, state invariants, caching rules |
| [.../mixin/CLAUDE.md](src/main/java/com/thirstwastaken2/mixin/CLAUDE.md) | mixin conventions, what is hooked, the fragile injections |
| [.../purity/CLAUDE.md](src/main/java/com/thirstwastaken2/purity/CLAUDE.md) | purity carriers, the cauldron offset, deferred interactions |
| [.../compat/CLAUDE.md](src/main/java/com/thirstwastaken2/compat/CLAUDE.md) | loot modification, the gated Create Fly integration |
| [src/client/java/com/thirstwastaken2/client/CLAUDE.md](src/client/java/com/thirstwastaken2/client/CLAUDE.md) | HUD sprite geometry, the config screen contract |
| [src/main/resources/CLAUDE.md](src/main/resources/CLAUDE.md) | manifests, recipes, tags, models, fonts, lang keys |
| [docs/CLAUDE.md](docs/CLAUDE.md) | the VitePress site and its writing style |

## Layout

```
src/main/java/com/thirstwastaken2/      common (client + server)
  ThirstWasTaken2.java                  ModInitializer: wiring and event registration
  api/ThirstApi.java                   item -> {hydration, quenched}, memoised per Item
  command/ThirstCommands.java          /thirst query|set|enable
  config/ThirstConfig.java             config/thirstwastaken2.json, compiled patterns, generation counter
  damage/ThirstDamageTypes.java        thirstwastaken2:dehydrate damage source
  data/ThirstData.java                 immutable player state + attachment type
  data/ThirstManager.java              tick loop, exhaustion maths, drink-by-hand
  item/ThirstItems.java                bowl and waterskin registration + creative tab
  item/WaterskinItem.java              three-drink storage, consumption and inventory transfers
  purity/ThirstComponents.java         quality, salinity, purity and serving data components
  purity/WaterQuality.java             contamination score, salinity and tier thresholds
  purity/WaterPurity.java              environmental sampling, effects and container detection
  purity/WaterInteractions.java        bowl/waterskin filling, cauldron purity transfer
  tooltip/ThirstTooltip.java           droplet row for the item tooltip (thirstwastaken2:droplets font)
  compat/LootIntegration.java          structure chests + Piglin barter water
  compat/createfly/                    optional Create Fly Sand Filter
  mixin/                               vanilla hooks

src/client/java/com/thirstwastaken2/client/
  ThirstWasTaken2Client.java            HUD element registration
  ThirstHud.java                       thirst bar rendering
  config/ThirstConfigScreen.java       vanilla-styled options screen
  compat/ModMenuIntegration.java       modmenu entrypoint

src/main/resources/
  fabric.mod.json                      entrypoints (main, client, modmenu)
  thirstwastaken2.mixins.json           mixin registry
  assets/thirstwastaken2/               textures, models, lang (9 locales)
  assets/thirstwastaken2/font/          droplets.json: tooltip droplet glyphs (U+E000..U+E007)
  data/thirstwastaken2/                 recipes, damage type, sand filter loot table
  data/minecraft/tags/                 bypasses_armor, mineable/pickaxe additions
```

## Runtime flow

```mermaid
flowchart TD
    Init[ThirstWasTaken2.onInitialize] --> Cfg[ThirstConfig.load]
    Init --> Attach[ThirstData.register]
    Init --> Events

    subgraph Events [registered events]
      Tick[END_SERVER_TICK -> ThirstManager.tick]
      Flush[END_SERVER_TICK -> WaterInteractions.tick]
      UseBlock[UseBlockCallback]
      UseItem[UseItemCallback -> fillBowl]
    end

    Tick --> TickPlayer[tickPlayer per ServerPlayer]
    TickPlayer --> Consume[consumeExhaustion: quenched then thirst]
    TickPlayer --> Slow[every 11 ticks: peaceful regen, rain]
    TickPlayer --> Dmg[every 40 ticks at 0 thirst: dehydrate damage]

    Exh[PlayerMixin.causeFoodExhaustion] --> AddExh[ThirstManager.addExhaustion]
    AddExh --> Mod[exhaustionModifier: climate x fire res x fire prot]

    Eat[ItemStackMixin.finishUsingItem] --> DrinkItem[ThirstManager.drinkItem]
    DrinkItem --> Api[ThirstApi.hydration]
    DrinkItem --> Effects[WaterPurity.applyEffects]

    Attach --> Sync[attachment sync to owning client]
    Sync --> Hud[ThirstHud.render]
```

## The player state model

`ThirstData` is a record — `thirst` (0-20), `quenched` (0-20), `exhaustion` (float), `enabled` — held
as a Fabric data attachment with `AttachmentSyncPredicate.targetOnly()`. Persistence uses `CODEC`;
the network uses `STREAM_CODEC`.

Every mutation returns a new record, so `ThirstManager.set` is the only write point and
`tickPlayer` only calls it when `!updated.equals(data)`. That keeps the sync to at most one packet
per player per tick.

Drain chain, mirroring vanilla hunger:

1. `Player.causeFoodExhaustion` → `ThirstManager.addExhaustion` (skipped while riding a mount).
2. Raw exhaustion is scaled by `exhaustionModifier`: climate (or the flat Nether value in dimensions
   where water evaporates), Fire Resistance, Fire Protection.
3. Once exhaustion passes 4, one point of `quenched` is spent; when quenched is empty, one point of
   `thirst` goes instead (unless Peaceful and depletion in Peaceful is off).
4. At 0 thirst, 1 damage every 40 ticks via `thirstwastaken2:dehydrate`.

## Water purity

Purity is an integer 0-3 (dirty, slightly dirty, acceptable, purified).

| Carrier | Storage |
|---|---|
| Items | contamination, purity and salinity data components |
| Cauldrons | offset `purity` and boolean `salty` blockstate properties |
| Create Fly fluids | the same quality components on `FluidStack` |
| Anything else | `ThirstConfig.defaultPurity` |

`WaterPurity.sampleAt(level, pos)` samples contamination and salinity only when water is collected or
drunk. Biome tags choose the baseline; temperature, altitude, flow and nearby mud or agriculture
apply small fixed modifiers. The result is stored on the container, so no environmental scan runs
on tick or tooltip paths.

`WaterPurity.INFO` caches, per `Item`, whether it counts as a water container and what static purity
it carries — this is how the optional Tough As Nails / Farmer's Delight / Farmer's Respite /
Brewin' and Chewin' / Collector's Reap support stays dependency-free.

## Mixins

| Mixin | Target | Purpose |
|---|---|---|
| `PlayerMixin` | `Player#causeFoodExhaustion`, `#canSprint` | mirror exhaustion, block sprinting at thirst <= 6 |
| `FoodDataMixin` | `FoodData#tick` (both `heal` call sites) | dehydration halts natural regen and refunds the food cost |
| `ItemStackMixin` | `#finishUsingItem`, `#addDetailsToTooltip` | grant hydration, render purity line + droplet row |
| `BottleItemMixin` | `BottleItem#use` | stamp purity on a bottle filled from a water block |
| `BucketItemMixin` | `BucketItem#use` | stamp purity on a bucket filled from a water block |
| `LayeredCauldronBlockMixin` | `#createBlockStateDefinition` | add purity and salinity properties |

## HUD

`ThirstWasTaken2Client` attaches `thirst_bar` after `VanillaHudElements.FOOD_BAR` and reserves 10px
of right-stack height. `ThirstHud.render` draws, in order:

1. ten droplet slots from `thirst_icons.png` (41x9: empty, quarter, half, three quarter and full on an
   8px stride, so u = 0/8/16/24/32), shaken when quenched hits zero, exactly like the vanilla hunger
   bar. Frames share their transparent edge columns, which is why the stride is 8 and not 9. Each
   droplet holds two thirst points; the quarter and three-quarter frames come from
   `drainedFraction`, which spends the client's `exhaustion` (0..4) against the next point once
   quenched is empty. There is no setting for this, the five-frame drain is the only behaviour;
3. the quenched outline from `appleskin_icons.png` at v=0, u = 0/9/18/27 by quarter.

## Config

`config/thirstwastaken2.json` is a Gson dump of `ThirstConfig`. `ThirstConfig.generation()` increments
on every load or commit; `ThirstApi` watches it to drop its per-item cache.

The Mod Menu screen (`ThirstConfigScreen`) writes straight into the live instance through
`OptionInstance` listeners and calls `ThirstConfig.commit()` on close. Only the HUD section is
client-side — the rest is server-authoritative and takes effect in singleplayer or when edited on the
server.

## Optional integrations

| Integration | Gate | Notes |
|---|---|---|
| Create Fly | `isModLoaded("create")` **and** `com.zurrtum.create...SmartBlockEntity` present | Sand Filter block, purifies pumped water by one step |
| Mod Menu | `modmenu` entrypoint | class only loads if Mod Menu resolves it |
| Loot | always | Fabric `LootTableEvents.MODIFY` on 5 vanilla chests + Piglin bartering |
| Food mods | always | resolved by registry id in `ThirstConfig.drinks` / `foods`, no classes referenced |
