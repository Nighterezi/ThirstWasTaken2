# platform/

The two places where the mod is allowed to know what it is running on: `Vanilla` for the Minecraft
version, `Loader` for the mod loader. Everything outside this package and its client twin,
`com.thirstwastaken2.client.platform`, compiles unchanged on every node.

```
core                          no version knowledge, no loader knowledge
Vanilla, ClientVanilla        version axis    Stonecutter comments, in src/main/java and src/client/java
Loader, ClientLoader          loader axis     one copy per loader, in src/main/<loader> and src/client/<loader>
```

**The two axes never meet in one file.** A file is either version conditional and lives in
`src/main/java`, or loader specific and lives in `src/main/fabric`. A loader file may contain version
conditionals (`Loader.creativeTabBuilder` does). A `src/main/java` file may never name a loader.

## Vanilla

The mod is built for several Minecraft versions from one source tree (see the root `AGENTS.md`), so
a handful of vanilla calls have to be written twice. `Vanilla` collects them behind signatures that
are identical on every version.

- **Only vanilla-facing plumbing.** No thirst logic, no config reads, no caching. If a method here
  needs to know what the mod is doing, it is in the wrong package.
- **Same signature on every version.** A caller must never need to know which branch is live.
- **Add to `Vanilla` rather than to the caller.** A `//?` block anywhere outside this package and
  `mixin/` is a signal the seam is missing, and in `src/main/java` or `src/client/java`
  `checkVersionSeam` fails the build on it. That check is the exit ramp in
  `docs/dev/PLATFORM-PLAN.md`; the number of blocks in here is not.
- Mixins are the documented exception: their `@Inject` signatures track the target method and cannot
  be abstracted away. Keep their bodies one line regardless.

### What 1.21.1 costs

1.21.1 is the one old version, and most of the conditionals here exist for it. What it lacks, and
what stands in, for this package. The full list for every version, including datagen, mixins and
tests, is [docs/dev/VERSION-DIFFERENCES.md](../../../../../../docs/dev/VERSION-DIFFERENCES.md); a new
seam gets a row there too.

| Missing on 1.21.1 | Seam | What 1.21.1 does instead |
|---|---|---|
| items knowing their id before construction | `registerItem` | registers the properties as they are |
| the consumable component | `DrinkItem` | overrides use, animation, duration and finishing itself |
| custom model data as float lists | `modelSelector(index, value)` | one integer; no item reads more than one index |
| the `item_model` component | `swapItemModel` | nothing: a sea-water bottle or bucket keeps vanilla's sprite, and only its tooltip says salty |
| styles without a shadow | `dropletFont` | the tooltip droplets are drawn with a shadow |
| a block's id inside its constructor | `isWaterCauldron` | `BlocksMixin` marks the water cauldron's construction |
| `hurtServer`, `level()` as `ServerLevel`, permission sets, environment attributes | `hurt`, `level`, `isGameMaster`, `isOwner`, `waterEvaporates` | the older call, same meaning |
| `Registry#get(id)` returning a holder | `mobEffect` | `getHolder(id)`, same meaning |

Pure renames (`ResourceLocation`, `CONSUME`, `moveTo`, `CONFUSION` and the rest) are replacements in
`stonecutter.gradle.kts`, not branches. A threshold written `>1.21.1` rather than a release number
means the exact release a call changed in was not pinned down; with no node between 1.21.1 and
1.21.11 it makes no difference to any jar.

`DrinkItem` is a class rather than a method because an item's use and animation are overrides. Keep it
the one place that knows how drinking starts and finishes: `WaterskinItem` extends it and only says
whether it has anything to drink (`canDrink`) and what a drink removes.

## Loader

`Loader` does not live in this directory. Each loader has its own copy at
`src/main/<loader>/java/com/thirstwastaken2/platform/Loader.java`, with the same class name and the
same public signatures, and each node's buildscript (`build.gradle.kts` for Fabric,
`build.neoforge.gradle.kts` for NeoForge) compiles exactly one of them. There is no interface
and no service lookup: a static call to a class that exists once per jar is the cheapest seam there
is, and the compiler checks every call site.

What does live here are the types those signatures need, because both copies have to share them:
`PlayerData`, `UseBlockHandler`, `UseItemHandler`, and on the client `StatusBarRenderer`.

