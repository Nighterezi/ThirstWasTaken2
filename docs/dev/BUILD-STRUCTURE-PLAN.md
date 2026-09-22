# Build structure plan

How to keep the multi-loader, multi-version build maintainable as optional integrations grow. The
number of Minecraft versions is already capped at four (see the root `AGENTS.md`). Integrations are
not capped: each one multiplies by loader, by version and by NeoForge's fluid API generation. This
plan puts them under the same rules the core code already follows, and stops each loader script from
wiring them by hand.

Nothing here changes what a player gets. Every step must leave the ten jars with the same contents.

## Status

| # | Item | Kind | Status |
|---|---|---|---|
| 0 | Fix stale comments and doc paths | docs | done |
| 1 | `checkVersionSeam` covers integration and loader directories; integrations get a `platform/` package | check | done |
| 2 | Choose how build code is shared between the two loader scripts | decision | decided: A, `build-logic` |
| 3 | `flightRecorder` and the `-PwithoutOptional` skeleton in one place | build | done |
| 4 | Integrations described by one table that both loader scripts read | build | done |
| 5 | Docs: root `AGENTS.md`, `shared.gradle.kts` header, `platform/AGENTS.md` | docs | done |

What was checked when 0 to 5 went in, on all ten nodes: `buildAndCollect`, `checkVersionSeam`,
`checkLoaderSeam`, `checkOptionalSeam` and `runGametest` pass, and `./gradlew -p build-logic test`
passes. Every jar's entries and their CRCs, and every manifest, were compared before and after. Item 1
changed only what it had to: `Vanilla`, the Sophisticated classes it touched and the two new
`sophisticated/platform` classes, plus `ThirstConfig.java` in the sources jars for item 0's comments.
Items 3 and 4 changed nothing at all, manifests included, byte for byte. A made-up row copied from
Kaleidoscope's, with a directory of its own, was wired and named in both manifests by the row alone,
then thrown away. Still to do by hand: the Drinking upgrade's manual steps in
`SOPHISTICATED-INTEGRATION.md` on `1.21.1-neoforge` and `26.2.x-neoforge`, and a `-PwithoutOptional=all`
dev client on `1.21.1` and `1.21.1-neoforge`.

Order: 0 and 1 are independent and can land first. 2 must be decided before 3 and 4. 3 is small and
proves the sharing mechanism before 4 relies on it. 5 goes with each step, not at the end.

A Stonecutter upgrade is not part of this plan. When a stable 0.10 comes out it is its own change,
never in the same one as these items: if the ten jars come out different, it has to be clear which
change did it.

## 0. Stale comments and doc paths

The `docs/dev` files moved into `integration/`, `mechanics/` and `benchmark/`. These still name the
old paths:

| File | Line | Should point to |
|---|---|---|
| `src/main/java/com/thirstwastaken2/config/ThirstConfig.java` | 67 | `docs/dev/mechanics/WATER-PURIFICATION-BALANCE.md` |
| `src/main/java/com/thirstwastaken2/config/ThirstConfig.java` | 79 | `docs/dev/mechanics/WATER-SICKNESS.md` |
| `src/main/sophisticated/AGENTS.md` | 20 | `docs/dev/integration/SOPHISTICATED-INTEGRATION.md` |
| `src/main/supplementaries/AGENTS.md` | 32 | `docs/dev/integration/SUPPLEMENTARIES-INTEGRATION.md` |
| `src/dev/java/com/thirstwastaken2/dev/benchmark/AGENTS.md` | 228 | `docs/dev/benchmark/BENCHMARK-BASELINE.md` |
| `docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md` | 116 | `docs/dev/integration/KALEIDOSCOPE-COOKERY-INTEGRATION.md` |
| `AGENTS.md` | 185–192 | link text only; the targets are already right |

These say the NeoForge nodes read what the **26.2** Fabric node generates. They read
`src/main/generated/${sc.current.version}`, which the Fabric node of the **same Minecraft version**
writes:

