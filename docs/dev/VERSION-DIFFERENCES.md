# Version differences

Everything that differs between the supported Minecraft versions, in one place: first what a player
can see, then what differs underneath and where the code handles it. Unlike the plan files beside it,
this page is a standing reference. It stays for as long as more than one version is supported.

Supported versions and their jars:

| Node | Jar suffix | Runs on | Java | Fabric API |
|---|---|---|---|---|
| `26.2.x` | `+26.2` | 26.2 | 25 | 0.160.0+26.2 |
| `26.1.x` | `+26.1.2` | 26.1, 26.1.1, 26.1.2 | 25 | 0.155.3+26.1.2 |
| `1.21.11` | `+1.21.11` | 1.21.11 | 21 | 0.141.6+1.21.11 |
| `1.21.1` | `+1.21.1` | 1.21, 1.21.1 | 21 | 0.116.17+1.21.1 |

Checked against the code on 2026-09-13: 58 `//? if` blocks and the replacements in
`stonecutter.gradle.kts`.

## What a player can see

Gameplay is the same on every version: the same thirst rules, water grades, recipes, loot, commands
and config. The gametests hold all four to that. Only these differ:

| | 26.2 | 26.1.x | 1.21.11 | 1.21.1 |
|---|---|---|---|---|
| Sea water in a bottle or bucket has its own sprite | yes | yes | yes | **no**, it looks like ordinary water |
| Droplets in item tooltips | no shadow | no shadow | no shadow | **with a shadow** |
| Config screen section headings | vanilla heading | vanilla heading | vanilla heading | **a centred text row** |

Everything else a player sees, the thirst bar, the bowl and waterskin sprites, the Salty tooltip line,
the drinking animation and sound, is the same. On some versions it is produced differently, which is
the rest of this page.

### Why 1.21.1 looks different

- **Sea-water bottles and buckets.** Later versions swap their sprite through the `minecraft:item_model`
  component, which only exists from 1.21.2. On 1.21.1 a vanilla item has one model for every stack,
  and changing that would mean overriding vanilla's `potion` and `water_bucket` models, which breaks
  resource packs and other mods. The bowl is unaffected because it is the mod's own item, with its own
  model and custom model data overrides.
- **The droplet shadow.** Later versions turn it off with `Style#withoutShadow`, which only exists
  from 1.21.4. On 1.21.1 the tooltip renderer decides, for all text at once.
- **Headings.** `OptionsList#addHeader` does not exist on 1.21.1.

One of these could still be closed: a sea-water **bottle** can be tinted through the potion's custom
colour, since vanilla's potion model already tints by it. The cost is that sea-water bottles then carry
different potion contents from ordinary ones, which other mods comparing contents would notice. The
bucket has no tint layer, and the shadow has no per-text switch, so those two stay.

## What differs underneath

Grouped by the release that introduced the newer form, newest first. A version is affected by every
section above its own line. The code column is where the difference is handled; callers elsewhere
never see it.

"Replacement" means a pure rename in `stonecutter.gradle.kts`, with no branch in any file.

### 26.2 (affects 26.1.x, 1.21.11, 1.21.1)

| Difference | Code |
|---|---|
| Whether the HUD is hidden (F1) moved from the options into the HUD object | `ClientVanilla.isHudHidden` |
| Options lists gained a full-width widget row | `ClientVanilla.addFullWidthRow` |
| Entity type constants moved from `EntityType` to `EntityTypes` | `TestFixtures.mountType`, `piglinType` |
| Advancement trigger classes moved into `triggers` | replacement |

### 26.1 (affects 1.21.11, 1.21.1)

| Difference | Code |
|---|---|
| The HUD draw target was renamed `GuiGraphicsExtractor` | replacement |
| Fabric's creative tab builder was renamed `FabricCreativeModeTab` | `Loader.creativeTabBuilder` |
| Fabric's data generation output and tag provider were renamed | replacement |
| Recipe results became `ItemStackTemplate`, cooking recipes gained new constructors, and building a result no longer takes the registries | `ThirstRecipeProvider`, `TestFixtures.assemble` |
| Model texture mappings take a `Material` | `ThirstModelProvider` |

Result: 1.21.11 writes shorter recipe files, because a live `ItemStack` omits components the item
already has by default. The stack the furnace hands out is the same; see
[src/datagen/java/AGENTS.md](../../src/datagen/java/AGENTS.md).

### 1.21.11 (affects 1.21.1)

| Difference | Code |
|---|---|
| `ResourceLocation` became `Identifier`, `ResourceKey#location` became `#identifier`, `Util` moved package | replacement |
| The advancement criterion package lost its `critereon` spelling | replacement, chosen inside the 26.2 rule because replacements do not chain |
| Command permission levels became permission sets | `Vanilla.isGameMaster`, `Vanilla.isOwner` |

