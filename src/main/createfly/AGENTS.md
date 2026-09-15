# src/main/createfly — the Create Fly Sand Filter

The Sand Filter from the original mod's Create integration, rebuilt on
[Create Fly](https://modrinth.com/mod/create-fly), the Fabric port of Create. Water pumped in at the
top comes out of the bottom one grade cleaner.

This directory, and `src/client/createfly`, are **only compiled by nodes that set `deps.create_fly`**
in `stonecutter.properties.toml`. Create Fly has no release for every Minecraft version the mod
supports (none at all for 1.21.1), so the integration cannot live in `src/main/java`. Today
`26.1.x` and `26.2.x` set it, and one body of code builds for both without a version branch.

```
java/com/thirstwastaken2/createfly/
  CreateFlyPresence       the gate: mod id plus a marker class, read as a resource
  CreateFlyEntrypoint     thirstwastaken2:createfly, run by ThirstWasTaken2Fabric
  CreateFlyMixinPlugin    applies the mixins below only when the gate passes
  SandFilter              block, item, block entity type, creative tab entry
  SandFilterBlock         FluidInventoryProvider: UP is the input, DOWN the output
  SandFilterBlockEntity   two one-bucket tanks and the transfer between them
  WaterFluids             water quality on Create's FluidStack
  SampledWater            per-pump cache of WaterPurity.sampleAt
  mixin/                  quality through Create's item and world fluid transfers
resources/
  thirstwastaken2.createfly.mixins.json
  assets/…                blockstate, Blockbench model, texture, item definition
  data/…                  recipe, recipe unlock, loot table, pickaxe tag
../../client/createfly/java/com/thirstwastaken2/client/createfly/
  CreateFlyClientEntrypoint, SandFilterClient, SandFilterTooltipBehaviour (goggles)
```

## How it stays optional

Three layers, all required:

1. **Build.** `build.gradle.kts` adds these source directories, and adds the two entrypoints and the
   mixin config to the built `fabric.mod.json`, only when `deps.create_fly` is set. The source
   manifest never names them, so every other node ships a jar that knows nothing about Create.
2. **Runtime gate.** `ThirstWasTaken2Fabric` runs every `thirstwastaken2:createfly` entrypoint, and
   `CreateFlyEntrypoint` touches `SandFilter` only after `CreateFlyPresence.isPresent()`. The mod id
   alone is not enough, since any mod may claim `create`. `CreateFlyPresence` names no Create class
   and no Minecraft class, because the mixin plugin calls it before Minecraft's classes may load.
3. **Data.** The recipe, its unlock advancement and the loot table each carry a
   `fabric:all_mods_loaded` condition, so a server without Create skips them instead of logging an
   unknown item. The blockstate and model for a block that was never registered are simply unused.

**Nothing outside this directory may reference a class in it**, other than through the entrypoint
names.

## Compiling against Create Fly without becoming it

Create Fly's class tweaker makes vanilla's `Container` implement `ContainerExtension`. Loom bakes the
tweakers of every mod on the compile classpath into the single Minecraft jar that all of a node's runs
share, so compiling against the Create Fly jar as a mod made `runGametest` and `runServer`, which run
without Create Fly, crash on `Container`. Turning `enableTransitiveAccessWideners` off is not an
option: Fabric API's injected methods (`getAttachedOrCreate`, creative tab `Output`) need it.

So `createFlyClasses` copies only `com/zurrtum/**` out of the jar into a plain library that Loom
does not treat as a mod, and both source sets compile against that. `runClient` still gets the real
Create Fly jar through `clientRuntimeOnly`, where Fabric Loader applies its tweaker itself.

That copy is unremapped, which is fine from 26.1 on. It also leaves out the members Create Fly widens
for itself, which is why `SandFilter` uses Fabric's `FabricBlockEntityTypeBuilder`: vanilla's
`BlockEntityType` constructor is private on 26.1. **1.21.11 and 1.21.1 will not get the Sand
Filter.** Both are obfuscated: Create Fly's 1.21.11 jar is in intermediary names, so a classes-only
copy would not remap, and compiling against the real jar brings back the `Container` crash above.
Decided on 2026-09-15; do not set `deps.create_fly` on an obfuscated node.

## The transfer

`SandFilterBlockEntity.tick` moves up to 10 mB a tick (a bucket every five seconds, the original's
default) from the input tank to the output tank and raises its grade by one. Two things about Create
Fly that are easy to get wrong, and that the first version of this block did get wrong:

- **Amounts are droplets, 81 to the millibucket.** `BucketFluidInventory.CAPACITY` is 81000. A tank
  of "1000" holds twelve millibuckets.
- **A tank's insertion and extraction flags apply to its capability, not to its `TankSegment`s.** The
  input forbids extraction and the output forbids insertion, which is what makes pipes respect the
  direction. The transfer therefore goes through `getPrimaryHandler()`. Going through
  `getCapability()` moves nothing at all, which is why the Sand Filter was removed in 1.0.2.

The tick builds nothing in the steady state. The filtered form of the input is built once per input
stack and cached, and the output is compared against it once per stack that lands there, because a tank
merging more of the same water keeps its stack object and only changes the amount. The amounts are then
moved directly on the two `TankSegment`s. A full or mismatched output returns before any of that, so a
blocked filter costs next to nothing. `./gradlew ":26.2.x:runBenchmark" -Pcreate` measures all three
states; see [src/dev/java/AGENTS.md](../dev/java/AGENTS.md).

The input tank only accepts water (`WaterOnlyHandler`): nothing drains it, so anything else would sit
there forever. Sea water passes through unchanged, because sand does not take salt out.

Pipes find the tanks by side: `UP` is the input and `DOWN` the output, because Create asks with the
face of the filter the pipe touches. Create decides whether a pipe *connects* without asking for a
side (`FluidHelper.hasFluidInventory` is true for any `FluidInventoryProvider`), so a pipe beside the
filter still bends towards it; it just finds no tank there and nothing flows. A `null` side is Create
acting for a player's hand and gets the output. The comparator reads the output tank; Create's own
helper only understands single-tank blocks and would always read zero.

## Water quality through Create

Create's fluid stacks carry data components like item stacks. `WaterFluids` keeps exactly **one**
quality component on a fluid (`water_purity` for a grade, `water_salty` for sea water), so two stacks
of the same water compare equal and can share a tank or pipe. Items get both components back from
`WaterPurity.setQuality` on the way out, which the cooking recipes need.

Water of different grades cannot share a tank. That is Create's rule for any two fluids with
different components, and it means a pipe network fed from two differently graded sources stalls
until one tank drains. Unstamped water, from a creative tank or another mod, reads as
`defaultPurity`, but it is not equal to water stamped with that grade.

| Mixin | Target | Why |
|---|---|---|
| `GenericItemFillingMixin` | `fillItem` | Spout and hand filling. Reads the fluid first, because filling spends it |
| `GenericItemEmptyingMixin` | `emptyItem` | Item Drain and hand pouring. Reads the item first, because emptying shrinks it; stamps a copy, because an emptying recipe hands out its own stack |
| `OpenEndedPipeMixin` | `removeFluidFromSpace` | A pump drawing from the world or a cauldron. Samples before draining, since a drained cauldron has lost its quality |
| `FluidDrainingBehaviourMixin` | `getDrainableFluid` | A Hose Pulley, graded where the hose ends, as the original mod did |

`OpenEndedPipeMixin` samples at `HEAD` into a field and stamps at `RETURN` instead of wrapping the
method: Create calls it on every flow check, and a wrapper allocates its operation object each time.
The two item mixins keep `@WrapMethod`, because they run once per filled or emptied item and have to
read their input before the call spends it.

The last two collect water on a tick, which is exactly where `purity/AGENTS.md` says the
neighbourhood scan must not run. `SampledWater` is the exception it allows: one cached sample per
pump, reused for 100 ticks. A cauldron's stored quality is only a blockstate read and is never cached,
and a block with no water is never sampled.

## Testing

The gametests run without Create Fly and cannot reach any of this; what they do prove is that the
node still loads without it. Check by hand with `./gradlew ":26.2.x:runClient"` or `":26.1.x:runClient"`:

- a pump from a plains pool into the top of a filter, and a second pump from its bottom into a
  Spout: `/data get block` on the filter shows the input one grade below the output;
- pouring a graded bottle into a Basin and filling a glass bottle back out, in survival (Create does
  not hand the filled bottle back in creative): the bottle keeps its grade and `water_salty: false`;
- a water cauldron with a set grade drained by a pump into a filter: the output is that grade plus one;
- a Hose Pulley over a pool pumping into a Fluid Tank: the tank holds the pool's sampled grade. A
  creative motor placed by `/setblock` turns the pulley the retracting way; place it with
  `{ScrollValue:-16}` instead;
- pouring a graded bottle into an Item Drain;
- a Spout over a Depot fills a glass bottle with the filtered grade. The Depot has to sit one block
  below the Spout, with a gap;
- Engineer's Goggles on the filter show both tanks with their grade.

Keep pipes of the two networks apart: a pipe beside another pipe joins it, and the filter's input and
output then become one network.