- `build.neoforge.gradle.kts` lines 9–10 (the header calls the file "the NeoForge node, `26.2.x-neoforge`"),
  63, 89 and 542.
- `.github/workflows/build.yml` line 103.

Check: `git grep -nE "docs/dev/[A-Z-]+\.md"` finds nothing but links whose target exists, and
`git grep -n "26.2 Fabric\|:26.2.x:runDatagen"` finds nothing but `CHANGELOG.md` and
`docs/dev/MANUAL-TESTING.md`, which mean 26.2 Fabric itself and are right as they are.

## 1. Version conditionals in integrations

### Today

`checkVersionSeam` (`gradle/shared.gradle.kts`) scans `src/main/java` and `src/client/java` only.
Every other Java root is outside it: the loader directories, the fluid API directories and every
integration. Five integration files carry `//?` outside `platform/` and `mixin/`, and no check notices:

| File | Branches | What differs |
|---|---|---|
| `sophisticated/SophisticatedPresence.java` | 1 | how a NeoForge mod file is searched for a class (1.21.2) |
| `sophisticated/drinking/DrinkingUpgradeContainer.java` | 2 | `CompoundTag#getStringOr` / `getIntOr` (1.21.5) |
| `sophisticated/drinking/DrinkingUpgradeItem.java` | 1 | the `UpgradeItemBase` constructor takes `Item.Properties` with an id (1.21.2) |
| `sophisticated/drinking/DrinkingUpgradeWrapper.java` | 2 | `ItemUseAnimation` / `UseAnim` (1.21.2), import and use |
| `sophisticated/mixin/AlchemyUpgradeWrapperMixin.java` | 2 | in `mixin/`, already allowed |

Kaleidoscope Cookery has none yet, but its plan widens it to 26.x, where it will need them.

### Change

1. **Scan every hand-written Java root**, found by directory rather than listed:
   `src/{main,client}/java` and `src/{main,client}/*/java`, skipping `src/main/generated`. A new
   integration directory is then covered the day it is created, without anyone remembering to add it.
   The rule stays the same: a `//?` is allowed only under a path segment named `platform` or `mixin`.
   The loader directories already pass (`platform/Loader`, `platform/ClientLoader`, `fabric/mixin/GuiMixin`).
2. **Move the Sophisticated branches out**, choosing where by what differs:
   - A **vanilla** difference goes into core `platform/Vanilla`, where every integration can use it:
     `Vanilla.getString(CompoundTag, String, String)`, `Vanilla.getInt(CompoundTag, String, int)` and
     `Vanilla.isDrinkAnimation(ItemStack)`. Check first that core does not already need them.
   - A difference in **the other mod's API** or in **the loader**, only that integration sees it, goes in
     `com.thirstwastaken2.<integration>.platform`. For Sophisticated: the mod file probe of
     `SophisticatedPresence`, and a small `platform/SophisticatedUpgradeItem` that holds the
     constructor branch and that `DrinkingUpgradeItem` extends. A constructor's `super(...)` call
     cannot be moved into a static method, and this is the same shape as core's `platform/DrinkItem`.
3. **Also scan for loader APIs.** `checkLoaderSeam` should cover the integration directories both
   loaders compile (`supplementaries`, `kaleidoscope`), which must not name `net.fabricmc` or
   `net.neoforged`. `createfly` is Fabric only, `create` and `sophisticated*` NeoForge only; they may.
   It reads the `loaders` column of the table, handed over by the loader scripts (see item 2).

### Watch out

- The integration `platform` classes are loaded only after the gate, like the rest of the
  integration. `checkOptionalSeam` has to stay green on every node; run it, don't assume it.
- Moving an import of `ItemUseAnimation` behind `Vanilla` keeps `DrinkingUpgradeWrapper` free of it
  altogether, which is the point. Do not replace the branch with a replacement rule in
  `stonecutter.gradle.kts`: `UseAnim` → `ItemUseAnimation` is not a pure rename, the enum constant
  names differ too.

