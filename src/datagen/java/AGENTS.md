# src/datagen — the generators for every datapack and asset JSON

Every recipe, advancement, tag, damage type, item model and model definition the mod ships is
written by the code in this directory. Nothing under `src/main/generated/` is edited by hand.

```bash
./gradlew ":26.2.x:runDatagen"
```

That rewrites `src/main/generated/<minecraft version>/` in place. Commit what it changes.

```bash
./gradlew ":26.2.x:checkDatagen"
```

That regenerates and then fails if the result differs from what is committed, which is what catches
a generator edited without regenerating, or a generated file edited by hand. It asks `git status`,
so it reports files that are merely staged as changed too; commit before reading its verdict.

## How it is wired

This is an ordinary Gradle source set, like the gametests and the dev tools, declared by
`fabricApi.configureDataGeneration` in `build.gradle.kts`. `thirstwastaken2-datagen` is its own small
mod (`src/datagen/resources/fabric.mod.json`), so none of it can reach a published jar, and
`ThirstDatagen.getEffectiveModId` is what makes everything it writes land in the `thirstwastaken2`
namespace rather than its own.

It runs as a **client**, because the item model providers live in `net.minecraft.client.data`. Strict
validation is off: every advancement the mod awards by id has a single `minecraft:impossible`
criterion, which strict validation reads as unreachable.

**The output directory is keyed by Minecraft version, not by build node**, and is a resource root of
`main`, so the jar picks it up with no further wiring. Two nodes on the same Minecraft version
produce byte-identical files and are meant to share one directory — that is what P2 needs when
NeoForge arrives. The `.cache/` beside the output is datagen's own hash cache and is gitignored.

## Why the three versions produce different bytes

The generators are one body of code; the serializers are not. A field whose value equals its codec's
default is omitted, and which fields have defaults changed between versions, so the same generator
writes `"category": "misc"` on 26.1 and omits it on 26.2. None of that is a difference in behaviour:
the codec that omits a field on write supplies the same value on read.

Two of these are worth knowing before reading a diff and thinking something broke:

- **`cookingtime` is omitted when it is the default**, which is 200 for smelting and 100 for
  campfire cooking. The campfire recipes are 600, so they always write it; the smelting ones are 200,
  so on 26.2 they never do.
- **Before 26.1 a recipe result is a live `ItemStack`**, whose components serialize as the delta from
  the item's own defaults. `ThirstItems.TERRACOTTA_WATER_BOWL` defaults to grade 3, fresh, custom
  model data `[0, 3]`, so on 1.21.11 a bowl result that sets exactly those writes no components at
  all. The stack the furnace hands out is the same either way. 26.1's `ItemStackTemplate` records the
  patch verbatim, which is why the newer nodes spell it out.

## Version conditionals

The same discipline as `src/main`, and the same threshold syntax. Two kinds of difference show up
here, and they are handled differently:

- **A pure rename** goes in `replacements` in `stonecutter.gradle.kts`, not in a `//?` block. The
  Fabric data generation API renamed `FabricDataOutput` to `FabricPackOutput` and `FabricTagProvider`
  to `FabricTagsProvider` for 26.1, and 26.2 moved the advancement trigger classes out of
  `net.minecraft.advancements` into `triggers` and `predicates` without renaming one of them. The
  code names the newest spelling and the replacements supply the older one.
- **A shape change** gets a `//?` block. There are seven, all for 1.21.11: six in
  `ThirstRecipeProvider` for the result type and the cooking recipe constructors, and one in
  `ThirstModelProvider` for the texture wrapper.

`builder(TagKey)` rather than `tag(TagKey)` is deliberate — it is Fabric's, exists on all three
versions, and needs no conditional. Vanilla's `tag` only exists from 26.2.

## The providers

| Provider | Writes |
|---|---|
| `ThirstRecipeProvider` | `data/…/recipe/`, and the `advancement/recipes/misc/` unlocks with them |
| `ThirstAdvancementProvider` | `data/…/advancement/`, the mod's own tab |
| `ThirstDamageTypeProvider` | `data/…/damage_type/dehydrate.json` |
| `ThirstDamageTypeTagProvider` | `data/minecraft/tags/damage_type/bypasses_armor.json` |
| `ThirstBiomeTagProvider` | `data/…/tags/worldgen/biome/stagnant_water.json` |
| `ThirstModelProvider` | `assets/…/models/item/`, and the definitions for the mod's own items |
| `ThirstItemModelDefinitionProvider` | the two definitions in `assets/…/items/` that have no item |

A new provider has to be added to `ThirstDatagen.onInitializeDataGenerator` or it never runs, and
nothing fails to tell you so.

## What is not generated

Textures, `icon.png`, `font/droplets.json`, the nine `lang/` files, `fabric.mod.json` and
`thirstwastaken2.mixins.json` all stay hand-written in `src/main/resources`. The lang files
deliberately so: eight of the nine are translations, and `en_us` is edited alongside them.

## Two rules that outlive this file

- **Renaming an advancement means renaming the constant in `ThirstAdvancements` with it**, or the
  advancement silently stops being awarded. `AdvancementGameTest` is what catches that.
- **The purity table lives in `ThirstRecipeProvider.PURIFY_TABLE` and nowhere else.** The Java side
  has no idea the purification recipes exist; changing the table changes all eighteen files at once,
  which is the whole reason they are generated.
