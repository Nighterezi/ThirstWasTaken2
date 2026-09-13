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
  `mixin/` is a signal the seam is missing.
- Mixins are the documented exception: their `@Inject` signatures track the target method and cannot
  be abstracted away. Keep their bodies one line regardless.

## Loader

`Loader` does not live in this directory. Each loader has its own copy at
`src/main/<loader>/java/com/thirstwastaken2/platform/Loader.java`, with the same class name and the
same public signatures, and `build.gradle.kts` compiles exactly one of them. There is no interface
and no service lookup: a static call to a class that exists once per jar is the cheapest seam there
is, and the compiler checks every call site.

What does live here are the types those signatures need, because both copies have to share them:
`PlayerData`, `UseBlockHandler`, `UseItemHandler`, and on the client `StatusBarRenderer`.

| `Loader` | What it hides |
|---|---|
| `isDevelopmentEnvironment`, `configDir`, `isModLoaded` | the loader's own environment |
| `playerData` | the attachment system that saves a value on a player and syncs it to its owner |
| `creativeTabBuilder` | a tab builder that places itself in the tab list |
| `onServerTickEnd`, `onUseBlock`, `onUseItem`, `onRegisterCommands` | the event bus |
| `onBuiltinLootTable` | loot table modification, datapack replacements excluded |
| `ClientLoader.addRightStatusBar` | HUD layer registration and the right-hand status bar height |

Rules:

- **Plumbing only, the same as `Vanilla`.** A method takes the mod's handler and hands it to the
  loader; it never decides anything. What the mod hooks, and in what order, stays in
  `ThirstWasTaken2.initialize`, which is loader independent.
- **Shape a signature after what the mod needs, not after the loader at hand.** Fabric's callbacks
  are one way to deliver a block interaction; NeoForge's cancellable events are another. The
  signature has to be implementable by both.
- **Every copy changes together.** Adding a method to one `Loader` means adding it to all of them;
  the node that lacks it is the one that fails to compile.
- **Entrypoints are loader code.** `ThirstWasTaken2Fabric` and `ThirstWasTaken2FabricClient` do one
  thing: call `ThirstWasTaken2.initialize` and `ThirstWasTaken2Client.initialize`. So do the manifest
  (`src/main/fabric/resources/fabric.mod.json`) and anything written against a loader-only mod, such
  as `ModMenuIntegration`.
- **`checkLoaderSeam` fails on a loader import in `src/main/java` or `src/client/java`.** It reads
  imports, so it cannot see the methods Fabric API injects into vanilla classes
  (`getAttachedOrCreate`, `FabricItemStack` and friends). Those compile on Fabric and only fail on the
  next loader. Do not call them outside `src/main/fabric`.

`src/gametest`, `src/dev` and `src/datagen` are still Fabric only, each its own small Fabric mod.
They sit outside the seam on purpose: none of them ships, and datagen output is shared by every
loader on a Minecraft version.