### Done when

- `checkVersionSeam` fails on a `//?` added to any file under `src/main/sophisticated/java/.../drinking`,
  and passes on all ten nodes as they are.
- `runGametest` passes on every node, and the Drinking upgrade still works on `1.21.1-neoforge` and
  `26.2.x-neoforge` (the manual steps in `SOPHISTICATED-INTEGRATION.md`).

## 2. Decision: how the two loader scripts share code

`gradle/shared.gradle.kts` is applied with `apply(from = ...)`, at the **end** of each loader script
(lines 584 and 724). A script applied like that is compiled on its own. It cannot hand the applying
script a Kotlin function, a class or a typed value. Today the only thing passed back is
`extra["thirst.optionalRunMods"]`, a plain `Set`. So "put the table in `shared.gradle.kts`" does not
work directly. There are three ways to do it:

| Option | How | For | Against |
|---|---|---|---|
| **A. `build-logic` included build** (recommended) | `includeBuild("build-logic")` in `settings.gradle.kts`; plain Kotlin classes: `Integration`, `integrations`, `flightRecorder`, `OptionalRunMods` | Typed, the IDE follows it, both scripts call the same functions, can have unit tests | One more build to know about; it has to be kept free of Loom and ModDevGradle |
| B. `extra` properties | `shared.gradle.kts` puts maps and lambdas into `extra`, the scripts cast them back | No new build | Untyped casts everywhere; `shared.gradle.kts` must be applied before the wiring, not at the end, so it has to be split |
| C. Data file | A `integrations.toml` read by both scripts | The table is plain data | Each script still needs the code that reads and applies it, which is duplicated again, or needs A |

**Recommendation: A**, with one rule that keeps it small. `build-logic` contains **data and pure
functions only**. It does not depend on Loom, ModDevGradle or Stonecutter. It returns directories,
names and argument lists. The loader scripts still make every `sourceSets`, `loom` and `neoForge` call
themselves, and pass in what they know (`sc.current.parsed`, the loader). Then a Loom or ModDevGradle
upgrade never touches `build-logic`, and `build-logic` never needs to know which version is active.

`shared.gradle.kts` stays as it is for what it already does: the toolchain, the seam checks and
`buildAndCollect`.

### How it is wired

- `includeBuild("build-logic")` is inside `pluginManagement { }` in `settings.gradle.kts`, and
  `build-logic` declares one plugin, `thirstwastaken2.build-logic`, that does nothing when applied.
  `build.gradle.kts`, `build.neoforge.gradle.kts` and `stonecutter.gradle.kts` list it in `plugins { }`,
  which is what puts its classes on their classpath. That is simpler than a `buildscript` classpath
  with dependency substitution.
- An included build, not `buildSrc`: a change to `buildSrc` recompiles every build script and throws
  away the configuration cache, which ten nodes would feel on every edit.
- It uses `kotlin-dsl` and names no Kotlin version, so its Kotlin is always the one Gradle embeds and a
  Gradle upgrade never leaves the two apart.
- The rule that keeps it small is checkable: `build-logic/build.gradle.kts` has no dependency but
  `kotlin-dsl` and the test suite.
- **A script applied with `apply(from)` cannot see `build-logic`'s classes**; it has a classpath of its
  own. So `shared.gradle.kts` is handed the two lists its checks need, `thirst.integrations` and
  `thirst.loaderIndependentIntegrations`, as extra properties, the same way it already gets
  `thirst.requiredJava`.
- Later, not in this plan: `shared.gradle.kts` could become a convention plugin in `build-logic`. It
  applies only the `java` plugin, so the rule above would still hold, and the extra properties would
  become parameters. Gradle discourages `apply(from)` script plugins, which is the reason to.

## 3. `flightRecorder` and the `-PwithoutOptional` skeleton

### Today

- `flightRecorder(file)` is written out twice, word for word (`build.gradle.kts:52`,
  `build.neoforge.gradle.kts:262`).
