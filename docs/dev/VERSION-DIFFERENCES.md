# Version differences

Everything that differs between the supported Minecraft versions, in one place: first what a player
can see, then what differs underneath and where the code handles it. Unlike the plan files beside it,
this page is a standing reference. It stays for as long as more than one version is supported.

Supported nodes and their jars:

| Node | Jar suffix | Runs on | Java | Loader API |
|---|---|---|---|---|
| `26.3.x` | `+26.3` | 26.3 | 25 | Fabric API 0.161.0+26.3 |
| `26.2.x` | `+26.2` | 26.2 | 25 | Fabric API 0.161.0+26.2 |
| `26.1.x` | `+26.1.2` | 26.1, 26.1.1, 26.1.2 | 25 | Fabric API 0.155.3+26.1.2 |
| `1.21.11` | `+1.21.11` | 1.21.11 | 21 | Fabric API 0.141.6+1.21.11 |
| `1.21.1` | `+1.21.1` | 1.21, 1.21.1 | 21 | Fabric API 0.116.17+1.21.1 |
| `26.3.x-neoforge` | `+26.3-neoforge` | 26.3 | 25 | NeoForge 26.3.0.7-beta |
| `26.2.x-neoforge` | `+26.2-neoforge` | 26.2 | 25 | NeoForge 26.2.0.88 |
| `26.1.x-neoforge` | `+26.1.2-neoforge` | 26.1, 26.1.1, 26.1.2 | 25 | NeoForge 26.1.2.109 |
| `1.21.11-neoforge` | `+1.21.11-neoforge` | 1.21.11 | 21 | NeoForge 21.11.45 |
| `1.21.1-neoforge` | `+1.21.1-neoforge` | 1.21.1 | 21 | NeoForge 21.1.251 |

The NeoForge jars are built and tested on every node. A NeoForge node builds the same Minecraft
version as the Fabric node it sits under, so everything on this page applies to both. 1.21 is the
one exception: the Fabric 1.21.1 jar claims it, and NeoForge 21.0 is a generation of its own.

NeoForge has published only betas for 26.3, so `26.3.x-neoforge` is pinned to one and asks players for
at least that build. Two of its optional integrations have no 26.3 release yet either: Cloth Config,
which only AppleSkin's own settings screen needs in runClient, and Sophisticated Core, so that node
alone among the NeoForge ones builds without the Sophisticated upgrades.

**This page is the version axis only.** What differs between Fabric and NeoForge on the *same*
Minecraft version, and which seam hides it, is in
[src/main/java/com/thirstwastaken2/platform/AGENTS.md](../../src/main/java/com/thirstwastaken2/platform/AGENTS.md).
Where an older NeoForge differs from a newer one, that is a version difference and it is on this page,
under the release that changed it.

Checked against the code on 2026-09-20: 126 `//? if` blocks, 9 replacement rules with 27 replacements
in `stonecutter.gradle.kts`, and the version branches in `build.gradle.kts` and
`build.neoforge.gradle.kts`.

## What a player can see

Gameplay is the same on every version: the same thirst rules, water grades, recipes, loot, commands
and config. The gametests hold every node to that. Only these differ:

| | 26.3 | 26.2 | 26.1.x | 1.21.11 | 1.21.1 |
|---|---|---|---|---|---|
| Sea water in a bottle or bucket has its own sprite | yes | yes | yes | yes | **no**, it looks like ordinary water |
| Droplets in item tooltips | no shadow | no shadow | no shadow | no shadow | **with a shadow** |
| Config screen section headings | vanilla heading | vanilla heading | vanilla heading | vanilla heading | **a centred text row** |
| The Sand Filter (Create Fly, Fabric only) | **no** | yes | yes | **no** | **no** |

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

### Why the Sand Filter is missing

Not a Minecraft difference: Create Fly has no release for 26.3, 1.21.11 or 1.21.1, and it is a Fabric port,
so no NeoForge node has it either. A node compiles `src/main/createfly` only when it sets
`deps.create_fly`; see [src/main/createfly/AGENTS.md](../../src/main/createfly/AGENTS.md). Farmer's
Delight is absent from the NeoForge nodes for the same kind of reason, but nothing a player sees
depends on it being there at build time: its recipes load or are skipped by their conditions.

## What differs underneath

Grouped by the release that introduced the newer form, newest first. A version is affected by every
section above its own line. The code column is where the difference is handled; callers elsewhere
never see it.

"Replacement" means a pure rename in `stonecutter.gradle.kts`, with no branch in any file.