### 1.21.9 (affects 1.21.1)

| Difference | Code |
|---|---|
| Fonts are named through `FontDescription` | `Vanilla.dropletFont` |
| "Water evaporates here" moved from the dimension type to environment attributes | `Vanilla.waterEvaporates` |

### 1.21.6 (affects 1.21.1)

| Difference | Code |
|---|---|
| Fabric API gained the HUD element and status bar height registries | `ClientLoader.addRightStatusBar`; on 1.21.1 `GuiMixin` draws the bar after the food bar and moves the air bubbles up |

### 1.21.5 (affects 1.21.1)

| Difference | Code |
|---|---|
| Item tooltips gained `addDetailsToTooltip` | `ItemStackMixin`; on 1.21.1 it wraps the hover-text call in `getTooltipLines` |
| The Confusion effect was renamed Nausea, `Entity#moveTo` became `snapTo` | replacement |
| Fabric API gained its own `@GameTest` annotation | replacement; on 1.21.1 tests use vanilla's with Fabric's empty structure |
| `GameTestHelper#assertTrue` takes a `Component` | `TestFixtures.check` |

### 1.21.4 (affects 1.21.1)

| Difference | Code |
|---|---|
| Custom model data became float lists | `Vanilla.modelSelector` |
| Item model definitions (`assets/…/items/`) replaced model overrides | `ThirstModelProvider`, `ThirstItemModelDefinitionProvider` (not built on 1.21.1) |
| Styles can turn off the text shadow | `Vanilla.dropletFont` (visible, see above) |
| The data generator's model classes, and Fabric's model provider, moved to client packages | replacement |

### 1.21.2 (affects 1.21.1)

| Difference | Code |
|---|---|
| Items are given their id before construction | `Vanilla.registerItem` |
| Drinking became the consumable component | `DrinkItem`; on 1.21.1 it overrides use, animation, duration and finishing |
| The `item_model` component | `Vanilla.swapItemModel` (visible, see above) |
| `Item#use` returns a result without the resulting stack; `CONSUME` became `SUCCESS_SERVER` | `ItemStackMixin`, `Loader.onUseItem`; the constant is a replacement |
| Server-side damage became `hurtServer` | `Vanilla.hurt` |
| `FoodData#tick` takes a `ServerPlayer` | `FoodDataMixin` |
| `Registry#get` became `getValue` | replacement |
| Recipes are registry entries with keys, built by a separate recipe provider | `ThirstRecipeProvider`, `ThirstAdvancementProvider`, `AdvancementGameTest` |
| The shapeless recipe builder can give its result components | `ThirstRecipeProvider`; on 1.21.1 the filled-bowl recipe is written out by hand |

### Somewhere between 1.21.1 and 1.21.11

These are written `>1.21.1` because the exact release was not pinned down. With no node in between,
it makes no difference to any jar.

| Difference | Code |
|---|---|
| A block's description id is known inside its constructor (on 1.21.1 asking caches a wrong name) | `Vanilla.isWaterCauldron`; on 1.21.1 `BlocksMixin` marks the water cauldron's construction |
| GUI blits take a render pipeline and a tint | `ClientVanilla.blit`; on 1.21.1 the tint is render state |
| Options lists gained section headings | `ClientVanilla.addHeader` (visible, see above) |
| `ServerPlayer#level` returns a `ServerLevel` | `Vanilla.level` |
| The drinking sound became a registry holder | `Vanilla.drinkSound` |
| Bucket pickup takes any living entity | `BucketItemMixin` |
| Advancement backgrounds are named by texture id | `ThirstAdvancementProvider` |
| Fabric's tag builder was renamed `builder` | `ThirstBiomeTagProvider`, `ThirstDamageTypeTagProvider` |
| `Entity#startRiding` gained a second flag | `PlayerStateGameTest` |
| Levels expose their highest buildable y | `BenchmarkWorld` |

## Differences in Fabric API rather than Minecraft

- **Attachment sync** exists on every version, including 1.21.1, where Fabric API backported it.
  `Loader.playerData` is the same on all four.
- **Loot tables from vanilla's experiment packs** are reported as built in by Fabric API on 1.21.1 and
  as a data pack's on later versions. The mod adds its loot to every table regardless of source, so
  this has no effect; it is recorded because it is why the mod stopped filtering by source.

## Keeping this page true

- A new `//? if` block or replacement gets a row under the release that introduced the newer form. If
  a player can see it, it also gets a row in the first table and a line on the
  [installation page](../docs/installation.md), both languages.
- Retiring a node deletes the sections that only affect it. For 1.21.1 that is every section from
  1.21.9 down, plus the unpinned one.
- `grep -rn "//? if" src --include=*.java` and `stonecutter.gradle.kts` are the source of truth; this
  page is their index.
