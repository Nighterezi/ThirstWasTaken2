# platform/

The single place where Minecraft version differences are allowed to live.

The mod is built for several Minecraft versions from one source tree (see the root `AGENTS.md`), so
a handful of vanilla calls have to be written twice. `Vanilla` collects them behind signatures that
are identical on every version.

## Rules

- **Only vanilla-facing plumbing.** No thirst logic, no config reads, no caching. If a method here
  needs to know what the mod is doing, it is in the wrong package.
- **Same signature on every version.** A caller must never need to know which branch is live.
- **Add to `Vanilla` rather than to the caller.** A `//?` block anywhere outside this package and
  `mixin/` is a signal the seam is missing.
- Mixins are the documented exception: their `@Inject` signatures track the target method and cannot
  be abstracted away. Keep their bodies one line regardless.