### 26.3 (affects 26.2, 26.1.x, 1.21.11, 1.21.1)

| Difference | Code |
|---|---|
| Blocks lost their codec, and with it `simpleCodec` and the `codec()` override a block owed vanilla | `SupportedBlock`, which carried it for `HangingPotBlock` |
| Every `PushReaction` constant was renamed; `DESTROY` is `POPPED` | replacement |
| `LootPoolSingletonContainer` split into three classes, of which the entry builders are typed on `UniformContainerBase` | replacement |
| Loot number providers split into an int and a float family, each behind a `Holder`, so a pool's rolls and a count are built differently | `Vanilla.lootPool`, `Vanilla.setCount` |
| `Inventory#placeItemBackInInventory` asks whether the client predicted the call | `Vanilla.placeItemBackInInventory` |
| Recipes became a registry: a recipe provider bootstraps them alongside their unlock advancements, and the criteria that name a recipe name a holder rather than a key | `ThirstRecipeProvider`, and `RecipeKeys` for the two providers outside that registry set |
| The advancement builder's `display` split in two, and only `rootDisplay` still takes the tab background | `ThirstAdvancementProvider`, at both call sites. Not a replacement: `display` is still the name a child calls on 26.3, so a rule rewriting it would be reversed onto those too |
| A saved block state is written under `id` and `properties` rather than `Name` and `Properties` | `CauldronGameTest` |
| The renderer's pipeline type moved from Blaze3D into Renderpearl | replacement |
| Opening a path in the file manager moved off `Util.OS` onto `Blaze3D` | `ClientVanilla.openPath` |
| The game moved from GLFW to SDL, so the window handle is an SDL one | the agent client's `ClientWindow` |
| A gametest's `TestData` names the dimension it runs in | NeoForge `ThirstWasTaken2GameTests` |

Result: 26.3 writes its recipe unlocks with a `recipes` key holding the recipe id, where earlier
versions write `recipe`. Nothing else in the generated files moved.

### 26.2 (affects 26.1.x, 1.21.11, 1.21.1)

| Difference | Code |
|---|---|
| Whether the HUD is hidden (F1) moved from the options into the HUD object | `ClientVanilla.isHudHidden`; the agent client's `AgentClientVanilla.toggleHud` flips the same state |
| Opening a screen moved from the client onto the GUI | `ClientVanilla.setScreen`, `AgentClientVanilla.screen` |
| Options lists gained a full-width widget row | `ClientVanilla.addFullWidthRow` |
| The right-hand status bar stack heights moved from `Gui` to `Hud` | NeoForge `ClientLoader.addRightStatusBar` |
| The main render target moved from the client onto its game renderer | `AgentClientVanilla.screenshot` |
| Entity type constants moved from `EntityType` to `EntityTypes` | `TestFixtures.mountType`, `piglinType` |
| Advancement trigger classes moved into `triggers` | replacement |

### 26.1 (affects 1.21.11, 1.21.1)

| Difference | Code |
|---|---|
| The HUD draw target was renamed `GuiGraphicsExtractor` | replacement |
| A block's render layer follows its textures, so a cut-out model needs no registration | `ClientLoader.renderCutout`; before it Fabric registers the layer and NeoForge reads `render_type` from the model |
| Blockstate definitions became `BlockStateModelDispatcher` | `HangingPotModels` |
| A widget draws in `extractWidgetRenderState` rather than `renderWidget`, and `drawString` became `text` | `ClientVanilla.canvas`, `ClientVanilla.text` |
| Fabric's creative tab builder was renamed `FabricCreativeModeTab` | `Loader.creativeTabBuilder` |
| Fabric's data generation output and tag provider were renamed | replacement |
| Recipe results became `ItemStackTemplate`, cooking recipes gained new constructors, and building a result no longer takes the registries | `ThirstRecipeProvider`, `FarmersDelightRecipeProvider`, `TestFixtures.assemble` |
| Model texture mappings take a `Material` | `ThirstModelProvider` |
| NeoForge stopped throwing when an attachment syncs to a connection that never negotiated the channel, and answers for a fake player's channelless connection rather than throwing | NeoForge `Loader.syncsTo` |
| Gametests gained padding between them, and `TestEnvironmentDefinition` a type parameter | NeoForge `ThirstWasTaken2GameTests` |

Result: 1.21.11 writes shorter recipe files, because a live `ItemStack` omits components the item
already has by default. The stack the furnace hands out is the same; see
[src/datagen/java/AGENTS.md](../../src/datagen/java/AGENTS.md).

