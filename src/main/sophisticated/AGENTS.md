# src/main/sophisticated — Sophisticated Core's upgrades

[Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks) keeps its upgrades in
Sophisticated Core, which Sophisticated Storage shares, so everything here targets Core and works for
both. Today it covers two upgrades:

- the **Tank upgrade** keeps water's grade: a dirty bucket poured in comes back out as dirty bottles,
  and sea water stays sea water. Without it the tank handed out plain water, which the mod reads as
  `defaultPurity`, so any water went in and Clean water came out;
- the **Feeding upgrade** restores thirst for what it feeds. Without it a melon fed from a backpack
  filled the hunger bar and left the thirst bar where it was.

What is still to do across Sophisticated's upgrades is in
[docs/dev/SOPHISTICATED-INTEGRATION.md](../../../docs/dev/SOPHISTICATED-INTEGRATION.md).

This directory is **only compiled by nodes that set `deps.sophisticated_core`** in
`stonecutter.properties.toml`. Today that is `1.21.1-neoforge`. From 1.21.11 Sophisticated Core's
tanks move fluid through NeoForge's transfer API (`ResourceHandler<FluidResource>`, transactions)
instead of `IFluidHandler`, so those nodes need an implementation of their own rather than a version
branch in this one.

```
java/com/thirstwastaken2/sophisticated/
  SophisticatedPresence      the gate: FML's mod file for `sophisticatedcore`, and a marker class inside it
  SophisticatedMixinPlugin   applies the mixins below only when the gate passes
  WaterQualityFluidHandler   a container's IFluidHandlerItem with the grade carried across it
  mixin/TankUpgradeWrapperMixin     wraps the one method the Tank upgrade finds container handlers through
  mixin/FeedingUpgradeWrapperMixin  hands out thirst where the Feeding upgrade finishes eating
resources/
  thirstwastaken2.sophisticated.mixins.json
```

## How it stays optional

The same three layers as [src/main/create](../create/AGENTS.md), minus data: nothing is registered,
so there is no entrypoint and no recipe.

1. **Build.** `build.neoforge.gradle.kts` adds these directories, and appends the mixin config and an
   optional `sophisticatedcore` dependency to the built `neoforge.mods.toml`, only when
   `deps.sophisticated_core` is set.
2. **Runtime gate.** `SophisticatedPresence` looks for the Tank upgrade's `SwapEmptyFluidContainerHandler`
   inside Core's own jar, a class only the `IFluidHandler` generation has, so a Core the mixin would not
   fit is skipped with a warning instead of crashing on a missing target.

**Nothing outside this directory may reference a class in it.**

## The Tank upgrade

`TankUpgradeWrapper.getFluidHandler(ItemStack)` is the only way the tank reaches a container, from its
input and output slots and from the cursor, so one `@WrapMethod` covers every transfer. The handlers
it returns know nothing of quality: NeoForge's bucket wrapper takes and gives plain water, and Core's
own bottle handler only matches a water bottle with no components at all. `WaterQualityFluidHandler`:

- looks a stamped container up as its `WaterPurity.unstamped` copy, so Core recognises it;
- stamps water leaving the container with the grade it held, and passes water entering it down plain;
- hands out a **stamped copy** from `getContainer`, never the delegate's own stack. Core's bottle
  handler drains only while its container still equals a plain bottle, and the tank ignores a drain
  that returns nothing after it has already filled itself, so stamping in place poured the last bottle
  in forever. That is what the salt case of the agent check below watches.

The fluid in the tank keeps the one-component rule of `WaterFluids` (shared with Create, in
`src/main/neoforge`), so water of two grades never shares a tank: Core compares with
`isSameFluidSameComponents`. A container whose water does not match the tank is refused, and Core
moves it on to the result slot, as it does with lava offered to a water tank. Water with no quality at
all, from a creative tank or another mod, still counts as `defaultPurity` but is not equal to water
stamped with that grade, the same as in Create.

A backpack's tank can also be filled or drained through the backpack's own fluid capability, by a pipe
for instance. Those transfers carry `FluidStack`s, which keep their components on their own.

## The Feeding upgrade

The mod hands out thirst for everything eaten or drunk at the head of `ItemStack.finishUsingItem`
(`ItemStackMixin`). `FeedingUpgradeWrapper.tryFeedingStack` calls `Item.finishUsingItem` directly, past
that hook, so `FeedingUpgradeWrapperMixin` wraps that one call and runs `ThirstManager.drinkItem` just
before it, on the server, which is the same point in the same order. Nothing else hands out thirst for
eating, NeoForge's `LivingEntityUseItemEvent.Finish` included, so nothing is counted twice.

The upgrade still decides *when* to feed by the hunger bar alone. Feeding because the player is thirsty
is a different upgrade; see the plan linked above.

## Testing

The gametests run without Sophisticated and prove the node still loads without it. The upgrades are
checked in a real client, from backpack templates in `tools/agent/sophisticated-pack`. Both scripts
say how to set the world up.

### Tank

[tools/agent/sophisticated-tank.jsonl](../../../tools/agent/sophisticated-tank.jsonl) builds four
backpacks from the templates in `tools/agent/sophisticated-pack`, lets each tank run in the main
hand, and exports what is left as SNBT. The file says how to set up the world and what each export has
to show. Checked on 2026-09-19, and once with the mixin disabled for comparison:

| Case | With the mixin | Without |
|---|---|---|
| purity-0 bucket, bottled | tank and bottle `water_purity: 0` | plain water, so the bottle reads Clean |
| four sea-water bottles, one bucket | a bucket with `water_salty: true`, four glass bottles, empty tank | the bottles are refused |
| dirty bottle into a Pure tank | refused, the tank keeps 250 mB of Pure | refused |
| unstamped bucket, bottled | tank and bottle `water_purity: 2` | plain water |

Not checked yet: the cursor path (clicking a container onto the tank in the GUI), which reaches the
same method, and Sophisticated Storage.

### Feeding

[tools/agent/sophisticated-feeding.jsonl](../../../tools/agent/sophisticated-feeding.jsonl) starves a
player to food 6, sets thirst to 4, and hands them a backpack with a Feeding upgrade and 16 melon
slices. Checked on 2026-09-19: seven slices were eaten, food went to 20 and thirst to 20. With the
mixin left out of the config, food went to 20 and thirst stayed at 4.

A world whose `level.dat` came from the Farmer's Delight check carries Nourishment on its player, which
cancels all exhaustion, so the script clears the player's effects first.
