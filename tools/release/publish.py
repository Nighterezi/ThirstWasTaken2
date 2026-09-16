"""Publishes one release of ThirstWasTaken2: every jar to Modrinth.

A release is eight uploads -- four Minecraft versions on two loaders -- each with its own file, its own
list of Minecraft releases and its own list of optional mods. Done by hand in Modrinth's web form that is
eight chances to attach the wrong jar or forget a game version, and nothing afterwards would say so.
Everything a release needs is already written down in the repository, so it is read rather than retyped:

- the version number and every node from `stonecutter.properties.toml`, the file the build and CI read
  too, so a node added there is released without touching this script;
- the Minecraft releases an upload claims, from that node's `mod.mc_releases`;
- the optional mods to list, from the `deps.*` keys the node resolves, so a dependency dropped from a
  node stops being listed for it;
- the release notes, from the matching section of `CHANGELOG.md`.

    python tools/release/publish.py --dry-run   # print every upload, send nothing
    python tools/release/publish.py             # build and upload to Modrinth
    python tools/release/publish.py --no-build  # publish the jars already in build/libs

Modrinth is where players get the mod, and it is the only place a release is published: there is no tag
and no GitHub release.

The token comes from `MODRINTH_TOKEN` in `.env`, which is git-ignored, or from the environment; it needs
Modrinth's create-version scope.

Re-running is safe, and is how a half-finished release is finished: a version already on Modrinth is
skipped by its version number. Nothing already published is overwritten, so a bad upload is deleted on
Modrinth by hand and then re-run here.

Standard library only, like the rest of the Python in this repository.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import tomllib
import urllib.error
import urllib.request
import uuid
from dataclasses import dataclass, field
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PROPERTIES = ROOT / "stonecutter.properties.toml"
SETTINGS = ROOT / "settings.gradle.kts"
CHANGELOG = ROOT / "CHANGELOG.md"
LIBS = ROOT / "build" / "libs"
ENV_FILE = ROOT / ".env"

MODRINTH = "https://api.modrinth.com/v2"
PROJECT = "thirst-was-taken-2"
# Modrinth asks every client for a User-Agent that identifies the project.
USER_AGENT = "Nighterezi/ThirstWasTaken2 release publisher (github.com/Nighterezi/ThirstWasTaken2)"

# Every `deps.*` key that names a mod a player can install, and how an upload should list it. Pinned by
# Modrinth project id because that is what the version endpoint stores, and an id outlives a rename.
# A key left out is left out deliberately: `fabric_loader`, `neoforge` and `loomx.loom_version` are the
# platform rather than a mod, and `cloth_config` is only on the dev client's runtime classpath -- no code
# in the mod names it -- so an upload must not tell players to install it.
DEPENDENCIES = {
    "fabric_api": ("fabric-api", "P7dR8mSH", "required"),
    "modmenu": ("modmenu", "mOgUt4GM", "optional"),
    "appleskin": ("appleskin", "EsAfCjCV", "optional"),
    "jade": ("jade", "nvQzSEkH", "optional"),
    "farmersdelight": ("farmers-delight-refabricated", "7vxePowz", "optional"),
    "create_fly": ("create-fly", "dKvj0eNn", "optional"),
}


@dataclass
class Node:
    """One Stonecutter node, which is one file on Modrinth."""

    name: str
    """The node as settings.gradle.kts names it: `26.2.x`, or `26.2.x-neoforge`."""
    loader: str
    minecraft: str
    """The Minecraft version the node compiles against; the jar name carries it."""
    game_versions: list[str]
    """Every Minecraft release the upload claims, from `mod.mc_releases`."""
    dependencies: list[dict] = field(default_factory=list)
    dependency_names: list[str] = field(default_factory=list)
    """The same dependencies as slugs, only so the printed plan is readable."""
    jar: Path = ROOT


def fail(message: str):
    sys.exit(f"error: {message}")


def run(command: list[str], **kwargs) -> subprocess.CompletedProcess:
    return subprocess.run(command, cwd=ROOT, text=True, **kwargs)


def load_token() -> str | None:
    """`MODRINTH_TOKEN` from the environment, or else from `.env`, which is git-ignored."""
    token = os.environ.get("MODRINTH_TOKEN")
    if token:
        return token.strip()
    if not ENV_FILE.exists():
        return None
    for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        if key.strip() == "MODRINTH_TOKEN":
            return value.strip().strip('"').strip("'")
    return None


def node_minecraft_versions() -> dict[str, str]:
    """Maps each node to the Minecraft version it compiles against, from settings.gradle.kts.

    The same reading .github/scripts/update_mc_deps.py does: `versions("1.21.1")` names a node after its
    Minecraft version, and `version("26.1.x", "26.1.2")` gives a node a name of its own plus the version
    behind it.
    """
    text = SETTINGS.read_text(encoding="utf-8")
    nodes: dict[str, str] = {}
    for group in re.findall(r"\bversions\(([^)]*)\)", text):
        for name in re.findall(r'"([^"]+)"', group):
            nodes[name] = name
    for name, minecraft in re.findall(r'\bversion\(\s*"([^"]+)"\s*,\s*"([^"]+)"\s*\)', text):
        nodes[name] = minecraft
    if not nodes:
        fail(f"no Stonecutter versions found in {SETTINGS.name}")
    return nodes


def version_number(node: Node, mod_version: str) -> str:
    """What the buildscripts set as the project version, so what the jar and the upload are called."""
    suffix = "-neoforge" if node.loader == "neoforge" else ""
    return f"{mod_version}+{node.minecraft}{suffix}"


def read_nodes(props: dict, mod_version: str) -> list[Node]:
    """Every node in the properties file, newest Minecraft first, Fabric before NeoForge.

    A loader table is what makes a node -- the rule CI's matrix and the dependency updater go by -- so
    `[neoforge."26.2.x"]` is the node `26.2.x-neoforge`. Values are read from the loader table first and
    then from the shared `["26.2.x"]` table, the layering stonecutter.gradle.kts tags each node with.
    """
    minecraft_of = node_minecraft_versions()
    nodes: list[Node] = []
    for loader in ("fabric", "neoforge"):
        for version, table in props.get(loader, {}).items():
            name = version if loader == "fabric" else f"{version}-neoforge"
            shared = props.get(version, {})

            def value(key: str, subkey: str, table=table, shared=shared):
                return table.get(key, {}).get(subkey, shared.get(key, {}).get(subkey))

            if name not in minecraft_of:
                fail(f"`{name}` has a table in {PROPERTIES.name} but no entry in {SETTINGS.name}")
            releases = value("mod", "mc_releases")
            if not releases:
                fail(f"`{name}` has no mod.mc_releases; Modrinth needs the Minecraft releases to list")

            dependencies, names = [], []
            for key, (slug, project_id, kind) in DEPENDENCIES.items():
                if value("deps", key) is not None:
                    dependencies.append({"project_id": project_id, "dependency_type": kind})
                    names.append(slug if kind == "optional" else f"{slug} (required)")

            minecraft = minecraft_of[name]
            node = Node(name, loader, minecraft, list(releases), dependencies, names)
            node.jar = LIBS / f"{props['mod']['name']}-{version_number(node, mod_version)}.jar"
            nodes.append(node)

    # Newest Minecraft first, Fabric before NeoForge: the order Modrinth lists them in afterwards, and
    # the order a player reads down the file list.
    def order(node: Node) -> tuple:
        return (node.loader != "fabric", [-int(part) for part in node.minecraft.split(".")])

    return sorted(nodes, key=order)


def changelog_section(mod_version: str) -> str:
    """The body of `## [1.0.6] - <date>`, up to the next release heading.

    Taken whole rather than summarised: the file is already written for players to read.
    """
    text = CHANGELOG.read_text(encoding="utf-8")
    pattern = re.compile(r"^## \[" + re.escape(mod_version) + r"\][^\n]*\n(.*?)(?=^## \[|\Z)", re.S | re.M)
    match = pattern.search(text)
    if not match:
        fail(f"{CHANGELOG.name} has no `## [{mod_version}]` section; write the entry before releasing")
    body = match.group(1).strip()
    if not body:
        fail(f"the `## [{mod_version}]` section of {CHANGELOG.name} is empty")
    return body


def modrinth_request(path: str, token: str | None = None, method: str = "GET",
                     body: bytes | None = None, content_type: str | None = None):
    headers = {"User-Agent": USER_AGENT}
    if token:
        headers["Authorization"] = token
    if content_type:
        headers["Content-Type"] = content_type
    request = urllib.request.Request(f"{MODRINTH}{path}", data=body, headers=headers, method=method)
    with urllib.request.urlopen(request, timeout=300) as response:
        return json.load(response)


def multipart(data: dict, jar: Path) -> tuple[bytes, str]:
    """Modrinth's version endpoint takes the metadata as a `data` field beside the file itself."""
    boundary = uuid.uuid4().hex
    body = bytearray()
    body += f"--{boundary}\r\n".encode()
    body += b'Content-Disposition: form-data; name="data"\r\n'
    body += b"Content-Type: application/json\r\n\r\n"
    body += json.dumps(data).encode("utf-8") + b"\r\n"
    body += f"--{boundary}\r\n".encode()
    body += f'Content-Disposition: form-data; name="file"; filename="{jar.name}"\r\n'.encode()
    body += b"Content-Type: application/java-archive\r\n\r\n"
    body += jar.read_bytes() + b"\r\n"
    body += f"--{boundary}--\r\n".encode()
    return bytes(body), f"multipart/form-data; boundary={boundary}"


