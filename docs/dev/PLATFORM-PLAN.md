# Platform plan

How ThirstWasTaken2 gets from three Fabric jars to the target matrix of Minecraft versions and mod
loaders, in what order, and what has to be true before each step starts. This is a working plan with
an end state, not a standing rule: delete it once the last phase lands, and promote anything that
outlives it to [AGENTS.md](../../AGENTS.md).

For gameplay ideas see [ROADMAP.md](ROADMAP.md). That file is open ended; this one finishes.

## The target

| Minecraft | Fabric | NeoForge |
|---|---|---|
| 26.2 | yes | yes |
| 26.1.x | yes | yes |
| 1.21.11 | yes | yes |
| 1.21.1 | yes, range `>=1.21 <=1.21.1` | yes, 1.21.1 only |
| 1.21 | covered by the node above | **no** |

Seven build nodes. 1.21 and 1.21.1 share one Fabric jar because nothing in the API surface moved
between them, but they are separate NeoForge generations (21.0 against 21.1), so a shared NeoForge
jar is not on the table. Almost nobody stayed on 1.21 once 1.21.1 landed three weeks later, so the
node is not worth its cost.

## Settled decisions

**One branch.** Git branches are for divergence in time; Stonecutter is for divergence in target.
All seven nodes ship the same feature set under the same version number, so they belong to one
trunk. Short lived feature branches and PRs stay exactly as they are. Revisit only if 1.21.1 stops
receiving features, and prefer retiring it over branching it.