- Reading `-PwithoutOptional` into a set is written out **three** times: both loader scripts and
  `stonecutter.gradle.kts:126`.
- `runClientMod` is the same skeleton in both scripts. It records the names in `optionalRunMods`,
  skips when `all` or one of the names was asked for, and otherwise adds the dependency. Only the
  last step differs: Fabric calls `clientMod("clientRuntimeOnly", ...)`, NeoForge adds to the
  `clientRunMods` configuration and takes a `configure` block.

### Change

In `build-logic` (option A):

```kotlin
fun flightRecorder(file: File): List<String>

/** Parses -PwithoutOptional, records every name a node offers, and answers whether to add a mod. */
class OptionalRunMods(asked: String?) {
    val offered: Set<String>
    fun include(names: List<String>): Boolean
}
fun parseWithoutOptional(value: String?): Set<String>
```

Each script keeps a one-line `runClientMod` that calls `include(names)` and adds the dependency its
own way. `stonecutter.gradle.kts` uses `parseWithoutOptional`. The export through
`extra["thirst.optionalRunMods"]` stays, because `stonecutter.gradle.kts` reads it across projects.

### Done when

- `./gradlew ":26.3.x:runClient" -PwithoutOptional=jade` and the same on `26.3.x-neoforge` leave Jade
  out, `-PwithoutOptional=nosuchmod` still fails the build with the list of names, and
  `-PwithoutOptional=all` with `tools/agent/boot.jsonl` still comes up and stays up.
- `-Pprofile` on `runBenchmark` still writes a `.jfr` on one Fabric and one NeoForge node.

## 4. One table of integrations

### Today

Each integration is wired by hand in each loader script it builds on: a `findProperty("deps.…")`,
an `if` block adding source directories, and a `processResources` block that patches the built
manifest. About 84 lines across the two scripts name an integration. Supplementaries and Kaleidoscope
Cookery are compiled by both loaders, so their blocks exist twice and must be kept in step.

What the blocks have in common, per integration:

| Integration | deps key | Loaders | Source dirs | Fluid API split | Mixin config | NeoForge optional deps | Fabric entrypoints |
|---|---|---|---|---|---|---|---|
| Create Fly | `deps.create_fly` | Fabric | main, client, dev | no | `thirstwastaken2.createfly.mixins.json` (inserted at index 1) | — | `thirstwastaken2:createfly`, `thirstwastaken2:createfly_client` |
| Create | `deps.create` | NeoForge | main | no | `thirstwastaken2.create.mixins.json` | `create` | — |
| Sophisticated | `deps.sophisticated_core` | NeoForge | main, client | yes (`sophisticated-transfer` / `-fluidhandler`) | `thirstwastaken2.sophisticated.mixins.json` | `sophisticatedcore` | — |
| Supplementaries | `deps.supplementaries` | both | main, client | no | `thirstwastaken2.supplementaries.mixins.json` | `supplementaries`, `moonlight` | adds `SupplementariesJade` to `jade` |
| Kaleidoscope Cookery | `deps.kaleidoscope_cookery` | both | main | no | `thirstwastaken2.kaleidoscope.mixins.json` | `kaleidoscope_cookery` | — |

### Change

In `build-logic`:

```kotlin
data class Integration(
    val dir: String,                      // "sophisticated": src/main/<dir>, src/client/<dir>, src/dev/<dir>
    val depsKey: String,                  // "deps.sophisticated_core"
    val loaders: Set<String>,             // what may compile it, for checkLoaderSeam
    val client: Boolean,
    val dev: Boolean,
    val fluidApiSplit: Boolean,           // adds src/main/<dir>-transfer or -fluidhandler
    val mixinConfig: String,
    val neoForgeDependencies: List<String>,
    val fabricEntrypoints: Map<String, List<String>>,
)

val integrations: List<Integration>

/** The directories an integration adds on this node; the caller decides which source set gets which. */
fun Integration.mainDirs(transferApi: Boolean): List<String>
fun Integration.neoForgeManifest(): String           // the [[mixins]] and [[dependencies]] text
fun Integration.patchFabricManifest(json: MutableMap<String, Any>)
```