### 1.21.11 (affects 1.21.1)

| Difference | Code |
|---|---|
| `ResourceLocation` became `Identifier`, `ResourceKey#location` became `#identifier`, `Util` moved package | replacement |
| The advancement criterion package lost its `critereon` spelling | replacement, chosen inside the 26.2 rule because replacements do not chain |
| Command permission levels became permission sets | `Vanilla.isGameMaster`, `Vanilla.isOwner` |
| The window handle accessor was renamed from `getWindow` to `handle` | `AgentClientVanilla.windowHandle` (written `>1.21.1`) |
| A connection's send listener became Netty's own | NeoForge `CapturingConnection` (written `>1.21.1`) |

### 1.21.9 (affects 1.21.1)

| Difference | Code |
|---|---|
| Fonts are named through `FontDescription` | `Vanilla.dropletFont` |
| The chain became the iron chain, item and texture | `ThirstRecipeProvider`, `HangingPotModels` |
| "Water evaporates here" moved from the dimension type to environment attributes | `Vanilla.waterEvaporates` |
| FML turned its environment fields into methods, and hands a mod's manifest out as a stream rather than a path | NeoForge `Loader.isDevelopmentEnvironment`, `ThirstWasTaken2GameTests.openManifest` |

### 1.21.6 (affects 1.21.1)

| Difference | Code |
|---|---|
| Fabric API gained the HUD element and status bar height registries | `ClientLoader.addRightStatusBar`; on 1.21.1 `GuiMixin` draws the bar after the food bar and moves the air bubbles up |
| Fabric's block render layer map moved into its rendering module and takes a chunk section layer | Fabric `ClientLoader.renderCutout` |
| Saving and loading an entity take a `ValueOutput` / `ValueInput` rather than a `CompoundTag` | `TestFixtures.savePlayer`, `loadPlayer` |

### 1.21.5 (affects 1.21.1)

| Difference | Code |
|---|---|
| Item tooltips gained `addDetailsToTooltip` | `ItemStackMixin`; on 1.21.1 it wraps the hover-text call in `getTooltipLines` |
| The Confusion effect was renamed Nausea, `Entity#moveTo` became `snapTo` | replacement |
| Fabric API gained its own `@GameTest` annotation | replacement; on 1.21.1 tests use vanilla's with Fabric's empty structure |
| `GameTestHelper#assertTrue` takes a `Component` | `TestFixtures.check` |
| Blockstate generators hand over a parsed definition rather than JSON | `HangingPotModels` |
| Tests register through the test function registry, and the server writes its own JUnit report with `--report` | NeoForge `ThirstWasTaken2GameTests` and `build.neoforge.gradle.kts`; before it the harness registers and reports itself |
| A `CompoundTag`'s getters answer with an `Optional` or a fallback | `DrinkingUpgradeContainer.handlePacket` (Sophisticated, NeoForge only) |

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
| Items and blocks are given their id before construction, and a block item names itself after the block only when asked | `Vanilla.registerItem`, `registerBlock`, `registerBlockItem`; `DrinkingUpgradeItem` (Sophisticated) |
| `Block#updateShape` reordered its parameters and schedules ticks through its own argument | `SupportedBlock` |
| Drinking became the consumable component | `DrinkItem`; on 1.21.1 it overrides use, animation, duration and finishing |
| The `item_model` component | `Vanilla.swapItemModel` (visible, see above); `ItemAppearanceGameTest` only asserts the swap where the component exists |
| `Item#use` returns a result without the resulting stack; `CONSUME` became `SUCCESS_SERVER` | `ItemStackMixin`, `Loader.onUseItem`; the constant is a replacement |
| Server-side damage became `hurtServer` | `Vanilla.hurt` |
| `FoodData#tick` takes a `ServerPlayer` | `FoodDataMixin` |
| `Registry#get` became `getValue` | replacement |
| `Registry#get(id)` returns a holder, where `getHolder` did | `Vanilla.mobEffect` |
| Every `Ingredient` became non-empty, so the codec lost its `CODEC_NONEMPTY` twin | `FarmersDelightRecipeProvider` |
| A components ingredient's `base` became a holder set rather than a whole ingredient | `build.neoforge.gradle.kts`, translating Fabric's JSON as it copies it |
| GUI draw calls take a render pipeline | the dev-only `GuiDrawMixin`, which records vanilla's food and air sprite rectangles |
| A potion's crafting remainder is a glass bottle, so the Cooking Pot serves boiled water into one | `FarmersDelightRecipeProvider` names no container either way (visible in game only) |
| Recipes are registry entries with keys, built by a separate recipe provider | `ThirstRecipeProvider`, `ThirstAdvancementProvider`, `AdvancementGameTest` |
| The shapeless recipe builder can give its result components | `ThirstRecipeProvider`; on 1.21.1 the filled-bowl recipe is written out by hand |
| Use animations became `ItemUseAnimation` | `DrinkingUpgradeWrapper.canFilter`, `AlchemyUpgradeWrapperMixin` (Sophisticated, NeoForge only) |
| A recipe names an ingredient by id or `#tag` rather than as an object | the Drinking upgrade's recipes, one copy per generation in `src/main/sophisticated-fluidhandler` and `-transfer` |