| `Loader` | What it hides |
|---|---|
| `isDevelopmentEnvironment`, `configDir`, `isModLoaded` | the loader's own environment |
| `onRegister` | when a registry accepts entries: at once on Fabric, from the registration event on a loader that freezes registries early |
| `playerData` | the attachment system that saves a value on a player and syncs it to its owner |
| `creativeTabBuilder` | a tab builder that places itself in the tab list |
| `onServerTickEnd`, `onUseBlock`, `onUseItem`, `onRegisterCommands`, `onTagsLoaded` | the event bus |
| `onLootTable` | loot table modification, on every table whoever wrote it |
| `ClientLoader.addRightStatusBar` | HUD layer registration and the right-hand status bar height |
| `ClientLoader.appleSkinShowsExhaustionUnderlay` | AppleSkin's own setting, which it keeps in a different class shape on each loader |

### How each loader answers

| Seam | Fabric | NeoForge |
|---|---|---|
| `isDevelopmentEnvironment`, `configDir`, `isModLoaded` | `FabricLoader` | `FMLEnvironment.isProduction`, `FMLPaths.CONFIGDIR`, `ModList` |
| `onRegister` | runs at once | queued, run from `RegisterEvent` for that registry on the mod bus. NeoForge constructs mods while the built-in registries are frozen, so anything that registers, items included, has to wait for the event. It fires data component types before items on purpose. A registration for a registry whose event already fired throws |
| `playerData` | a data attachment, `AttachmentSyncPredicate.targetOnly()` | an `AttachmentType` with `serialize(codec.fieldOf("value"))` and `sync((holder, to) -> holder == to, ...)`, registered through `onRegister`. Saved under `neoforge:attachments` rather than `fabric:attachments`, so a world moved between loaders starts every player at full thirst |
| `creativeTabBuilder` | `FabricCreativeModeTab.builder()`, `FabricItemGroup` before 26.1 | `CreativeModeTab.builder()` |
| `onServerTickEnd` | `ServerTickEvents.END_SERVER_TICK` | `ServerTickEvent.Post` |
| `onUseBlock`, `onUseItem` | `UseBlockCallback`, `UseItemCallback`: a non-`PASS` result stops the chain | `PlayerInteractEvent.RightClickBlock`, `RightClickItem`: on a non-`PASS` result, `setCancellationResult` and `setCanceled(true)`, which also skips later listeners |
| `onTagsLoaded` | `CommonLifecycleEvents.TAGS_LOADED` | `TagsUpdatedEvent` |
| `onRegisterCommands` | `CommandRegistrationCallback` | `RegisterCommandsEvent` |
| `onLootTable` | `LootTableEvents.MODIFY` | `LootTableLoadEvent`, `getTable().addPool` |
| `ClientLoader.addRightStatusBar` | `HudElementRegistry.attachElementAfter(FOOD_BAR)` plus `HudStatusBarHeightRegistry.addRight`; `GuiMixin` on 1.21.1 | a layer `registerAbove(VanillaGuiLayers.FOOD_LEVEL)` that draws at `guiHeight() - hud.rightHeight` and advances `Hud.rightHeight` only when it drew, and only when the player can be hurt, which is when vanilla draws the food bar |
| `ClientLoader.appleSkinShowsExhaustionUnderlay` | `ModConfig.INSTANCE.showFoodExhaustionHudUnderlay` | `ModConfig.SPEC.isLoaded() && ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY.get()`; reading a NeoForge config value before FML loads it throws |

### What older NeoForge versions change

Every NeoForge node answers every seam the way the table says. Where an older NeoForge differs, the
difference is a version conditional inside the NeoForge loader directories:

| Seam | Differs on | What it does there |
|---|---|---|
| `isDevelopmentEnvironment` | 1.21.1 (NeoForge 21.1) | the static field `FMLEnvironment.production`; the method arrived with 21.9 |
| `playerData` saving | 1.21.1 | `serialize(codec)` rather than a map codec under `value`, so the saved shape differs from later versions |
| `playerData` syncing | before 26.1 (NeoForge 21.1 and 21.11) | the predicate, `Loader.syncsTo`, also asks `connection.hasChannel(SyncAttachmentsPayload.TYPE)`: those versions throw when they send the payload to a connection that never negotiated it, which is what a gametest mock player has, and a vanilla client too |
| `ClientLoader.addRightStatusBar` | before 26.2 | `Gui.rightHeight`; 26.2 moved it to `Hud` |

