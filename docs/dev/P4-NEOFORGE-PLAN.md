# P4: NeoForge on 26.2

How the mod gets its first NeoForge jar, on Minecraft 26.2 only, as the fifth build node. A working plan
with an end state: delete it once P4's gate in [PLATFORM-PLAN.md](PLATFORM-PLAN.md) is met, and fold
anything that outlives it into [platform/AGENTS.md](../../src/main/java/com/thirstwastaken2/platform/AGENTS.md)
and the root [AGENTS.md](../../AGENTS.md).

**Status: not started.** Estimate 8 to 15 days. The exit ramp is 20 days: past it, NeoForge stays on
26.2 and P5 is dropped.

## Scope

In:

- A `26.2.x-neoforge` node that builds a NeoForge jar from the same source tree and version number.
- A NeoForge copy of `Loader` and `ClientLoader`, and the item and component registration seam P2 left
  alone.
- The same gametests running on the NeoForge node, headlessly, in CI.
- The optional integrations that exist on NeoForge 26.2: AppleSkin, Jade, and the config screen in the
  mods list.

Out, each for a stated reason:

- **Create.** The Sand Filter is written against Create Fly, a Fabric port. `src/main/createfly` and
  `src/client/createfly` stay Fabric only; the NeoForge node never sets `deps.create_fly`.
- **Farmer's Delight at runtime.** No NeoForge build for 26.2 on Modrinth as of 2026-09-15. Its recipes
  still have to load correctly, or be skipped, with and without it (step 4), because the check costs
  nothing and the mod id is the same.
- **`src/dev` and `/thirst benchmark`.** Dev tooling, Fabric only, never shipped. `devClasses` is not
  built on the NeoForge node.
- **`src/datagen`.** Stays Fabric only, as settled in P2. The NeoForge node reads what the 26.2 Fabric
  node writes.
- **Publish automation.** P5.
- **Carrying a world between loaders.** Fabric saves the thirst attachment under `fabric:attachments`,
  NeoForge under `neoforge:attachments`. A world moved from one loader to the other starts every player
  at full thirst. Say so in the changelog; do not write a migration.

## Where things stand

Checked 2026-09-15. Re-check before relying on any of it.