def publish_modrinth(node: Node, mod_version: str, project_id: str, title: str,
                     changelog: str, token: str) -> str:
    number = version_number(node, mod_version)
    data = {
        "project_id": project_id,
        # What the version is called on the page. Past releases read "Thirst Was Taken 2 1.0.5+26.2",
        # the project's own title and the version number, so the title is asked for rather than written
        # here: `mod.name` is the jar's name, ThirstWasTaken2, not the name on Modrinth.
        "name": f"{title} {number}",
        "version_number": number,
        "changelog": changelog,
        "dependencies": node.dependencies,
        "game_versions": node.game_versions,
        "version_type": "release",
        "loaders": [node.loader],
        # Modrinth features the newest of each loader by itself; featuring eight files by hand would
        # bury the rest of the page.
        "featured": False,
        "status": "listed",
        "file_parts": ["file"],
        "primary_file": "file",
    }
    body, content_type = multipart(data, node.jar)
    try:
        created = modrinth_request("/version", token=token, method="POST",
                                   body=body, content_type=content_type)
    except urllib.error.HTTPError as error:
        detail = error.read().decode("utf-8", "replace")
        fail(f"Modrinth refused `{number}` ({error.code}): {detail}")
    return created["id"]


def git(*args: str) -> str:
    result = run(["git", *args], capture_output=True)
    if result.returncode != 0:
        fail(f"git {' '.join(args)} failed: {result.stderr.strip()}")
    return result.stdout.strip()