The resource translation in `build.neoforge.gradle.kts` also differs on 1.21.1: the ingredient type
goes under vanilla's `type` key, and the components ingredient's `base`, a whole ingredient there, is
turned into the `items` holder set. From 1.21.11 on it is `neoforge:ingredient_type` and the holder set.

### The NeoForge nodes' scope

`26.2.x-neoforge`, `26.1.x-neoforge`, `1.21.11-neoforge` and `1.21.1-neoforge` are the NeoForge nodes.
What they leave out, and why:

- **Create Fly.** A Fabric port; `src/main/createfly` never compiles here, because the node does not
  set `deps.create_fly`.
- **Farmer's Delight at runtime.** No NeoForge build for 26.2 when this was written, and the older
  nodes were kept the same. Its recipes still load or are skipped correctly, through the translated `neoforge:conditions`.
- **`src/dev` and `src/datagen`.** Fabric only; each node reads the files the Fabric node on its
  Minecraft version generates.
- **Minecraft 1.21.** The Fabric 1.21.1 jar covers it; NeoForge 21.0 is a generation of its own.
- **Carrying a world between loaders.** Fabric saves the thirst attachment under `fabric:attachments`,
  NeoForge under `neoforge:attachments`, so a moved world starts every player at full thirst. There is
  no migration, on purpose.

The jars are `ThirstWasTaken2-<version>+<minecraft>-neoforge.jar`. They are not released yet: every
Minecraft version now has a NeoForge node, and NeoForge ships, marked beta, once the manual passes in
[docs/dev/MANUAL-TESTING.md](../../../../../../docs/dev/MANUAL-TESTING.md) are done (P5 in
[docs/dev/PLATFORM-PLAN.md](../../../../../../docs/dev/PLATFORM-PLAN.md)).

Two mixins reach methods NeoForge patches: `ItemStack#addDetailsToTooltip`, where the mod's rows land
after NeoForge's own tooltip hook, and `CauldronBlock#receiveStalactiteDrip`, whose `RETURN` injection
also fires on NeoForge's early return for modded fluids. Both are harmless as written.

Rules:

- **Plumbing only, the same as `Vanilla`.** A method takes the mod's handler and hands it to the
  loader; it never decides anything. What the mod hooks, and in what order, stays in
  `ThirstWasTaken2.initialize`, which is loader independent.
- **Shape a signature after what the mod needs, not after the loader at hand.** Fabric's callbacks
  are one way to deliver a block interaction; NeoForge's cancellable events are another. The
  signature has to be implementable by both.
- **Every copy changes together.** Adding a method to one `Loader` means adding it to all of them;
  the node that lacks it is the one that fails to compile.
- **Entrypoints are loader code.** `ThirstWasTaken2Fabric`, `ThirstWasTaken2FabricClient` and the NeoForge
  `@Mod` classes `ThirstWasTaken2NeoForge` and `ThirstWasTaken2NeoForgeClient` do one thing: call
  `ThirstWasTaken2.initialize` and `ThirstWasTaken2Client.initialize`. The NeoForge client class also
  registers the config screen with the mods list, which Mod Menu's entrypoint does on Fabric. The
  NeoForge `Loader` finds the mod event bus itself, through `ModList`, so the mod class passes nothing
  in. So are the manifests (`fabric.mod.json`, `neoforge.mods.toml`) and anything written against a
  loader-only mod, such as `ModMenuIntegration`.
- **`checkLoaderSeam` fails on a loader import in `src/main/java` or `src/client/java`.** It reads
  imports, so it cannot see the methods Fabric API injects into vanilla classes
  (`getAttachedOrCreate`, `FabricItemStack` and friends). Those compile on Fabric and only fail on the
  next loader. Do not call them outside `src/main/fabric`.

`src/dev` and `src/datagen` are Fabric only, each its own small Fabric mod. They sit outside the seam
on purpose: neither ships, and datagen output is shared by every loader on a Minecraft version.
Datagen writes Fabric's spellings once, and the NeoForge node translates the three Fabric-only JSON
shapes as it copies resources (`build.neoforge.gradle.kts`), rather than datagen writing a second
copy; see [src/main/resources/AGENTS.md](../../../../resources/AGENTS.md).

`src/gametest` runs on both loaders. Its test classes are shared; the NeoForge node swaps one import
and adds a harness of its own in `src/gametest/neoforge`, which is test code rather than a seam. See
[src/gametest/java/AGENTS.md](../../../../../gametest/java/AGENTS.md).