### Somewhere between 1.21.1 and 1.21.11

These are written `>1.21.1` because the exact release was not pinned down. With no node in between,
it makes no difference to any jar.

| Difference | Code |
|---|---|
| A block's description id is known inside its constructor (on 1.21.1 asking caches a wrong name) | `Vanilla.isWaterCauldron`; on 1.21.1 `BlocksMixin` marks the water cauldron's construction |
| GUI blits take a render pipeline and a tint | `ClientVanilla.blit`, `ClientVanilla.blitSprite`; on 1.21.1 both are render state, set before the draw and reset after it |
| Options lists gained section headings | `ClientVanilla.addHeader` (visible, see above) |
| `ServerPlayer#level` returns a `ServerLevel` | `Vanilla.level` |
| The drinking sound became a registry holder | `Vanilla.drinkSound` |
| Bucket pickup takes any living entity | `BucketItemMixin` |
| The food check a sprint asks moved from the client's `LocalPlayer` onto `Player` | `PlayerMixin`; on 1.21.1 `LocalPlayerMixin` hooks the client's own copy, and `TestFixtures.canSprint` answers from vanilla's rule there, because a server test cannot reach it |
| A NeoForge attachment saves through a map codec, so the value goes under a field | NeoForge `Loader.playerData`; a world carried from one to the other starts at full thirst |
| Advancement backgrounds are named by texture id | `ThirstAdvancementProvider` |
| Fabric's tag builder was renamed `builder` | `ThirstBiomeTagProvider`, `ThirstDamageTypeTagProvider` |
| NeoForge reads a custom ingredient's type from `neoforge:ingredient_type` rather than vanilla's `type` | `build.neoforge.gradle.kts` |
| NeoForge's fluid API became the transfer API (`ResourceHandler`, `ItemAccess`, transactions), and Sophisticated Core followed it | `src/main/neoforge-fluidhandler` / `-transfer` and `src/main/sophisticated-fluidhandler` / `-transfer`, chosen in `build.neoforge.gradle.kts` (written `>=1.21.2`) |
| NeoForge's `item_exists` recipe condition became `registered` | the Drinking upgrade's recipes, per generation as above |
| FML hands a mod file's contents out through `getContents`, where `findResource` gave a path | `SophisticatedPresence` (written `>=1.21.2`) |
| `Entity#startRiding` gained a second flag | `PlayerStateGameTest` |
| Levels expose their highest buildable y | `BenchmarkWorld` |

## Differences in Fabric API rather than Minecraft

- **Attachment sync** exists on every version, including 1.21.1, where Fabric API backported it.
  `Loader.playerData` is the same on all four Fabric nodes.
- **Loot tables from vanilla's experiment packs** are reported as built in by Fabric API on 1.21.1 and
  as a data pack's on later versions. The mod adds its loot to every table regardless of source, so
  this has no effect; it is recorded because it is why the mod stopped filtering by source.

## Keeping this page true

- A new `//? if` block or replacement gets a row under the release that introduced the newer form. If
  a player can see it, it also gets a row in the first table and a line on the
  [installation page](../docs/installation.md).
- A difference between the loaders is not a version difference: it belongs in
  [platform/AGENTS.md](../../src/main/java/com/thirstwastaken2/platform/AGENTS.md). A `//? if` inside
  `src/main/neoforge`, `src/client/neoforge` or `src/gametest/neoforge` is both, and belongs here too.
- Retiring a node deletes the sections that only affect it. For 1.21.1 that is every section from
  1.21.9 down, plus the unpinned one.
- `grep -rn "//? if" src --include=*.java`, `stonecutter.gradle.kts` and the `sc.current.parsed`
  branches in both buildscripts are the source of truth; this page is their index.
