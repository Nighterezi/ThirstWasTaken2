# Releasing

A release of ThirstWasTaken2 is eight files: four Minecraft versions on Fabric and the same four on
NeoForge. `tools/release/publish.py` builds them and uploads each to Modrinth with the Minecraft
releases and the optional mods that node actually has. It reads `stonecutter.properties.toml`,
`settings.gradle.kts` and `CHANGELOG.md` for all of that, so a node added to the build is released
without editing the script.

Releases are published to Modrinth only. The script makes no git tag and no GitHub release.

## Before publishing

1. **Bump the version.** `mod.version` in `stonecutter.properties.toml` is the one place it is written;
   the jar name, `fabric.mod.json` and `neoforge.mods.toml` all come from it.
2. **Write the changelog.** A `## [<version>] - <date>` section in `CHANGELOG.md`, in the plain,
   non-technical style the `write-docs` skill describes. The script sends that section, whole, to
   Modrinth as the version's changelog, and refuses to publish without it.
3. **Check the documented versions.** `python .github/scripts/update_mc_deps.py --check` fails when
   `README.md` or `docs/docs/installation.md` names a dependency version the build no longer pins.
4. **Test by hand.** [MANUAL-TESTING.md](MANUAL-TESTING.md) is the per-version checklist. CI has already
   run the game tests on every node.
5. **Commit and push.** The script stops on an uncommitted change, because what is published has to be
   built from a commit someone can check out again.

## Publishing

```bash
python tools/release/publish.py --dry-run
```

Prints each of the eight uploads -- the jar, the loader, the Minecraft releases it claims, the mods it
lists -- and sends nothing. Read it once, then:

```bash
python tools/release/publish.py
```

which builds every node (`gradlew buildAndCollect`) and uploads each jar to Modrinth.

| Flag | What it does |
|---|---|
| `--dry-run` | Prints the plan, publishes nothing. |
| `--no-build` | Publishes the jars already in `build/libs`, skipping Gradle. |
| `--version 1.0.6` | Fails unless the properties file says that version. A guard against releasing the wrong one. |
| `--allow-dirty` | Releases with uncommitted changes. |

The Modrinth token comes from `MODRINTH_TOKEN` in `.env`, which is git-ignored and stays on your
machine; it needs the create-version scope.

## When something goes wrong

Re-running finishes a half-done release: a version already on Modrinth is skipped by its version number.
Nothing published is ever overwritten, so a bad upload is deleted on Modrinth by hand first and then
published again from here.

## What each upload says

Read off the node's tables in `stonecutter.properties.toml`:

- **Version number and file name** -- `<mod.version>+<the Minecraft version the node compiles against>`,
  plus `-neoforge` on a NeoForge node, exactly what the buildscripts name the jar.
- **Minecraft releases** -- `mod.mc_releases`. The Fabric 1.21.1 file claims 1.21 as well; the NeoForge
  one does not.
- **Loader** -- the node's loader table.
- **Mods** -- one per `deps.*` key the node resolves: Fabric API required, Mod Menu, AppleSkin, Jade,
  Farmer's Delight, Create Fly and Create optional. `deps.cloth_config` is deliberately not among them: it is
  only on the dev client's runtime classpath, and no code in the mod names it. `deps.fabric_loader`,
  `deps.neoforge` and the Loom version are the platform, not mods to install.
