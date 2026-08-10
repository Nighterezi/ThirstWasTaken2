# docs/

VitePress site for players and server owners. English at the root, Vietnamese under `vi/`.

```bash
npm install
npm run docs:dev      # local preview
npm run docs:build    # must pass before you call a docs change done
```

## Layout

| Path | Holds |
|---|---|
| `index.md`, `vi/index.md` | The hero page. Feature cards link into the two sections below. |
| `docs/` | The manual: overview, installation, commands, configuration, FAQ. |
| `features/` | What the mod does and why, in prose. No config key listings. |
| `.vitepress/config.mts` | Nav and both sidebars. |
| `.vitepress/theme/` | Default theme plus `custom.css` for the brand colour. |
| `public/` | `logo.png` for the site, `logo-icon.png` for release pages, `water-purity.png`. |

`docs/` answers "how do I set this up". `features/` answers "what is this like to play". A page that
starts listing config keys belongs in `docs/`.

## Style

Written for a server owner who has never seen the code. That means:

- Plain language. Say what a setting does to the game, not which class reads it.
- No Java identifiers, no file paths inside `src/`, no mention of Mojang mappings.
- Short sentences. A default and a one-line reason beats a paragraph.
- Config keys as `### keyName` with the default in the first line, so the on-page outline becomes a
  usable index.
- **No em dashes or en dashes.** Use a comma, a period, or "to" for a range. Check with a ripgrep for
  `[—–]` before finishing.

Say the same thing in exactly one place and link to it. Numbers a player cares about (item hydration
values, sickness chances, purity by location) live on the `features/` page that explains them, and
`docs/configuration.md` links there instead of repeating the tables.

Vietnamese pages are a full mirror, and their links are absolute with the prefix:
`/vi/docs/commands`, not `/docs/commands`. In-game wording comes from
`src/main/resources/assets/thirstwastaken/lang/vi_vn.json`, so purity levels are Bẩn, Hơi bẩn, Có thể
uống and Đã tinh lọc, and "quenched" is "đã khát".

## Keeping it true

The pages describe real behaviour, so a change to the mod means a docs edit in the same pass. The
ones most likely to go stale:

| Changed | Update |
|---|---|
| A field in `ThirstConfig` | `docs/configuration.md`, both languages |
| A `/thirst` subcommand | `docs/commands.md` |
| Exhaustion, climate or damage in `ThirstManager` | `features/thirst-and-quenched.md` |
| Hydration values, bowls, loot | `features/drinking.md` |
| Anything in `WaterPurity` or a purify recipe | `features/water-purity.md` |
| Supported Minecraft, Loader or Fabric API version | `docs/installation.md` |