Each loader script then has one loop in place of its blocks:

```kotlin
for (integration in integrations.filter { findProperty(it.depsKey) != null }) {
    // Fabric: main dirs into `main`, client dirs into `client`, dev dirs into `dev`.
    // NeoForge: main dirs into `main`, client dirs from the preprocessed `clientSources`, as today.
}
```

and `processResources` makes one pass over the same filtered list for the manifest.

### What stays in the loader scripts

The table covers the part that repeats. It does not try to cover what is specific to one mod, and
each of these keeps its own block and comment:

- `createFlyClasses` (Fabric): Create Fly's classes without its class tweaker.
- `createLibraries` (NeoForge): the libraries Create bundles, for the compiler.
- `nestedMods` (Fabric): nested jars Loom does not unpack.
- The dependencies themselves: `compileOnly` notations, `runClientMod` calls and their names.

If a new integration needs something the table cannot say, add a block for it rather than a column
only one row uses.

### Watch out

- **Order is part of the output.** Create Fly's mixin config goes in at index 1 of `mixins`, the rest
  are appended. The NeoForge manifest is appended to in a fixed order. Keep both orders, or check that
  the new order loads the same (it is a manifest, so it should, but check before claiming it).
- `processResources` declares `inputs.property(...)` per integration so that a change of version
  reruns it. Keep one input per integration, from the table.
- `checkOptionalSeam` finds its roots by itself. Do not make it depend on the table unless it misses
  something without it. It did miss one thing: the pattern of integration packages core code must not
  name was written out by hand, so a new integration would not have been in it. That pattern now comes
  from the table's `dir` column; the roots are still found from the compiled classes.

### Done when

All of this, on all ten nodes:

- Before the change, run `./gradlew buildAndCollect` and save, for each jar, the sorted list of entries
  (`unzip -Z1`) and the manifest (`fabric.mod.json` or `META-INF/neoforge.mods.toml`). After the change,
  the entry lists are the same and the manifests have the same content (same order, or an order
  difference that has been checked).
- CI is green: every seam check, `checkDatagen`, `checkNeoForgeResources` and `runGametest`.
- `runClient -Pagent=tools/agent/boot.jsonl -PwithoutOptional=all` comes up on one Fabric and one
  NeoForge node that build integrations (`1.21.1`, `1.21.1-neoforge`).
- Adding a made-up row to the table (for example a copy of Kaleidoscope's row with another `dir`) is
  the only change needed to wire a new integration's directories and manifest on both loaders. Try it
  once on a local branch and throw it away.

## 5. Docs that change with this

- **Root `AGENTS.md`**: the source roots table points to the integration table instead of listing
  what each loader script does; "Adding an integration" becomes a short checklist: add a row, add the
  deps keys to `stonecutter.properties.toml` and `MODRINTH_DEPS`, put version differences in
  `<integration>/platform`.
- **`gradle/shared.gradle.kts` header**: it says the source directory wiring is deliberately not
  there. After item 4 that wiring is in `build-logic`, but it is still not in `shared.gradle.kts`, so
  the text has to say where it went.
- **`src/main/java/com/thirstwastaken2/platform/AGENTS.md`**: an integration may have its own
  `platform/` package for its own mod's API. A vanilla difference still goes into core `Vanilla`.
- **Each integration `AGENTS.md`** that describes its build block points to its row in the table.

## Not doing

- **Moving the loader scripts' Loom or ModDevGradle calls into `build-logic`.** That would make
  `build-logic` depend on both plugins and on their versions, which is the coupling this plan avoids.
- **One build script for both loaders.** Loom splits `main` and `client`, ModDevGradle does not, and
  the two scripts differ in that on purpose.
- **Upgrading Stonecutter as part of items 1 to 4.** See the note under Status.
