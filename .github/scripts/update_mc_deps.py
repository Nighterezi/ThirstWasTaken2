"""Bumps the Minecraft-bound dependency versions in stonecutter.properties.toml.

Dependabot cannot do this. The versions live in a Stonecutter properties file it does not read, and
even if it did, it would offer every version node the newest Fabric API, the one built for the newest
Minecraft. This asks Modrinth instead, once per node, for the newest upload of each mod that is a
Fabric build and lists the Minecraft version that node compiles against, and writes it back in place,
comments and layout untouched.

    python .github/scripts/update_mc_deps.py            # rewrite the file
    python .github/scripts/update_mc_deps.py --dry-run  # only report
    python .github/scripts/update_mc_deps.py --summary build/deps-summary.md

Rules:
- A node compiles against the Minecraft version settings.gradle.kts gives it (`26.1.x` -> `26.1.2`),
  so that is the version a candidate has to list. A newer Minecraft patch is a manual bump.
- Only release uploads are taken, unless the pinned version is itself a beta or alpha: a node on a
  pre-release dependency stays on that channel until a release catches up.
- A candidate has to be published after the pinned version. Nothing is ever downgraded.
- Fabric Loader is one global value and comes from Fabric's meta API, stable builds only. Raising it
  raises the minimum loader players need, since fabric.mod.json writes it as `>=`.
- Loom is left alone: a Loom bump tends to need a Gradle bump alongside it.

Standard library only, so CI needs nothing but a Python interpreter.
"""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
import urllib.parse
import urllib.request
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PROPERTIES = ROOT / "stonecutter.properties.toml"
SETTINGS = ROOT / "settings.gradle.kts"

MODRINTH = "https://api.modrinth.com/v2"
FABRIC_META = "https://meta.fabricmc.net/v2/versions/loader"
# Modrinth asks every client for a User-Agent that identifies the project.
USER_AGENT = "Nighterezi/ThirstWasTaken2 dependency updater (github.com/Nighterezi/ThirstWasTaken2)"


@dataclass(frozen=True)
class ModrinthDep:
    key: str
    """The key after `deps.` in the properties file."""
    project: str
    """Modrinth project slug."""
    by_id: bool = False
    """Pinned by Modrinth version id instead of version number."""


# Every per-node dependency the build resolves from Modrinth or from a Maven that publishes the same
# version numbers (Fabric API). Add a line here when build.gradle.kts gains a `deps.*` property.
MODRINTH_DEPS = [
    ModrinthDep("fabric_api", "fabric-api"),
    ModrinthDep("modmenu", "modmenu"),
    # AppleSkin shares one version number between its Fabric and NeoForge uploads.
    ModrinthDep("appleskin", "appleskin", by_id=True),
    ModrinthDep("cloth_config", "cloth-config"),
    ModrinthDep("jade", "jade"),
    ModrinthDep("farmersdelight", "farmers-delight-refabricated"),
    ModrinthDep("create_fly", "create-fly"),
]


@dataclass
class Change:
    node: str | None
    """Version node, or None for a global value."""
    key: str
    old: str
    new: str
    old_label: str
    """What a person reads as the old version; differs from `old` for id-pinned dependencies."""
    new_label: str
    url: str


def get_json(url: str):
    request = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.load(response)


def node_minecraft_versions() -> dict[str, str]:
    """Maps each Stonecutter node to the Minecraft version it compiles against, from settings.gradle.kts."""
    text = SETTINGS.read_text(encoding="utf-8")
    nodes: dict[str, str] = {}
    # versions("1.21.1", "1.21.11"): the node name is the Minecraft version.
    for group in re.findall(r'\bversions\(([^)]*)\)', text):
        for name in re.findall(r'"([^"]+)"', group):
            nodes[name] = name
    # version("26.1.x", "26.1.2"): a named node and the version behind it.
    for name, minecraft in re.findall(r'\bversion\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)', text):
        nodes[name] = minecraft
    if not nodes:
        sys.exit(f"No Stonecutter versions found in {SETTINGS.name}.")
    return nodes


class Properties:
    """The properties file as lines, so a rewrite keeps every comment and blank line."""

    def __init__(self, path: Path):
        self.path = path
        # newline="" keeps CRLF on a Windows checkout, so the rewrite only touches the changed values.
        with path.open(encoding="utf-8", newline="") as file:
            self.lines = file.read().splitlines(keepends=True)

    def _pattern(self, key: str) -> re.Pattern[str]:
        return re.compile(r'^(\s*' + re.escape(key) + r'\s*=\s*")([^"]*)(".*)$', re.DOTALL)

    def find(self, node: str | None, key: str) -> tuple[int, str] | None:
        """Line index and value of `key` inside `[node]`, or in the top level when node is None."""
        section: str | None = None
        pattern = self._pattern(key)
        for index, line in enumerate(self.lines):
            header = re.match(r'^\s*\["([^"]+)"\]\s*$', line)
            if header:
                section = header.group(1)
                continue
            if section == node:
                match = pattern.match(line)
                if match:
                    return index, match.group(2)
        return None

    def set(self, index: int, key: str, value: str) -> None:
        match = self._pattern(key).match(self.lines[index])
        assert match, self.lines[index]
        self.lines[index] = match.group(1) + value + match.group(3)

    def replace_in_comment_above(self, index: int, old: str, new: str) -> None:
        """Keeps a comment such as `# 3.0.6+mc1.21, the Fabric upload.` in step with the id below it."""
        above = index - 1
        if above >= 0 and self.lines[above].lstrip().startswith("#") and old in self.lines[above]:
            self.lines[above] = self.lines[above].replace(old, new)

    def save(self) -> None:
        self.path.write_text("".join(self.lines), encoding="utf-8", newline="")