| | Fact |
|---|---|
| NeoForge | `26.2.0.88` is the newest `26.2.0.x` on `maven.neoforged.net` |
| Build plugin | ModDevGradle `net.neoforged.moddev` `2.0.147` |
| Stonecutter | `version("<name>", "<mc>").buildscript = "build.neoforge.gradle.kts"` gives a node its own buildscript; [rotgruengelb/stonecutter-mod-template](https://github.com/rotgruengelb/stonecutter-mod-template) runs Loom and ModDevGradle nodes side by side in one build this way, on Stonecutter 0.9.7 |
| AppleSkin | `3.0.10+mc26.2` for NeoForge |
| Jade | `26.2.10+neoforge`. `JadeIntegration` already carries `@WailaPlugin`, which is how Jade finds plugins on NeoForge |
| Cloth Config | `26.2.155+neoforge` (AppleSkin's own config screen) |
| Farmer's Delight, Create | no NeoForge 26.2 build on Modrinth |
| Loader imports | none in `src/main/java` or `src/client/java`; `checkLoaderSeam` is green |
| Loader-injected methods | `getAttachedOrCreate` and `setAttached` appear only in the Fabric `Loader` |
| Gametests | 123 mod tests. The only loader API they name is Fabric's `@GameTest` annotation |
| Version conditionals | 69 `//? if` blocks, up from 58 at the end of P3. Written down so P4's addition is visible |

NeoForge API the seam maps onto, read from the `26.2.x` branch of NeoForge:

| Seam | NeoForge 26.2 |
|---|---|
| `playerData` | `AttachmentType.builder(initial).serialize(codec.fieldOf(...)).sync(streamCodec, (holder, to) -> holder == to)`, registered through `DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID)`; read with `getData`, written with `setData`. Neither loader copies it on death by default, which matches |
| `onServerTickEnd` | `ServerTickEvent.Post` |
| `onUseBlock`, `onUseItem` | `PlayerInteractEvent.RightClickBlock`, `RightClickItem`: on a non-`PASS` result, `setCancellationResult` and `setCanceled(true)` |
| `onTagsLoaded` | `TagsUpdatedEvent` |
| `onRegisterCommands` | `RegisterCommandsEvent#getDispatcher` |
| `onLootTable` | `LootTableLoadEvent`: `getKey()` is already a `ResourceKey<LootTable>`; append with `getTable().addPool(builder.build())` |
| `creativeTabBuilder` | `CreativeModeTab.builder()` |
| `configDir`, `isModLoaded`, `isDevelopmentEnvironment` | `FMLPaths.CONFIGDIR`, `ModList`, `FMLEnvironment`. The 1.21.9+ form: `FMLEnvironment.getDist()` rather than the static `dist` field 1.21.1 still has |
| `ClientLoader.addRightStatusBar` | `RegisterGuiLayersEvent#registerAbove(VanillaGuiLayers.FOOD_LEVEL, id, layer)`. **The stack height is unresolved**, see step 5 |
| config screen | `IConfigScreenFactory`, registered on the mod container |
| components ingredient | `DataComponentIngredient`: `{"neoforge:ingredient_type": "neoforge:components", "items", "components", "strict"}`, `strict` false by default |
| any-of ingredient | `CompoundIngredient`: `children`, with `ingredients` accepted as an alias |
| load conditions | `"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": ...}]` |
| gametests | `RegisterGameTestsEvent#registerTest(Identifier, GameTestInstance)`, test functions in `Registries.TEST_FUNCTION`; the game test server exits with the number of failed required tests |

## Decisions

Settle these at the start of step 1. Each has a recommendation; none of them blocks the spike.

**Node name: `26.2.x-neoforge`, Fabric nodes keep their names.** Renaming every node to `<version>-fabric`
the way the template does would touch CI artifact names, run directories, every `:26.2.x:` in the docs
and the active version, for no gain on a build that has one NeoForge node. `sc.current.version` is `26.2`
on both nodes, so the generated resources directory is shared without any change.

**Buildscripts: `build.gradle.kts` stays the Fabric one, `build.neoforge.gradle.kts` is new, and the
shared tail moves out.** What both nodes need (the Java toolchain, `checkLoaderSeam`, `checkVersionSeam`,
`buildAndCollect`, the `AGENTS.md` and `*.bak` excludes, the source directory wiring) goes to a script
plugin such as `gradle/shared.gradle.kts`, applied by both. Do not rename `build.gradle.kts` in the same
change; that is churn the diff does not need.

**Registration: defer the registering call, not the field.** See step 2. Keeps `ThirstItems.WATERSKIN`
and the other 110 or so item references typed as `Item`, rather than turning them into suppliers.

**Datagen output: translate at build time, not a second writer and not an alias.** A
`fabric:components` alias on NeoForge cannot work: the ingredient type is read from the
`fabric:type` key on Fabric and from `neoforge:ingredient_type` on NeoForge, so registering a type
under Fabric's name changes nothing about which key is read. The NeoForge buildscript rewrites the
three Fabric shapes in the 23 affected files as it copies resources. See step 4.

**Gametest mod id: `thirstwastaken2_gametest` on both loaders.** NeoForge mod ids cannot contain a
hyphen. Renaming it on Fabric too keeps test ids and report names the same on every node.

**Release: yes, marked beta, after the manual pass.** P3 released and stopped for feedback; do the same
here. The jar is named `ThirstWasTaken2-<version>+26.2-neoforge.jar`.

## Steps

### 0. Spike (1 day)

Stand the node up far enough to compile `main` and `client`, the way P0 did for 1.21.1, and record what
breaks. It exists to answer four questions before the estimate is trusted:

1. **Does registering directly inside `RegisterEvent` work?** NeoForge is expected to unfreeze each
   registry while its event fires. If it does not, step 2 changes shape.
2. **How does a HUD row move the air bubbles up on 26.2?** NeoForge's 1.21.x `Gui.rightHeight` does not
   appear in the `26.2.x` `Gui.java.patch`. Find what replaced it, in NeoForge or in vanilla's own HUD.
3. **Does `squeek.appleskin.ModConfig.INSTANCE` exist in AppleSkin's NeoForge jar** with the fields
   `AppleSkinIntegration` reads? If not, that read moves behind `ClientLoader`.
4. **Do the mixins apply?** NeoForge patches six of the classes they target: `Player`, `BucketItem`,
   `ItemStack`, `Blocks` and `CauldronBlock`, and on the client `Minecraft`. With `defaultRequire: 1`, a
   mixin that no longer matches crashes at startup, which is the answer arriving loudly. Launch the
   server once to find out.

**Check:** the four answers are written into this file, under the step they change. If more than one
of them is "no", re-estimate before step 1.

### 1. The node (1.5 to 2 days)

- `settings.gradle.kts`: the NeoForged repository in `pluginManagement`, ModDevGradle on the plugin
  classpath without applying it, and `version("26.2.x-neoforge", "26.2").buildscript =
  "build.neoforge.gradle.kts"`. `vcsVersion` and the active version stay `26.2.x`.
- `stonecutter.properties.toml`: a `["26.2.x-neoforge"]` table with `deps.neoforge`, `deps.appleskin`
  (a Modrinth version id, as on Fabric), `deps.jade`, `deps.cloth_config`, and a Minecraft range in
  NeoForge's own syntax, `[26.2,26.3)`, under a key of its own rather than reusing `mod.mc_compat`.
- `build.neoforge.gradle.kts`: `neoForge { version = ... }`, one `main` source set, and `runs` for
  client, server and game test server, each in `run/26.2.x-neoforge/...` like the Fabric runs.
- **One source set on NeoForge.** ModDevGradle has no split between `main` and `client`, so the
  NeoForge buildscript adds `src/client/java`, `src/client/resources` and `src/client/neoforge/*` to
  `main`. The Fabric nodes keep the split, so the compiler still catches client code reached from common
  code on four nodes out of five.
- Resources: `src/main/generated/26.2` as a resource root, the `src/main/neoforge/*` loader directories,
  and the same `processResources` expansion of `*.mixins.json`.
- The `stonecutterGenerate` dependency. The template needs `createMinecraftArtifacts` to depend on it;
  check whether `processResources` does too, as it does on Loom.
- No mutex on `createMinecraftArtifacts` yet. With one NeoForge node nothing runs it in parallel; P5
  adds it, see "Carried to P5" in [PLATFORM-PLAN.md](PLATFORM-PLAN.md#carried-to-p5).

**Check:** `:26.2.x-neoforge:compileJava` reaches the compiler and fails only on the missing NeoForge
`Loader` and `ClientLoader`, and `:26.2.x:build` still passes untouched.

### 2. The registration seam (1 to 2 days)

Items and blocks take an intrusive registry holder when they are constructed, and NeoForge freezes the
built-in registries before a mod is constructed. Constructing `ThirstItems`' static fields during mod
construction therefore fails on NeoForge, however they are registered afterwards. Data component types
and creative tabs have no intrusive holders but still cannot be registered into a frozen registry.

Add one seam:

```java
/** Runs {@code registration} when {@code registry} accepts new entries. */
public static void onRegister(ResourceKey<? extends Registry<?>> registry, Runnable registration)
```

- Fabric runs `registration` immediately.
- NeoForge queues it and runs it from `RegisterEvent` for that registry, on the mod bus.
- `ThirstWasTaken2.initialize` calls `Loader.onRegister(Registries.DATA_COMPONENT_TYPE,
  ThirstComponents::register)`, then `ITEM` with `ThirstItems::register` and `CREATIVE_MODE_TAB` with
  the tab. `ThirstItems.register` triggers the class's static initializer, which is where the items are
  built and registered; `Vanilla.registerItem` keeps its direct `Registry.register`.
- `ThirstComponents` gets a real `register()` that forces class initialization, and its fields are
  registered from there rather than at an arbitrary first touch.

The risk is anything touching `ThirstItems` before the item event, from a mixin, `ThirstApi` or a
static field elsewhere. On NeoForge that fails at startup, not silently, so the first launch finds it.
Also check that the component event fires before the item event, because the terracotta water bowl's
properties name the purity and salt components.

**Fallback, if the spike's first question comes back "no":** fields become `Supplier<Item>` or
`Holder<Item>` behind `DeferredRegister`. That is 23 files of mechanical `.get()` edits. Measure it before
starting rather than after.

**Check:** `:26.2.x:runGametest` still passes 123 tests. On Fabric the seam changes nothing but order.

### 3. The NeoForge `Loader` (1 to 2 days)

`src/main/neoforge/java/com/thirstwastaken2/platform/Loader.java`, with every public signature the Fabric
copy has, mapped as in the table above. Plus:

- `src/main/neoforge/java/com/thirstwastaken2/neoforge/ThirstWasTaken2NeoForge.java`, a `@Mod` class
  taking `IEventBus` and `ModContainer`. It hands the mod bus to `Loader` through a package-private
  method, then calls `ThirstWasTaken2.initialize()`. Nothing else, the same rule as the Fabric
  entrypoint.
- Game bus handlers on `NeoForge.EVENT_BUS`; registration and attachments on the mod bus.
- `playerData`: the `DeferredHolder` is resolved lazily inside the `PlayerData` record, because
  `ThirstData.STORAGE` is built during class initialization, before registration.
- `onUseBlock` and `onUseItem`: the Fabric callbacks return a result that stops the chain and becomes the
  interaction's result. Reproduce that exactly: cancel only on non-`PASS`, and set the cancellation
  result. Check which side each event fires on against Fabric; the gametests drive both through
  `player.gameMode`, so they will tell.

**Check:** `:26.2.x-neoforge:compileJava` passes; `checkLoaderSeam` still passes on every node.

### 4. Resources (1 to 2 days)

**Manifest.** `src/main/neoforge/resources/META-INF/neoforge.mods.toml`, templated like
`fabric.mod.json`: mod id, version, display name, authors, logo, `[[mixins]]` for
`thirstwastaken2.mixins.json` and the client config, required `neoforge` and `minecraft` dependencies,
and `optional` entries for AppleSkin, Jade and Farmer's Delight.

**Datagen output.** A transform in the NeoForge buildscript's `processResources`, over the JSON under
`src/main/generated/26.2` only:

| Fabric | NeoForge |
|---|---|
| `"fabric:type": "fabric:components"`, `base`, `components` | `"neoforge:ingredient_type": "neoforge:components"`, `items`, `components`, no `strict` |
| `"fabric:type": "fabric:any"`, `ingredients` | `"neoforge:ingredient_type": "neoforge:compound"`, `children` |
| `"fabric:load_conditions": [{"condition": "fabric:all_mods_loaded", "values": [...]}]` | `"neoforge:conditions": [{"type": "neoforge:mod_loaded", "modid": ...}]`, one per mod |

Read and write through a JSON parser, not string replacement, and keep it next to the seam checks so
it is found. Fabric's components ingredient matches a stack that carries at least the listed components,
which is NeoForge's `strict: false`; confirm with `PurificationGameTest` rather than by reading.

Add `checkNeoForgeResources`: fails when any `"fabric:` key survives in the processed resources. A new
Fabric-only shape in a generator then fails the NeoForge build instead of loading as a broken recipe.

**Check:** the processed recipes load without a parse error in the server log, with Farmer's Delight
absent, and the four Farmer's Delight files are skipped by their condition rather than reported as an
unknown recipe type.

### 5. Client (1.5 to 3 days)

- **`ClientLoader`** in `src/client/neoforge`: `addRightStatusBar` registers a GUI layer above
  `VanillaGuiLayers.FOOD_LEVEL`, and moves the stack up by the answer to the spike's second question.
  If that answer is "nothing does", draw the row from a layer and shift the air bubbles with a client
  mixin, the way 1.21.1's `GuiMixin` does; that mixin then lives in `src/client/neoforge`.
- **`MinecraftMixin`** names no loader API. Move it to a loader independent client mixin config,
  `src/client/resources/thirstwastaken2.client.mixins.json`, with the class under
  `com.thirstwastaken2.client.mixin`, which `checkVersionSeam` already allows. `GuiMixin` and
  `LocalPlayerMixin` stay in the Fabric config: both are empty on 26.2, and what 1.21.1 NeoForge needs is
  P5's question.
- **Config screen.** `ModMenuIntegration` stays Fabric. The NeoForge client entrypoint registers
  `IConfigScreenFactory` returning `ThirstConfigScreen`.
- **Client entrypoint.** `ThirstWasTaken2NeoForgeClient`, a `@Mod(dist = Dist.CLIENT)` class calling
  `ThirstWasTaken2Client.initialize()`.
- **AppleSkin.** Depending on the spike: nothing, or the `ModConfig` read behind `ClientLoader`.
- **Jade.** Nothing expected, through `@WailaPlugin`. The manual pass checks the dedicated server,
  still the one open item in [MANUAL-TESTING.md](MANUAL-TESTING.md).

**Check:** `:26.2.x-neoforge:runClient` starts with AppleSkin, Jade and Cloth Config, and the thirst bar
draws above hunger with the air bubbles above it underwater.

### 6. Gametests (2 to 3 days)

The same 123 test methods, run by NeoForge's game test server. No test body changes.

- **Annotation.** A replacement in `stonecutter.gradle.kts`, only on nodes whose project name ends in
  `-neoforge`, swapping the import of `net.fabricmc.fabric.api.gametest.v1.GameTest` for a
  `com.thirstwastaken2.gametest.GameTest` annotation that lives in `src/gametest/neoforge`. Keep writing
  `@GameTest` with no arguments, the rule the 1.21.1 harness already relies on.
- **Discovery.** One list of test classes. The NeoForge harness reads the `fabric-gametest` entrypoint
  list out of `src/gametest/resources/fabric.mod.json`, so a class added there runs on every node, and a
  class missing from it is missing everywhere, the same as today.
- **Registration.** For every annotated method, a `Consumer<GameTestHelper>` in `TEST_FUNCTION` and a
  `FunctionGameTestInstance` through `RegisterGameTestsEvent`, with Fabric's defaults: empty structure,
  the default environment, required, and the same tick limit. Ship an empty structure in the gametest
  resources if NeoForge has none to name. Test ids follow Fabric's, `<mod id>:<class>/<method>` in lower
  case.
- **Harness.** A gametest `neoforge.mods.toml`, a ModDevGradle mod entry for the source set, a
  `gameTestServer` run with `neoforge.enabledGameTestNamespaces=thirstwastaken2_gametest`, and a
  `runGametest` task that runs it, so CI and the docs use one task name on every node.
- **Report.** Fabric writes `build/gametest/report.xml`. Find whether the NeoForge server can write the
  same JUnit report; if not, CI uploads the log instead, and the exit code stays the gate.

Before trusting the node, break one thing on purpose, as the gametest rules require: comment out the
`onUseBlock` registration in the NeoForge `Loader` and see the drinking tests go red on that node only.

**Check:** `:26.2.x-neoforge:runGametest` reports 123 of 123, and deliberately breaking the NeoForge
`Loader` fails it.

### 7. CI (half a day)

- `discover` already builds one job per table in `stonecutter.properties.toml`; the new table adds the
  job.
- Skip `devClasses` and `checkDatagen` on `-neoforge` jobs, and run `checkNeoForgeResources` there.
  `checkLoaderSeam` and `checkVersionSeam` read the same files on every node; running them on one
  NeoForge job too is harmless.
- Cache: ModDevGradle downloads and decompiles its own Minecraft artifacts. Check the job time against
  the Fabric ones before accepting it.

**Check:** five green jobs on a pull request.

### 8. Manual pass and docs (1 day)

- A NeoForge section in [MANUAL-TESTING.md](MANUAL-TESTING.md), a subset of the Fabric one: the HUD and
  its stacking with air, AppleSkin's outline and tooltips, the config screen from the mods list, Jade on
  water and cauldrons, drinking by hand, a dedicated server start with and without Jade, and dying and
  respawning with a thirst value that is not full.
- [platform/AGENTS.md](../../src/main/java/com/thirstwastaken2/platform/AGENTS.md): the NeoForge
  mapping, `onRegister`, and that datagen output is translated, not written twice.
- [src/main/resources/AGENTS.md](../../src/main/resources/AGENTS.md), [src/gametest/java/AGENTS.md](../../src/gametest/java/AGENTS.md)
  and the root [AGENTS.md](../../AGENTS.md): two manifests, the NeoForge harness, the node name.
- [VERSION-DIFFERENCES.md](VERSION-DIFFERENCES.md) only if a version conditional was added.
- CHANGELOG, the docs site and the Modrinth page through the `write-docs` skill, for the release.
- The count of `//? if` blocks, written into [PLATFORM-PLAN.md](PLATFORM-PLAN.md) next to P3's.

## Measurements

Decide by these, the same way P3 did.

| Watch | Why | Stop when |
|---|---|---|
| Days spent | the exit ramp in the platform plan | past 20: ship what is green, keep NeoForge on 26.2, drop P5 |
| Lines in each `Loader` copy | the settled estimate was 300 to 500 per loader, which is what ruled out Architectury | past 700 in the NeoForge pair: reconsider before going on |
| Loader conditionals in `src/main/java` or `src/client/java` | the invariant: the two axes never meet in one file | any: add the seam instead |
| Gametests on the NeoForge node | the platform plan's rule | cannot be made to run: the node does not ship |
| NeoForge-only branches in gametest bodies | a forked assertion means the tests stopped testing the same thing | any: fix the harness, not the test |

## Done when

- Five nodes build, pass `checkLoaderSeam` and `checkVersionSeam`, and pass their gametests in CI.
- The NeoForge manual pass is ticked.
- The P4 row in [PLATFORM-PLAN.md](PLATFORM-PLAN.md) is struck through, with the numbers.
- This file is deleted, with what outlives it moved into the `AGENTS.md` files listed in step 8.
