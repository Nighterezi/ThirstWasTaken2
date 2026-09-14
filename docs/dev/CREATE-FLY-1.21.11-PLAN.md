# Create Fly on 1.21.11

How the Sand Filter gets from 26.1.x and 26.2.x to 1.21.11. A working plan with an end state: delete it
once 1.21.11 ships the Sand Filter, and fold anything that outlives it into
[src/main/createfly/AGENTS.md](../../src/main/createfly/AGENTS.md).

**Status: paused.** Nothing below has been started.

## Where things stand

- `26.1.x` and `26.2.x` set `deps.create_fly` and compile `src/main/createfly` and
  `src/client/createfly` from one body of code, with no version branch.
- Those nodes compile against `createFlyClasses`, a copy of Create Fly's `com/zurrtum/**` classes with
  its `fabric.mod.json` and class tweaker left out. Compiling against the real jar, with either
  `compileOnly` or `modCompileOnly`, makes Loom bake Create Fly's injected `ContainerExtension`
  interface into the Minecraft jar every run shares, and `runGametest` crashes without Create Fly.
  Both were tried.
- `1.21.1` has no Create Fly release and stays out of scope.

## Why 1.21.11 is different

| | 26.x | 1.21.11 |
|---|---|---|
| Create Fly artifact | `26.2-rc-2-6.0.9-1`, `26.1.2-6.0.9-4` | `1.21.11-6.0.9-5`, built from the `1.21.11_obfuscation` branch |
| Names in the jar | Mojang, unobfuscated | Intermediary, remapped at release |
| How it injects `ContainerExtension` | `transitive-inject-interface` in `create.classtweaker` | `custom.loom:injected_interfaces` in `fabric.mod.json`; `create.accesswidener` injects nothing |
| Loom on our node | no remapping | remaps `mod*` configurations to Mojang names |

A classes-only copy cannot work on 1.21.11: without `fabric.mod.json` Loom does not remap it, and our
code compiles against Mojang names that an intermediary jar does not have.

The `1.21.11_obfuscation` branch has every class the integration touches, in the same packages:
`GenericItemFilling`, `GenericItemEmptying`, `OpenEndedPipe`, `FluidDrainingBehaviour`,
`SmartFluidTankBehaviour`, `FluidInventoryProvider`, `BucketFluidInventory`, `ComparatorUtil`,
`AllBlockEntityBehaviours`, `IHaveGoggleInformation`, `TooltipBehaviour`, `CreateLang`. Their
signatures have not been checked.

## Plan

### 1. A remappable copy without the injection

Replace `createFlyClasses` with a task that, per node, keeps Create Fly a mod but removes what leaks
into the shared Minecraft jar:

- copy the whole jar;
- from `fabric.mod.json`, delete `custom."loom:injected_interfaces"` and the `accessWidener` or
  `classTweaker` entry;
- leave out the tweaker file itself.

Feed it to `modCompileOnly(files(...))` and `modClientCompileOnly(files(...))`, so Loom remaps it on
1.21.11 and passes it through unchanged on 26.x. Using one task for every node keeps a single answer to
"how do we compile against Create Fly". If Loom still processes something else from the jar, fall back
to the classes-only copy on 26.x and use this task only below 26.1.

`modClientRuntimeOnly` keeps the real jar, so `runClient` loads Create Fly with its injection applied by
Fabric Loader.

**Check:** `":1.21.11:runGametest"` passes with 124 tests and no `NoClassDefFoundError`, and
`":26.2.x:runGametest"` and `":26.1.x:runGametest"` still do.

### 2. Enable the node

Add `deps.create_fly = "1.21.11-6.0.9-5"` under `["1.21.11"]` in `stonecutter.properties.toml`, then run
`":1.21.11:compileClientJava"`.

Expected breakage, handled with `//?` blocks inside `src/main/createfly` (allowed there, since
`checkVersionSeam` does not cover it):

- `Block#getAnalogOutputSignal` and `isPathfindable` signatures;
- `Item.Properties#useBlockDescriptionPrefix` and `BlockBehaviour.Properties#setId`;
- `CreativeModeTabEvents`, which Fabric API may still call by its older name on 1.21.11;
- Create Fly's own `SmartBlockEntity`, `ValueInput`/`ValueOutput` use and `CreateLang` builders.

Prefer a Stonecutter `replacements` rule for a pure rename over a `//?` block.

### 3. Mixin targets

Compare the four mixin targets in the 1.21.11 jar with `javap` after remapping:
`GenericItemFilling#fillItem`, `GenericItemEmptying#emptyItem`,
`OpenEndedPipe#removeFluidFromSpace` with fields `world` and `outputPos`, and
`FluidDrainingBehaviour#getDrainableFluid`. A mismatch only fails at runtime, when the class loads.

### 4. Data and assets

The recipe, unlock advancement, loot table and item model definition are hand-written for 26.x.
Check each against 1.21.11's formats: the `#c:sands` ingredient string, `items/sand_filter.json` item
model definitions (1.21.4+), and loot table field names. If one differs, split it by Minecraft version
the way `src/main/generated/<version>/` does.

### 5. In-game test on `":1.21.11:runClient"`

The same checks as 26.1.x, listed under Testing in
[src/main/createfly/AGENTS.md](../../src/main/createfly/AGENTS.md): filter grade, cauldron through a
pump, Hose Pulley, Item Drain, Spout over a Depot with a gap, Basin in survival, goggles.

### 6. Documentation

`CHANGELOG.md`, `README.md` (the Create Fly row and Known limitations), `FORK-STATUS.md`,
`docs/docs/features/create.md` (the version warning), `docs/docs/installation.md`, `docs/docs/faq.md`,
`docs/docs/features/water-purity.md`, `docs/MODRINTH.md`.

## Alternative considered

`modImplementation` for Create Fly, as its Modrinth page suggests. It solves remapping and the crash in
one line, because Create Fly is then present at runtime. It is rejected for now because every run,
gametests included, would load Create Fly: the gametests would stop proving that the mod works without
Create, and startup and benchmark numbers would include Create's cost.