def modrinth_version(project: str, id_or_number: str) -> dict | None:
    try:
        return get_json(f"{MODRINTH}/project/{project}/version/{urllib.parse.quote(id_or_number, safe='')}")
    except urllib.error.HTTPError as error:
        if error.code == 404:
            return None
        raise


def modrinth_candidates(project: str, minecraft: str) -> list[dict]:
    query = urllib.parse.urlencode({
        "loaders": json.dumps(["fabric"]),
        "game_versions": json.dumps([minecraft]),
        "include_changelog": "false",
    })
    versions = get_json(f"{MODRINTH}/project/{project}/version?{query}")
    return sorted(versions, key=lambda v: v["date_published"], reverse=True)


def check_modrinth(props: Properties, node: str, minecraft: str, dep: ModrinthDep, changes: list[Change],
                   warnings: list[str]) -> None:
    found = props.find(node, f"deps.{dep.key}")
    if found is None:
        return
    index, pinned = found

    current = modrinth_version(dep.project, pinned)
    if current is None:
        warnings.append(f"`{node}` `deps.{dep.key}` = `{pinned}` was not found on Modrinth ({dep.project}); skipped.")
        return

    channels = {"release", current["version_type"]}
    newest = next((v for v in modrinth_candidates(dep.project, minecraft) if v["version_type"] in channels), None)
    if newest is None or newest["date_published"] <= current["date_published"]:
        return
    value = newest["id"] if dep.by_id else newest["version_number"]
    # Jade sometimes re-uploads a build under the same number, which resolves to the same artifact.
    if value == pinned:
        return

    props.set(index, f"deps.{dep.key}", value)
    if dep.by_id:
        props.replace_in_comment_above(index, current["version_number"], newest["version_number"])
    changes.append(Change(
        node=node,
        key=dep.key,
        old=pinned,
        new=value,
        old_label=current["version_number"],
        new_label=newest["version_number"],
        url=f"https://modrinth.com/mod/{dep.project}/version/{newest['id']}",
    ))


def numeric(version: str) -> tuple[int, ...]:
    return tuple(int(part) for part in re.findall(r"\d+", version))


def check_loader(props: Properties, changes: list[Change]) -> None:
    found = props.find(None, "deps.fabric_loader")
    if found is None:
        return
    index, pinned = found
    stable = [v["version"] for v in get_json(FABRIC_META) if v.get("stable")]
    if not stable:
        return
    newest = max(stable, key=numeric)
    if numeric(newest) <= numeric(pinned):
        return
    props.set(index, "deps.fabric_loader", newest)
    changes.append(Change(None, "fabric_loader", pinned, newest, pinned, newest,
                          "https://github.com/FabricMC/fabric-loader/releases"))


def files_mentioning(value: str) -> list[str]:
    """Tracked files other than the properties file that still name an old version, for the PR body."""
    result = subprocess.run(["git", "grep", "-l", "-w", "-F", value, "--", ".", f":!{PROPERTIES.name}",
                             ":!**/package-lock.json"],
                            cwd=ROOT, capture_output=True, text=True)
    return [line for line in result.stdout.splitlines() if line]


def summary(changes: list[Change], warnings: list[str]) -> str:
    out = ["Updates the Minecraft-bound dependencies in `stonecutter.properties.toml`. Each candidate is a Fabric "
           "upload on Modrinth that lists the Minecraft version its node compiles against.", ""]
    if changes:
        out += ["| Node | Dependency | From | To |", "| --- | --- | --- | --- |"]
        for change in changes:
            out.append(f"| {change.node or 'all'} | `{change.key}` | `{change.old_label}` | "
                       f"[`{change.new_label}`]({change.url}) |")
        out.append("")

    notes = []
    if any(change.key == "fabric_loader" for change in changes):
        notes.append("Fabric Loader is written into `fabric.mod.json` as `>=`, so this raises the minimum loader "
                     "players need.")
    stale = {}
    for change in changes:
        for path in files_mentioning(change.old_label):
            stale.setdefault(path, set()).add(change.old_label)
    if stale:
        notes.append("These files still mention an old version. Update them if it is a documented minimum:")
        notes += [f"  - `{path}`: {', '.join(f'`{v}`' for v in sorted(values))}" for path, values in sorted(stale.items())]
    notes += warnings
    if notes:
        out += ["### Notes", ""] + [note if note.startswith("  ") else f"- {note}" for note in notes] + [""]
    return "\n".join(out)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--dry-run", action="store_true", help="report what would change without writing")
    parser.add_argument("--summary", type=Path, help="write a Markdown summary here, for a pull request body")
    args = parser.parse_args()

    props = Properties(PROPERTIES)
    changes: list[Change] = []
    warnings: list[str] = []

    check_loader(props, changes)
    for node, minecraft in node_minecraft_versions().items():
        if props.find(node, "mod.mc_compat") is None:
            warnings.append(f"`{node}` is in settings.gradle.kts but has no table in {PROPERTIES.name}; skipped.")
            continue
        for dep in MODRINTH_DEPS:
            check_modrinth(props, node, minecraft, dep, changes, warnings)

    for change in changes:
        print(f"{change.node or 'all'}: {change.key} {change.old_label} -> {change.new_label}")
    for warning in warnings:
        print(f"warning: {warning}", file=sys.stderr)
    if not changes:
        print("Everything is up to date.")

    if not args.dry_run and changes:
        props.save()
    if args.summary:
        args.summary.parent.mkdir(parents=True, exist_ok=True)
        args.summary.write_text(summary(changes, warnings), encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
