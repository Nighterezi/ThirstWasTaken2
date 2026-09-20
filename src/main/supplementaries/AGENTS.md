# src/main/supplementaries — water quality in Moonlight's soft fluids

[Supplementaries](https://modrinth.com/mod/supplementaries) keeps water in **soft fluids**, a system of
its required library [Moonlight Lib](https://modrinth.com/mod/moonlight), so a jar, a goblet and a
faucet all move water through the same three classes. Everything here targets those, which is why one
directory covers both of Supplementaries' blocks and its faucet, and why the only class of
Supplementaries' own that is touched is the faucet's behaviour for vanilla cauldrons.

Today it fixes three ways water lost its quality and adds one thing Supplementaries cannot do alone:

- **a jar or a goblet keeps the grade**: a dirty bottle poured in comes back out dirty, and a jar of
  Murky water will not take Clean water. Without it everything came back out plain, which reads as
  `defaultPurity`, so two decorative blocks purified water for free;
- **sea water stays sea water**: Moonlight converts a water bottle to its own water fluid, so without
  the salt flag a jar turned the sea drinkable;
- **a faucet keeps what a cauldron holds**, both when it drains one and when it fills one, and a
  cauldron poured into keeps the worse of the two, exactly as pouring a container in by hand does;
- **a jar or a goblet of water can be drunk**, which Supplementaries cannot do on its own: Moonlight
  only drinks a fluid that names a food item, and `moonlight:water` names none.

What is still to do is in
[docs/dev/SUPPLEMENTARIES-INTEGRATION.md](../../../docs/dev/SUPPLEMENTARIES-INTEGRATION.md).

This directory is **only compiled by nodes that set `deps.supplementaries`** in
`stonecutter.properties.toml`, which is `1.21.1` and `1.21.1-neoforge` and nothing else:
Supplementaries has no release for any newer Minecraft version.

**It is the one optional integration both loaders compile.** Moonlight is multi-loader, so
`SoftFluid`, `SoftFluidStack` and `SoftFluidTank` are the same classes with the same signatures on
Fabric and NeoForge, and since it is one Minecraft version there is no Stonecutter branch either.
Nothing here may name a loader.

```
supplementaries/java/com/thirstwastaken2/supplementaries/     both 1.21.1 nodes
  SupplementariesPresence      the gate: a classpath probe for Moonlight, and one for Supplementaries
  SupplementariesMixinPlugin   applies each mixin only where its target is installed
  SoftFluidQuality             the only map between a SoftFluidStack and a WaterQuality
  SoftFluidDrinking            a serving out of a jar or a goblet, drunk as the bottle it would fill
  CauldronQuality              what a faucet and a vanilla water cauldron say about the water between them
  mixin/SoftFluidMixin                  the two components onto water, for the conversions Moonlight makes itself
  mixin/SoftFluidStackMixin             the mod's own stamping rules on every container a tank fills
  mixin/SoftFluidTankMixin              what goes into a tank, and drinking out of one
  mixin/WaterCauldronInteractionMixin   the faucet's cauldron behaviour, the one Supplementaries class
supplementaries/resources/
  thirstwastaken2.supplementaries.mixins.json
```

## How a grade moves

A `SoftFluidStack` carries data components the way an `ItemStack` does, so the grade lives on the
fluid itself. It keeps **exactly one** of `water_purity` and `water_salty`, the rule `WaterFluids`
already keeps for NeoForge fluid stacks, because Moonlight refuses to mix two stacks whose components
differ: that is what stops a jar of Murky water from quietly taking Clean water, and it only works if
water of one grade always looks identical.

Four hooks, and each is there for a reason the others do not cover:

| Hook | Does |
|---|---|
| `SoftFluidTank.addFluid` | stamps water entering a tank with the grade it already reads as, before the tank decides whether it fits. Gradeless water would otherwise refuse to share a tank with water that has a grade, although both read the same everywhere else |
| `SoftFluidStack.splitToItem` | runs the container a tank just filled through `WaterPurity.setQuality`. Moonlight builds that stack from scratch, so it needs the mod's own rules: `water_salty: false` on fresh water, which every purification recipe matches on, and the sprite that says a bottle is sea water |
| `SoftFluid.getPreservedComponents` | adds the two components to what Moonlight copies of its own accord. `moonlight:water` lists another thirst mod's component, not this one's. This is what carries the grade onto a **NeoForge fluid stack** and back: a jar is an `IFluidHandler` there, so a pipe or a faucet pointed at a tank moves its water through one |
| `WaterCauldronInteraction` | a cauldron keeps its quality in a blockstate, not a component, so no soft fluid hook reaches it |

- **The quality is read before the call, never after.** `splitToItem` spends the serving, and a stack
  spent to nothing reads as empty, so the grade is taken first and applied to what comes back. The
  same shape as `GenericItemFillingMixin` in `src/main/createfly`.
- **`getPreservedComponents` is answered once per registry entry**, cached in a `@Unique` field,
  because a faucet asks on every transfer.
- **Water is recognised by key, not by tag**, `stack.is(MLBuiltinSoftFluids.WATER)`, everywhere a
  stack is in hand. The one hook that has only the registry entry asks whether its vanilla fluid is
  water, since a `SoftFluid` does not know its own key.
- **Unstamped water is `defaultPurity`**, the same fallback every unstamped container already has, and
  it is made explicit on the way into a tank so that everything in a tank compares equal by grade.

## Drinking

`SoftFluidTank.tryDrinkUpFluid` only runs when the fluid names a food item, and water names none, so
the mixin is at `@At("HEAD")` and cancellable: it answers for water before that test and leaves every
other fluid to Moonlight. A serving is drunk as **the water bottle it would have been poured into**,
through `ThirstManager.drinkItem`, so the grade decides what it restores, one roll drives the nausea
and poison it may bring, and the advancement counts, exactly as for a bottle finished by hand. A full
thirst bar refuses it, the same rule as drinking that bottle.

The work is server side only. Both call sites hand the client a success so the arm swings and sync
their block entity afterwards, so the client has nothing to do but agree.

## How it stays optional

The same three layers as [src/main/create](../create/AGENTS.md) and
[src/main/sophisticated](../sophisticated/AGENTS.md).

1. **Build.** `build.gradle.kts` and `build.neoforge.gradle.kts` each add this directory, and append
   the mixin config to the built manifest with the two mods as optional dependencies, only when
   `deps.supplementaries` is set. Fabric needs Loom to remap both mods, so they are `modCompileOnly`
   rather than plain libraries; Loom does not unpack a dependency's nested jars into a run, so
   Moonlight's own CodecUI is taken out of its jar while the build configures. It has to be done then
   and not by a task: Loom resolves the mod configurations while configuring, and a jar written
   afterwards is not there to be remapped.
2. **Runtime gate.** `SupplementariesPresence` probes the classpath for Moonlight's `SoftFluidTank`
   and for Supplementaries' `WaterCauldronInteraction`, as resources, which never loads a class. It
   names no loader, unlike the gates of the other two integrations, because both loaders compile it.
   A mod of a version that moved either class is skipped with a warning rather than crashing on a
   missing mixin target.
3. **Mixin plugin.** `SupplementariesMixinPlugin` asks the gate per mixin: Moonlight for the three
   soft fluid ones, Supplementaries itself for the cauldron one, since other mods ship Moonlight
   without Supplementaries.

## Checking it

The gametests run without either mod, which is what proves the mod is unchanged when they are absent;
`runGametest` passing on both 1.21.1 nodes is that check. What the integration does is checked in a
real client, on both nodes, with
[tools/agent/supplementaries.jsonl](../../../tools/agent/supplementaries.jsonl):

```bash
./gradlew ":1.21.1:runClient" -Pagent=tools/agent/supplementaries.jsonl -Pquickplay=SupplementariesAgent
```

It pours a dirty bottle and a salty one through a jar and draws them back, pours a pure bottle into a
goblet and drinks it, and runs two faucets between cauldrons. Both nodes answer the same: the grade is
on the fluid in the block, the bottle drawn back out still carries it, thirst goes from 4 to 10 out of
the goblet, and a dirty cauldron drained through a faucet arrives dirty while pure water poured into a
dirty cauldron leaves it dirty.

The script addresses the player by selector: a Fabric dev client names its player `Player<NN>` and a
NeoForge one names it `Dev`.
