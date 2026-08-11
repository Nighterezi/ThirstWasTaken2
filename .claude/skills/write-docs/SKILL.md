---
name: write-docs
description: Write ThirstWasTaken2 end-user content - CHANGELOG entries, the VitePress documentation site in docs/, and the Modrinth page. Use when adding a changelog entry for a release, or writing/editing documentation pages. Enforces the plain, non-technical writing style and the bilingual English/Vietnamese mirror.
---

# Writing ThirstWasTaken2 docs and changelog

You are writing for **players and server owners**, not developers. They install the mod, play with it
and edit a config file. They do not care about classes, mixins, components, sprite generation,
refactors, or how anything works internally.

## Voice and rules (always)

- Short and clear. One idea per sentence. Cut every word that adds nothing.
- Say what changed in the game and why it matters to the player. Never describe the code or the
  internal mechanism.
- No duplicate information. If a point is already made, do not restate it in another section.
- Plain words over jargon. If a technical term is unavoidable, keep it to the exact config key,
  command or item name the player sees in game.
- **Never use the em dash or en dash character.** Use a comma, a full stop, or "to" for a range.
  Check with a ripgrep for `[—–]` before finishing.
- Never use emoji anywhere.
- Match the existing tone of the file you are editing.

Words that almost always mean the bullet is written for a developer: sprite, texture, component,
attachment, mixin, cache, record, registry, code-generated, refactor, class name, method name. Rewrite
the bullet around what the player sees instead, or cut it.

## Changelog

- File: `CHANGELOG.md` at the repo root. It is not published on the docs site, so this is the only
  file to edit.
- Newest version goes at the top, right under the intro line.
- Use the `[1.0.1]` entry as the tone reference: concise, professional, easy to scan.
- Format:

  ```
  ## [<version>] - <YYYY-MM-DD>

  ### Added
  - ...

  ### Changed
  - ...

  ### Fixed
  - ...

  ### Notes
  - ...
  ```

- Use only the sections you need (`Added`, `Changed`, `Fixed`, `Removed`, `Notes`). Skip empty ones.
  The `1.0.0` entry also uses `Changed from the original` and
  `Known issues and unavailable integrations`; reuse those headings only when a release genuinely
  needs them.
- Each line is one user-facing outcome. Good: "Ocean water no longer hydrates and cannot be made
  drinkable by cooking." Bad: "Moved purity sampling out of the tooltip path into WaterPurity."
- Never address the reader as `you` or `your`. State the behaviour or the required action directly.
- Summarize the affected behaviour once. Do not list several example scenarios when one general
  statement is accurate.
- Keep each bullet to one or two short sentences. Include exact commands, item names, config keys or
  file paths only when they help identify the change.
- Do not explain both the old and new behaviour unless the contrast is needed to understand the fix.
- Reserve `Notes` for required upgrade actions, compatibility details, or a brief "no action required"
  statement. Do not repeat items from other sections or document every edge case.
- Get the version from `gradle.properties` (`mod_version=26.2-<version>`). The changelog heading uses
  the mod version only, without the Minecraft prefix.

### What belongs in the main sections

The main sections answer "what is different when someone plays this version". Lead with the result,
not the setting or the artwork that produces it. "Water bowls now look different at each purity level"
belongs at the top level. "Four code-generated water-bowl sprites" does not, because it describes how
the icons were made.

Move to the collapsible details block: config key renames, removed or replaced keys, changes to the
shape of a config entry, and new debug or log output. Keep one short line in `Changed` pointing to it,
for example "Some config keys were renamed. See the details below."

Do not claim a fix for a limit or failure the previous version could not reach. Check what was
actually possible before writing a `Fixed` line.

### Collapsible details block

Place it at the end of the version's section, after `Notes`. Only add one when there are config-level
changes to record. Blank lines around the content are required or the Markdown will not render.

```
<details>
<summary>Configuration file details</summary>

- ...

</details>
```

## Documentation site (VitePress, in `docs/`)

Read `docs/CLAUDE.md` for the page layout and which page owns which numbers, then apply the style
rules above. In addition:

- Bilingual. English lives in `docs/docs/`, Vietnamese in `docs/vi/docs/`. Adding or changing a page
  means updating both languages in the same pass.
- Vietnamese links are absolute and carry the prefix: `/vi/docs/commands`, not `/docs/commands`.
- Vietnamese in-game wording comes from
  `src/main/resources/assets/thirstwastaken2/lang/vi_vn.json`, so purity levels are Bẩn, Hơi bẩn,
  Có thể uống and Đã tinh lọc, and "quenched" is "đã khát".
- Pages start with a `#` heading, not frontmatter.
- Use normal Markdown: headings, short paragraphs, tables for options, numbered lists for steps. Keep
  pages scannable.
- Config keys are `### keyName` with the default in the first line, so the page outline becomes a
  usable index.
- VitePress containers (`::: tip`, `::: warning`) are fine for a single important callout. Do not
  overuse them.
- Say a number in exactly one place and link to it. Hydration values, sickness chances and purity by
  location live on the `docs/features/` page that explains them; `docs/configuration.md` links there.
- `npm run docs:build` must pass before a docs change is done.

## Modrinth page

`docs/MODRINTH.md` is the project description on Modrinth and follows the same voice. It is a short
feature list, not a changelog, so only touch it when a release adds or removes something a player
would look for before downloading. Image links there stay absolute.

## Before finishing

- Re-read the text once and delete any sentence that repeats another.
- Compare a new changelog entry with `[1.0.1]` and shorten anything noticeably more verbose without
  adding necessary information.
- Read every main-section bullet and ask whether a player who never opens a config file would care.
  If not, move it into the details block or cut it.
- Search changelog changes for second-person wording such as `you`, `your` and `yours`, and rewrite it.
- Search the output for `[—–]` and any emoji, and remove them.
- If a docs page was touched, confirm the matching page in the other language was updated too, and
  that `npm run docs:build` passes.
