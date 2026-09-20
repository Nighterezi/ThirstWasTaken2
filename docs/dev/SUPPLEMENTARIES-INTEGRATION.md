# Supplementaries integration plan

What ThirstWasTaken2 does and still has to do with
[Supplementaries](https://modrinth.com/mod/supplementaries) on **both loaders, on Minecraft 1.21.1
only**. This file is the order of work, what each step needs and what it is checked with; how the
finished parts work is in
[src/main/supplementaries/AGENTS.md](../../src/main/supplementaries/AGENTS.md).

Written on 2026-09-20 from Supplementaries `1.21.1-3.9.9` and Moonlight Lib `1.21.1-3.6.5`, read from
the `1.21.1` branch of [Supplementaries](https://github.com/MehVahdJukaar/Supplementaries) and the
`1.21` branch of [Moonlight](https://github.com/MehVahdJukaar/Moonlight), which is that exact release.

## Why 1.21.1, and why both loaders

Supplementaries' newest Minecraft version is **1.21.1**. It has no 1.21.11, 26.1, 26.2 or 26.3 build,
so `1.21.1` and `1.21.1-neoforge` are the only two nodes this can touch, and the 2026-09 changelog
line saying the Supplementaries integration awaits compatible releases stays true for every other
node. Both 1.21.1 uploads exist, Fabric and NeoForge, from the same source.

That makes this the **first optional integration that both loaders compile**. Everything it touches is
in Moonlight Lib, Supplementaries' required library, which is multi-loader: `SoftFluidTank`,
`SoftFluidStack` and `SoftFluid` are the same classes with the same signatures on Fabric and on
NeoForge. One source directory serves both, and since it is one Minecraft version it carries no
Stonecutter branch either. Create Fly (Fabric) and Sophisticated (NeoForge) each needed a directory
per loader because the mods themselves differ; this one does not.

It also needs no core-code fork, so it does not push against the rule in `AGENTS.md` that 1.21.1 is
retired rather than forked for. If 1.21.1 is ever retired, this integration goes with it and nothing
else moves.

## Where water lives in Supplementaries

Supplementaries does not use Minecraft's fluids for its containers. It uses Moonlight's **soft fluid**
system, a data-driven registry (`moonlight:soft_fluid`) whose entries are JSON, counted in **bottles**
rather than millibuckets: one bottle, two to a bowl, four to a bucket. On NeoForge a bottle converts to
**250 mB**, the same serving the mod's own waterskin and bowls already move.

| Piece | What it is |
|---|---|
| `SoftFluid` | a registry entry: textures, colour, `containers` (filled item to empty item pairs), an optional `food`, and `preserved_components_from_item` |
| `SoftFluidStack` | a fluid, a count in bottles, and a `DataComponentPatch`. It implements `DataComponentHolder` |
| `SoftFluidTank` | what a block holds. `interactWithPlayer`, `fillItem`, `drainItem`, `tryDrinkUpFluid` |
| `ISoftFluidTankProvider` | the interface a block entity implements so a faucet can reach its tank |
| `FaucetBehaviorsManager` | the faucet's source and target list, first match wins |

Three blocks matter: the **Jar** (capacity from `JAR_CAPACITY`, drinking gated on `JAR_EAT`), the
**Goblet** (one bottle, drinking gated on `GOBLET_DRINK`) and the **Faucet**, which moves fluid between
a block behind it and a block below, vanilla water cauldrons included.

**`SoftFluidStack` already carries data components**, and `moonlight:water` already lists a third
party's thirst component in `preserved_components_from_item`:

```json
"preserved_components_from_item": ["thirst:purity"]
```

That is not this mod. It is the other Thirst mod, and it is the proof that the author treats this list
as the place a thirst mod's grade goes.

## Status

| # | Item | Kind | Status |
|---|---|---|---|
| 1 | Water keeps its grade through jars, goblets and faucets | bug | **Done**, both loaders |
| 2 | Sea water stays sea water | bug | **Done**, both loaders |
| 3 | Faucet and water cauldron keep the cauldron's grade | bug | **Done**, both loaders |
| 4 | Drink from a jar or a goblet | feature | **Done**, both loaders |
| 10 | Faucet on world water samples it | bug | Not started |
| 5 | Faucet fills and drains the hanging pot | feature | Not started |
| 6 | The mod's own bowls as containers Moonlight knows | data | Not started |
| 7 | The grade is visible: tooltip, tint, Jade | feature | Not started |
| 8 | Upstream: the two components in Moonlight's `water.json` | upstream | Not started |
| 9 | Changelog and player docs | docs | Not started |

Order as with Sophisticated: **the bugs first**, item 1 before the others because it is the root cause
of most of them, then the cheapest feature, then the rest. Item 8 runs in parallel and blocks nothing.
Item 9 last.

Items 1 to 4 are in, on both 1.21.1 nodes. How they work is in
[src/main/supplementaries/AGENTS.md](../../src/main/supplementaries/AGENTS.md); what each one turned
out to need is below. Item 10 was found while building them and is a bug of the same family, so it
comes before the features. The player docs of item 9 are still to write, so nothing here is announced
yet.

## The bugs, and the one cause under them

The four finished items were checked in a real client on both 1.21.1 nodes with
[tools/agent/supplementaries.jsonl](../../tools/agent/supplementaries.jsonl), which pours a dirty
bottle and a salty one through a jar and draws them back, pours a pure bottle into a goblet and drinks
it, and runs two faucets between cauldrons. Both nodes answer the same. The gametests, which run
without either mod, still pass on every node, which is the other half of the check.

With both mods installed today, **a jar launders water**. A dirty bottle poured into a jar and drawn
back out is a plain water bottle, which the mod reads as `defaultPurity`, Clean by default. The same
holds for the goblet, for a bucket, and for a faucet pointed at a cauldron. That is a free purification
chain built out of two decorative blocks, and it is worse than the Sophisticated Tank bug was, because
the jar is cheap and early.

Every part of it is one thing: **nothing carries the grade across the item to fluid boundary.**
`SoftFluidStack.fromItem` copies only the components in `SoftFluid.getPreservedComponents()`, and
`moonlight:water` does not list this mod's two. The same list is consulted when a filled item is made
again (`createFilledStacks`), and on NeoForge when a `SoftFluidStack` is converted to a `FluidStack`
and back (`SoftFluidStackImpl.fromForgeFluid` and `toForgeFluid`). One list, four paths.

Preserving the components is necessary but **not sufficient**, for two reasons that are this mod's own
rules and not Moonlight's fault:

- **A fresh container must write `water_salty: false`.** All 27 purification recipes match on it, so a
  bottle that leaves it out silently stops being cookable. That already went wrong once with looted
  bottles, and `PurificationGameTest` watches for it.
- **Sprites are part of the contract.** `WaterPurity.setQuality` runs `syncModel`; a bottle built by
  Moonlight through `PotionContents.createItemStack` never goes through it, so a salty bottle out of a
  jar would look like ordinary water.

So the fix is two injections, not one, and the second is where the mod's own rules are applied.

### 1. Water keeps its grade (done)

`SoftFluidMixin` on `getPreservedComponents`: when the fluid is water, return the entry's list plus
`thirstwastaken2:water_purity` and `thirstwastaken2:water_salty`. One return-value inject, body one
line, and it covers all four paths above at once, the NeoForge `FluidStack` bridge included. The
waterskin and the terracotta bowls already carry the item fluid capability on NeoForge
(`purity/AGENTS.md`), so that bridge is how a faucet reaches them.

`SoftFluidStackMixin` on `splitToItem`, the one choke point every container a tank fills comes out of:
run the resulting stack through `WaterPurity.setQuality` with the quality read off the fluid **before**
the call, since that call spends the serving and a stack spent to nothing reads as empty. That is what
writes `water_salty: false`, clears a grade off salt water and points the model at the right item
definition.

A third hook was needed that the plan did not foresee. `SoftFluidTankMixin` on `addFluid` stamps water
entering a tank with the grade it already reads as, before the tank decides whether it fits: water that
arrives without a grade would otherwise refuse to share a tank with water that has one, although both
read as `defaultPurity` everywhere else. It also comes down to the one component the tank compares on,
since a container arrives carrying both.

Every target is matched **by name alone**, with no descriptor, so nothing here depends on Loom
remapping Minecraft names inside a mixin annotation for a class that is not Minecraft's.

One helper, `SoftFluidQuality`, is the only place that maps a `SoftFluidStack` to a `WaterQuality` and
back, the way `WaterFluids` is for NeoForge `FluidStack`s.

**A consequence worth stating:** once the components are on the fluid, `isFluidCompatible` refuses to
mix grades, because it compares components. A jar holding Murky water will not take Clean water. That
is the same rule the NeoForge containers already keep, and the same rule pouring by hand keeps.

Checked with `tools/agent/supplementaries.jsonl` on both nodes: after a dirty bottle is poured in, the
jar's own block data reads `{components: {"thirstwastaken2:water_purity": 0}, id: "moonlight:water"}`,
and the bottle drawn back out of it is `water_purity 0, water_salty false`.

### 2. Sea water stays sea water (done)

A sea-water bottle is a plain `minecraft:potion` holding water, told apart only by `water_salty`.
`SoftFluidStack.fromItem` sees `Potions.WATER` and converts it to `moonlight:water`, so without item 1
sea water becomes fresh water in a jar, which is the one conversion the whole `WaterQuality` design
exists to prevent. Item 1 carries the flag, and the `toItem` half gives the bottle back its sprite and
keeps it out of the cooking recipes.

What still needs deciding: **whether the goblet and the jar should show sea water as turquoise**, as
the mod's own bowls do. That is item 7 and can wait; the data being right is what matters here.

Checked in the same script: a salty bottle poured into a jar leaves `{"thirstwastaken2:water_salty":
1b}` on the fluid, and the bottle drawn back out is salty and carries no grade at all.

### 3. Faucet and water cauldron (done)

`WaterCauldronInteraction` in Supplementaries is both a faucet source and a faucet target for vanilla
cauldrons, and it knows nothing about the `purity` blockstate value this mod adds:

- draining offers `MLBuiltinSoftFluids.WATER` with no components, so a dirty cauldron fills a jar with
  water that reads Clean;
- filling an empty cauldron sets `Blocks.WATER_CAULDRON.defaultBlockState()`, which drops the property
  back to `0`, unset, so whatever was poured in reads `defaultPurity`.

The cauldron is the one carrier whose quality is **not** a component, so item 1 does not reach it.
`WaterCauldronInteractionMixin` stamps the offer from `WaterPurity.storedQuality(state)` on the way out,
and on the way in writes `WaterQuality.worse(held, poured)` into the state, the same rule pouring by
hand and rain already keep. The class is package-private, so the mixin targets it by name with
`@Mixin(targets = "...")`.

Two alternatives were looked at and rejected. A **data faucet interaction**
(`data/<ns>/faucet_interactions/*.json`) does take priority over the built-ins, since
`FaucetBehaviorsManager.apply` registers data interactions before its own, but `DataFluidInteraction`
matches an exact blockstate through a `RuleTest` and replaces it with a fixed one, so covering three
levels times six purity values means eighteen entries that still drain the cauldron in one go instead
of a layer at a time, and it cannot fill at all. Registering a replacement through
`addRegisterFaucetInteractions` does not work either: listeners run **after** the built-ins and the
faucet takes the first match.

Checked in the same script, with two faucets: a full dirty cauldron drained into an empty one arrives
as `water_cauldron[level=3,purity=1]`, dirty, and a pure cauldron poured into a dirty one leaves it
dirty, which is `WaterQuality.worse` doing what pouring by hand does.

### 10. Faucet on world water samples it

Found while building the four above, and a bug of the same family rather than a feature. A faucet
pointed at a water source block in the world offers plain water, which the tank then reads as
`defaultPurity`, so a faucet over a swamp fills a jar with Clean water where a bottle filled there by
hand comes out Dirty. Nothing here made that worse, but item 1 is what makes it stand out: every other
way into a jar now carries a real grade and this one does not.

The fix is `LiquidBlockInteraction`'s `getProvidedFluid`, stamped with `WaterPurity.sampleAt`. It needs
care rather than cleverness: **sampling must never land on a tick path**, and a faucet runs on one. It
takes the same answer as Create's pumps and Sophisticated's, a sample per position reused for 100
ticks, which is `SampledWater` in `src/main/neoforge` and again in `src/main/createfly`. A third copy
would have to live here, since neither of those is compiled by both loaders.

## The features

### 4. Drink from a jar or a goblet (done)

**Today you cannot drink water from either.** `SoftFluidTank.tryDrinkUpFluid` runs only when
`containsFood()`, which asks the entry for a `food` item, and `moonlight:water` has none. Only potions
and soups can be drunk. For a thirst mod this is the obvious feature: a goblet on a table and a jar in
a cellar should be somewhere water is kept and drunk.

`SoftFluidTankMixin` on `tryDrinkUpFluid`, `@At("HEAD")` and cancellable: if the tank holds water, drink
one serving through the mod's own path, so the grade decides what it restores and one roll drives nausea
and poison exactly as drinking by hand does, then shrink the tank by one and return true. Injecting at
the head is deliberate: it runs before the `containsFood` test, so no `food` entry is needed and nothing
Moonlight does for other fluids changes.

Both call sites are already gated by Supplementaries' own config (`JAR_EAT`, `GOBLET_DRINK`), so a
server that turned jar eating off keeps it off. Drinking from a goblet awards Supplementaries' own
`nether/goblet` advancement, which stays as it is.

Open question: **whether a jar of sea water can be drunk at all.** Drinking by hand lets a player drink
it and be punished for it, so the consistent answer is yes.

Checked in the same script: a goblet filled from a Pure bottle takes thirst from 4 to 10 and is left
empty. Drinking a jar or goblet of sea water, and what a Murky one gives, are left to the manual pass
for now.

### 5. Faucet fills and drains the hanging pot

The copper and iron hanging pots are the mod's own water block, and a faucet above one is the obvious
build. The pot keeps its quality in a blockstate with no block entity, so it is a `FaucetSource.BlState`
and a `FaucetTarget.BlState`, registered through `FaucetBehaviorsManager.addRegisterFaucetInteractions`.
Listener order does not matter here: no built-in claims this block, so ours is reached.

This is the **first item that needs an entry point**, because a listener has to be registered at init.
Items 1 to 4 are mixins only and need none. See the build notes below for what that costs.

Checked with `tools/agent/supplementaries-pot.jsonl`: a faucet fills a pot from a jar above it and
drains it into a cauldron below, both keeping the grade, and a pot holding Murky water refuses Clean.

### 6. The mod's own bowls as containers Moonlight knows

`moonlight:water`'s `containers` list is how Moonlight learns that an item is a water container. It
already names a handful of other mods' cups and buckets. Adding `thirstwastaken2:terracotta_bowl` to
`thirstwastaken2:terracotta_water_bowl` at `BOWL` capacity makes the mod's bowl work in a jar, a goblet
and a faucet the way a vanilla bottle does.

The waterskin is deliberately left out: it holds three servings with a fill level of its own, and
`FluidContainerList` maps one empty item to one filled item with a fixed capacity, which cannot express
it. On NeoForge the waterskin is already reachable through its fluid capability, so the faucet path
covers it there anyway.

This one is **data, not code**, but it is the same file as item 8 and has the same problem: the entry is
a whole registry object, so writing our own `data/moonlight/moonlight/soft_fluid/water.json` replaces
Moonlight's copy in full, which would silently drop every other mod's containers the day Moonlight adds
one, and which pack wins is not something to rely on. So: **do not ship an override.** Send item 8
upstream, and until it lands, add the bowl the same way item 1 adds the components, from the mixin.

### 7. The grade is visible

Three small pieces, none of them load bearing:

- the jar and goblet item tooltips, which hold their contents in a `SOFT_FLUID_CONTENT` component, show
  the grade line the mod's other containers show;
- the fluid in a placed jar or goblet is tinted by grade, so a jar of sea water is turquoise and a dirty
  one is brown, through Moonlight's soft fluid colour hook rather than by replacing textures;
- Jade shows the grade of a jar, goblet or hanging pot under the crosshair, next to what it already
  shows for world water and cauldrons.

The first two are client code and belong in `src/client/supplementaries`. Jade's part goes in the
existing `client/compat/JadeIntegration`, which already samples blocks the same way.

## Upstream and docs

### 8. Upstream: the two components in Moonlight's `water.json`

A pull request to Moonlight adding `thirstwastaken2:water_purity` and `thirstwastaken2:water_salty` to
`preserved_components_from_item`, and the terracotta bowl pair to `containers`. The file already carries
`thirst:purity` and seven other mods' containers, so this is a change the author has taken before.
`CodecUtils.lenientHomogeneousList` is what parses both fields, so entries for mods that are not
installed are skipped rather than failing the load.

If it is merged, the `getPreservedComponents` half of item 1 and all of item 6 can be deleted, and
everything else stays. If it is not, nothing is blocked. **Do not wait on it**, and do not make a
released version depend on a Moonlight release that does not exist yet.

### 9. Changelog and player docs

A CHANGELOG entry, a line on the installation page's compatible-mods list, and the Modrinth and
CurseForge pages, in the plain style `docs/AGENTS.md` and the `write-docs` skill ask for. The 2026-09
changelog line listing Supplementaries as an integration awaiting a compatible release needs updating
too: it becomes 1.21.1 only, both loaders.

## What the build needs

### Dependencies

Two new keys, in **both** 1.21.1 tables of `stonecutter.properties.toml`, since the whole integration is
gated on `deps.supplementaries` being set:

```toml
deps.supplementaries = "<Modrinth version id>"
deps.moonlight       = "<Modrinth version id>"
```

Both are **pinned by Modrinth version id, not version number**, for the reason AppleSkin is: the Fabric
and NeoForge uploads share one version number. For `1.21.1-3.9.9` and `1.21.1-3.6.5` those are
`h8FyJ2as` and `Grv7RNwq` on Fabric, `WrZWfRjP` and `yYx5Qs1i` on NeoForge, each with the version number
written in the comment above it, which `update_mc_deps.py` reads back. Both need adding to
`MODRINTH_DEPS` in `.github/scripts/update_mc_deps.py` with `by_id=True`, or the daily dependency PR
will never offer them.

Moonlight is named separately because Supplementaries' jar does not contain it, and every class this
integration touches is Moonlight's.

On Fabric both have to be **remapped mods** (`modCompileOnly`) rather than plain libraries the way
Create Fly is, since they are mixed into and a published Fabric jar is in intermediary names. That
brought one surprise: **Loom does not unpack a dependency's nested jars into a run**, which is why
Cloth Config is already named by hand for AppleSkin, and Moonlight bundles CodecUI and reads it on its
first line. CodecUI is published nowhere else, so it is taken out of Moonlight's own jar, while the
build is configured rather than by a task: Loom resolves the mod configurations then, and a jar written
afterwards is not there to be remapped.

### Source sets

```
src/main/supplementaries/java/com/thirstwastaken2/supplementaries/     both 1.21.1 nodes
  SupplementariesPresence     the gate: a classpath probe for each of the two mods
  SupplementariesMixinPlugin  applies each mixin only where its target is installed
  SoftFluidQuality            the only map between SoftFluidStack and WaterQuality
  SoftFluidDrinking           a serving out of a jar or a goblet
  CauldronQuality             what a faucet and a water cauldron say about the water between them
  mixin/SoftFluidMixin                  the two components onto water, for Moonlight's own conversions
  mixin/SoftFluidStackMixin             the mod's own stamping rules on every container a tank fills
  mixin/SoftFluidTankMixin              what goes into a tank, and drinking out of one
  mixin/WaterCauldronInteractionMixin   the faucet and the cauldron's purity
  HangingPotFaucet            item 5 only: the faucet source and target
src/main/supplementaries/resources/
  thirstwastaken2.supplementaries.mixins.json
src/client/supplementaries/java/...     item 7 only: tooltip lines and the fluid tint
```

`build.gradle.kts` and `build.neoforge.gradle.kts` each add these directories and append the mixin
config to the built manifest when `deps.supplementaries` is set, the way both already do for Create Fly
and Sophisticated. The Fabric side also needs the mods on the `runClient` classpath; the NeoForge side
the same through `clientRunMods`.

### The entry point, and why items 1 to 4 avoid it

A Fabric entrypoint class must implement `ModInitializer` and a NeoForge one must be annotated `@Mod`,
and both name their loader, so neither can live in a directory both loaders compile. Nor can
`src/main/java` call into this directory, since the nodes without Supplementaries would then not
compile.

**Items 1 to 4 are mixins only**, reached through the mixin config and the config plugin, so they need
no entry point at all. Item 5 is the first that does, and it costs two one-class directories,
`src/main/supplementaries-fabric` and `src/main/supplementaries-neoforge`, each a shim calling one
`init()` in the shared directory. That is the same shape the build already uses to pick
`neoforge-fluidhandler` or `neoforge-transfer`. Worth knowing before item 5 is scheduled, and a reason
to land 1 to 4 first.

### How it stays optional

The same three layers as `src/main/create` and `src/main/sophisticated`:

1. **Build.** Nothing is compiled and no mixin config is named unless `deps.supplementaries` is set, so
   the other eight nodes are untouched.
2. **Runtime gate.** `SupplementariesPresence` asks `Loader.isModLoaded` for both mod ids and then
   probes for `SoftFluidTank`, since Supplementaries can be present with a Moonlight too old to have
   it. A failed probe logs a warning and skips, rather than crashing on a missing mixin target.
3. **Mixin plugin.** `SupplementariesMixinPlugin` asks the gate before a single Moonlight class is
   named.

No hard dependency, ever, and the two 1.21.1 jars must behave identically with neither mod installed.

## Risks

- **This mixes into a library, not a mod.** Moonlight is a dependency of many mods, not only
  Supplementaries. The two mixins from item 1 change what every soft fluid tank does with water, so a
  jar from some other Moonlight mod inherits the behaviour too. That is the right answer for a thirst
  mod, but it needs saying out loud, and it is why the gate checks Moonlight rather than only
  Supplementaries.
- **Two moving targets.** Supplementaries and Moonlight release often and together, and Moonlight's
  1.21.1 branch carries `@Deprecated(forRemoval = true)` methods in exactly the classes this touches.
  Pinned ids and the runtime probe keep a crash from reaching a player, but this needs rebuilding more
  often than the Sophisticated integration does.
- **Loom remapping.** Mixins into a remapped mod on Fabric need that mod on the compile classpath for
  the refmap to resolve. It cost nothing in the end because every target is matched by name alone: the
  remapped jar keeps `method=["tryDrinkUpFluid"]` as written while the handler's own signature comes
  out in intermediary, which is exactly what is wanted. **A descriptor in a mixin annotation would put
  that back on the table**, so keep matching by name here.
- **The experimental settings prompt.** With these mods on the classpath a Fabric dev client calls
  every world experimental and stops in front of one, so `-Pquickplay` and `-Pagent` went nowhere until
  the driven client learned to press through it. That is `ClientWindow.passWorldPrompt`, and it is
  loader independent, so a NeoForge client that starts doing the same is already covered. Nothing about
  it reaches a player's game.
- **No gametest.** Gametests run without Supplementaries on the classpath, so every check above is an
  agent script in a real client, on both 1.21.1 nodes, as with Sophisticated and Farmer's Delight.
  `runGametest` must keep passing unchanged, which is itself the check that the mod is unaffected when
  the mods are absent.
- **Licence.** Supplementaries and Moonlight are under the Supplementaries Team License. Compiling
  against them and shipping nothing of theirs is fine; nothing of theirs may be bundled or
  redistributed, and the dependency stays a Modrinth coordinate.

## Not doing

- **Supplementaries' other blocks.** Soap, urns, ash, pancakes, sugar cubes and bamboo spikes have
  nothing to do with water quality, and Supplementaries registers no food or drink item that would want
  a value in `ThirstConfig`.
- **Purifying water inside a jar.** Purification is 27 cooking recipes over item stacks. A jar over a
  campfire is a different mechanism and would need its own balance pass; the distillation idea in
  `ROADMAP.md` is where that conversation belongs.
- **Lumisene and Supplementaries' other fluids.** Not water, no grade, nothing to keep.
- **Forge.** Supplementaries still publishes a Forge 1.21.1 jar. This mod does not, and that does not
  change.