def check_worktree(allow_dirty: bool) -> None:
    """A release has to be a commit someone can check out again, so a dirty tree stops it."""
    if git("status", "--porcelain"):
        if not allow_dirty:
            fail("the working tree has uncommitted changes; commit them or pass --allow-dirty")
        print("warning: releasing with uncommitted changes; no commit matches what is published")
    branch = git("rev-parse", "--abbrev-ref", "HEAD")
    if branch != "main":
        print(f"warning: on branch `{branch}`, not `main`")


def gradle_build() -> None:
    """`buildAndCollect` builds every node and copies the jars into build/libs, the same as CI."""
    wrapper = str(ROOT / "gradlew.bat") if os.name == "nt" else "./gradlew"
    print(f"$ {wrapper} buildAndCollect\n")
    if run([wrapper, "buildAndCollect"]).returncode != 0:
        fail("the build failed; nothing was published")


def main() -> None:
    parser = argparse.ArgumentParser(description="Publish a ThirstWasTaken2 release.")
    parser.add_argument("--dry-run", action="store_true", help="print every upload and send nothing")
    parser.add_argument("--no-build", action="store_true", help="publish the jars already in build/libs")
    parser.add_argument("--version", help="fail unless stonecutter.properties.toml says this version")
    parser.add_argument("--allow-dirty", action="store_true", help="release with uncommitted changes")
    args = parser.parse_args()

    props = tomllib.loads(PROPERTIES.read_text(encoding="utf-8"))
    mod_version = props["mod"]["version"]
    if args.version and args.version != mod_version:
        fail(f"{PROPERTIES.name} says `{mod_version}`, not `{args.version}`")
    changelog = changelog_section(mod_version)
    nodes = read_nodes(props, mod_version)

    print(f"ThirstWasTaken2 {mod_version}, {len(nodes)} files\n")
    check_worktree(args.allow_dirty)

    if not args.no_build and not args.dry_run:
        gradle_build()
    missing = [node.jar.name for node in nodes if not node.jar.exists()]
    if missing:
        fail("not in build/libs: " + ", ".join(missing)
             + "\n       build first, or drop --no-build")

    token = load_token()
    if not token:
        fail(f"no MODRINTH_TOKEN in the environment or in {ENV_FILE.name}")
    project = modrinth_request(f"/project/{PROJECT}", token=token)
    published = {version["version_number"]
                 for version in modrinth_request(f"/project/{PROJECT}/version", token=token)}

    for node in nodes:
        number = version_number(node, mod_version)
        print(f"  {number}  ({node.name})")
        print(f"    file      {node.jar.name}, {node.jar.stat().st_size // 1024} KiB")
        print(f"    loader    {node.loader}")
        print(f"    minecraft {', '.join(node.game_versions)}")
        print(f"    mods      {', '.join(node.dependency_names) or 'none'}")
        if number in published:
            print("    -> already on Modrinth, skipped")
        elif args.dry_run:
            print("    -> would upload")
        else:
            created = publish_modrinth(node, mod_version, project["id"], project["title"],
                                       changelog, token)
            print(f"    -> uploaded, {created}")
        print()

    if args.dry_run:
        print("\nDry run: nothing was published.")
    else:
        print(f"\nDone. https://modrinth.com/mod/{PROJECT}/versions")


if __name__ == "__main__":
    main()