**Loader axis is source sets, not comments.** `src/fabric/java` and `src/neoforge/java`, selected by
a per node buildscript. Loader specific code stays real Java that the IDE compiles and refactors.
The alternative, commenting out the inactive loader the way
[rotgruengelb/stonecutter-mod-template](https://github.com/rotgruengelb/stonecutter-mod-template)
does, is fine for its 495 line example and wrong at this size.

**Version axis stays Stonecutter comments.** It works today and the discipline in
[AGENTS.md](../../AGENTS.md) already covers it.

**No Architectury.** Loader specific code is expected to land around 300 to 500 lines per loader.
That does not pay for the machinery.

**No migration to a template.** Three things are worth copying and nothing else: the per node
`buildscript` assignment, datagen output keyed by Minecraft version and shared between loaders, and
`me.modmuss50.mod-publish-plugin` once there are more than four nodes to upload.

## The invariant

```
core/                                       no version knowledge, no loader knowledge
platform/Vanilla, ClientVanilla             version axis      Stonecutter comments
platform/Loader                             loader axis       separate source sets
```

**The two axes never meet in one file.** A file is either version conditional and lives in
`src/main`, or loader specific and lives in `src/fabric` or `src/neoforge`. A loader specific file
may contain version conditionals. A `src/main` file may never contain a loader conditional.

## Verified constraints

Checked 2026-09-12. Re-check before relying on any of it.

- **Attachment sync does not exist on 1.21.1.** `AttachmentRegistry.Builder#syncWith` arrived in the
  Fabric API for 1.21.4. [ThirstData](../../src/main/java/com/thirstwastaken2/data/ThirstData.java)
  relies on it, so the 1.21.1 node needs a hand written sync packet, including the join, respawn and
  dimension change paths.
- **NeoForge exists for 26.1 and 26.2.** No blocker on the loader axis.
- **Mixins are required.** [thirstwastaken2.mixins.json](../../src/main/resources/thirstwastaken2.mixins.json)
  sets `"required": true` and `defaultRequire: 1`, so a mixin that fails to apply is a hard crash at
  startup, not a disabled feature. Any Minecraft release the mod claims compatibility with has to be
  launched at least once.
- **Loom's datagen wiring drops a Stonecutter task dependency.** `configureDataGeneration` adds its
  output to `main`'s resources by reading `srcDirs` back and setting them again, which flattens them
  to plain files. `processResources` and the sources jar then read the directory Stonecutter
  generates without waiting for it, and every node but the active one fails to build.
  [build.gradle.kts](../../build.gradle.kts) restores the dependency by hand.
- **`mod.mc_releases` is currently read by nothing.** It is declared per version in
  [stonecutter.properties.toml](../../stonecutter.properties.toml) but no build script or workflow
  consumes it. Marking a release compatible is still a manual step on Modrinth.
- **`docs/dev` is excluded from the site.** `srcExclude` in
  [config.mts](../../docs/.vitepress/config.mts) carries `dev/**`. Without it VitePress treats these
  pages as public and the dead link check fails on their links into `src/`.

## Phases

P0 and P1 are done. The remaining day counts are still estimates, but P0 re-anchored them against a
real build; see [P0-SPIKE.md](P0-SPIKE.md).

| Phase | Work | Estimate | Gate to move on |
|---|---|---|---|
| ~~**P0**~~ | Spike: stand up a `1.21.1` node, run `:1.21.1:build`, record what actually breaks | 1 day | **Done.** [P0-SPIKE.md](P0-SPIKE.md): 23 of 58 Java files and 31 of 57 JSON files break; 4 structural forks; overlap with P1 and P2 is total |
| ~~**P1**~~ | Move the 90 resource files to datagen, output keyed by Minecraft version | 2 to 3 days | **Done.** 58 of the 90 are generated into `src/main/generated/<minecraft version>/`; `:<version>:checkDatagen` runs in CI on all three nodes and 61 gametests pass on each |
| **P2** | `platform/Loader`, written while there is still only one loader | 2 to 3 days | Existing nodes build and pass gametests |
| **P3** | 1.21.1 Fabric node: sync packet, HUD fork, drink item fork, asset overlay | 4 to 6 days | Gametests green on four nodes, then **release and stop for feedback** |
| **P4** | NeoForge on 26.2 only | 8 to 15 days | Gametests green on five nodes |
| **P5** | NeoForge across the remaining versions, starting with 1.21.1, plus publish automation | 4 to 8 days | Seven nodes green |

Roughly 19 to 32 days of work left. Spread it over months, not weeks.

P0 came first because the ordering of P1 and P2 against P3 rested on an unverified claim: that the
1.21.1 port touches the same files datagen and the loader seam touch. The spike confirmed it in both
directions, so the ordering stands and P3 does not start early.

NeoForge starts on 26.2 rather than 1.21.1 to keep the unknowns to one. On 26.2 the code already
fits and only the loader is new; on 1.21.1 both the loader API and the Minecraft API are unfamiliar
at once. The users are on 1.21.1, so that is the first target of P5, not of P4.

## Exit ramps

Decide by measurement, not by feel.

- Version conditionals pass **50 blocks** (`grep -rn "//?" src`, currently 30 lines across 11
  blocks; P1 added 7 of them, all in datagen and all for 1.21.11), or any single file forks past half
  its body: stop and reconsider a branch for the 1.21.x family.
- P4 passes **20 days**: keep NeoForge on one version and drop P5.
- Gametests cannot be made to run on a node: that node does not ship. No exceptions.
- 1.21.1 starts shipping a different version number from the rest: it has frozen in practice.
  Retire it and let the last release stand rather than branching it.

## Support policy

Proposed here, to be promoted to [AGENTS.md](../../AGENTS.md) once P3 lands, because it is a
standing rule rather than a plan.

- **At most four version nodes.** Adding one means retiring one.
- The shape is one long lived old version, the two newest, and one in transition.
- When 26.3 arrives, 26.1.x is the one to drop. It is the shortest lived of the four.
- 1.21.1 is the deliberate exception, kept for the modpack ecosystem rather than for being current.

## Open questions

P0's four are all answered in [P0-SPIKE.md](P0-SPIKE.md): the 1.21.1 dependency block is resolved and
built, `loomx.loom_version` needs no per-version value, the rename is two `replacements` lines rather
than one, and the gametests run on 1.21.1 at the cost of a forked harness and no forked assertions.

P1 answered two more. The output directory is keyed by Minecraft version rather than by node, so two
loaders on one Minecraft version will share it, and the generators needed no version constant of
their own: `ThirstDatagen` has none. What P1 found instead is that **the same generator writes
different bytes per version**, because a codec that omits a defaulted field changed which fields have
defaults. That is expected and documented in [src/datagen/java/AGENTS.md](../../src/datagen/java/AGENTS.md);
it is not drift.

What is still open, for P2:

- Whether datagen stays in `src/datagen` shared by both loaders, or splits per loader. Fabric's
  `DataGeneratorEntrypoint` is Fabric API; NeoForge has its own entrypoint over the same vanilla
  providers. The generators themselves should not have to move either way.
- Whether the two loaders on one Minecraft version really do produce byte-identical files. P1 set
  the directory up for it but only one loader exists to test it with.
