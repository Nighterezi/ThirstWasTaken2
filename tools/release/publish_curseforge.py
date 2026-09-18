"""Uploads the jars of one release of ThirstWasTaken2 to CurseForge.

The CurseForge counterpart of `publish.py`, and it reads the same things: the nodes and their Minecraft
releases from `stonecutter.properties.toml`, the changelog from `CHANGELOG.md`, the jars from
`build/libs`. It does not build; run `publish.py` (or `gradlew buildAndCollect`) first.

CurseForge's upload API only adds files to a project that already exists: the project itself is created
by hand at https://authors.curseforge.com, with `docs/CURSEFORGE.md` as its description. Its numeric id
goes in `.env` as `CURSEFORGE_PROJECT_ID`, beside the `CURSEFORGE_TOKEN` this script authenticates with.

    python tools/release/publish_curseforge.py --dry-run          # print every upload, send nothing
    python tools/release/publish_curseforge.py                    # upload every jar
    python tools/release/publish_curseforge.py --only 26.2.x      # upload one node

Unlike Modrinth, the upload API cannot list a project's files, so nothing here can tell an upload that
already happened: re-running uploads every jar again. `--only` finishes a half-done release.

Standard library only, like the rest of the Python in this repository.
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import tomllib
import urllib.error
import urllib.request
import uuid
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from publish import (  # noqa: E402
    ENV_FILE, PROPERTIES, Node, changelog_section, fail, read_nodes, version_number,
)

CURSEFORGE = "https://minecraft.curseforge.com/api"
TITLE = "Thirst Was Taken 2"

# `deps.*` keys to CurseForge project slugs, the way the upload API names a relation. The same keys as
# `publish.DEPENDENCIES`, for the same reasons.
RELATIONS = {
    "fabric_api": ("fabric-api", "requiredDependency"),
    "modmenu": ("modmenu", "optionalDependency"),
    "appleskin": ("appleskin", "optionalDependency"),
    "jade": ("jade", "optionalDependency"),
    "farmersdelight": ("farmers-delight-refabricated", "optionalDependency"),
    "create_fly": ("create-fly", "optionalDependency"),
}

LOADERS = {"fabric": "Fabric", "neoforge": "NeoForge"}


def env_value(key: str) -> str | None:
    """`key` from the environment, or else from `.env`, which is git-ignored."""
    value = os.environ.get(key)
    if value:
        return value.strip()
    if not ENV_FILE.exists():
        return None
    for line in ENV_FILE.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line.startswith("#") or "=" not in line:
            continue
        name, value = line.split("=", 1)
        if name.strip() == key:
            return value.strip().strip('"').strip("'") or None
    return None


def request(path: str, token: str, body: bytes | None = None, content_type: str | None = None):
    headers = {"X-Api-Token": token, "User-Agent": "ThirstWasTaken2 release publisher"}
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(f"{CURSEFORGE}{path}", data=body, headers=headers,
                                 method="POST" if body else "GET")
    with urllib.request.urlopen(req, timeout=300) as response:
        return json.load(response)


def game_version_ids(token: str) -> dict[str, int]:
    """Every name an upload tags a file with, to CurseForge's id for it.

    Minecraft releases are read only from the `minecraft-*` version types: the same name also appears
    under Bukkit's and the addons' types, and those ids would tag the file as something else.
    """
    types = {t["id"]: t["slug"] for t in request("/game/version-types", token)}
    ids: dict[str, int] = {}
    for version in request("/game/versions", token):
        slug = types.get(version["gameVersionTypeID"], "")
        if slug.startswith("minecraft-") or slug in ("modloader", "java", "environment"):
            ids.setdefault(version["name"], version["id"])
    return ids


def java_version(node: Node) -> str:
    """26.1 and later run on Java 25, 1.21.x on Java 21, as the build's toolchains do."""
    return "Java 21" if node.minecraft.startswith("1.") else "Java 25"


def tags(node: Node) -> list[str]:
    return [*node.game_versions, LOADERS[node.loader], java_version(node), "Client", "Server"]


def relations(props: dict, node: Node) -> list[dict]:
    version = node.name.removesuffix("-neoforge")
    table = props.get(node.loader, {}).get(version, {}).get("deps", {})
    shared = props.get(version, {}).get("deps", {})
    return [{"slug": slug, "type": kind} for key, (slug, kind) in RELATIONS.items()
            if key in table or key in shared]


def multipart(metadata: dict, jar: Path) -> tuple[bytes, str]:
    boundary = uuid.uuid4().hex
    body = bytearray()
    body += f"--{boundary}\r\n".encode()
    body += b'Content-Disposition: form-data; name="metadata"\r\n'
    body += b"Content-Type: application/json\r\n\r\n"
    body += json.dumps(metadata).encode("utf-8") + b"\r\n"
    body += f"--{boundary}\r\n".encode()
    body += f'Content-Disposition: form-data; name="file"; filename="{jar.name}"\r\n'.encode()
    body += b"Content-Type: application/java-archive\r\n\r\n"
    body += jar.read_bytes() + b"\r\n"
    body += f"--{boundary}--\r\n".encode()
    return bytes(body), f"multipart/form-data; boundary={boundary}"


def main() -> None:
    parser = argparse.ArgumentParser(description="Upload a ThirstWasTaken2 release to CurseForge.")
    parser.add_argument("--dry-run", action="store_true", help="print every upload and send nothing")
    parser.add_argument("--only", action="append", metavar="NODE", help="upload only this node")
    parser.add_argument("--project-id", help="overrides CURSEFORGE_PROJECT_ID")
    args = parser.parse_args()

    props = tomllib.loads(PROPERTIES.read_text(encoding="utf-8"))
    mod_version = props["mod"]["version"]
    changelog = changelog_section(mod_version)
    nodes = read_nodes(props, mod_version)
    if args.only:
        unknown = set(args.only) - {node.name for node in nodes}
        if unknown:
            fail(f"no such node: {', '.join(sorted(unknown))}")
        nodes = [node for node in nodes if node.name in args.only]

    missing = [node.jar.name for node in nodes if not node.jar.exists()]
    if missing:
        fail("not in build/libs: " + ", ".join(missing))

    token = env_value("CURSEFORGE_TOKEN")
    if not token:
        fail(f"no CURSEFORGE_TOKEN in the environment or in {ENV_FILE.name}")
    project_id = args.project_id or env_value("CURSEFORGE_PROJECT_ID")
    if not project_id and not args.dry_run:
        fail(f"no CURSEFORGE_PROJECT_ID in {ENV_FILE.name}; create the project on "
             "https://authors.curseforge.com first")
    ids = game_version_ids(token)

    print(f"ThirstWasTaken2 {mod_version}, {len(nodes)} files, project {project_id or '(none)'}\n")
    for node in nodes:
        number = version_number(node, mod_version)
        names = tags(node)
        unknown = [name for name in names if name not in ids]
        if unknown:
            fail(f"CurseForge has no game version called {', '.join(unknown)}")
        links = relations(props, node)
        print(f"  {number}  ({node.name})")
        print(f"    file      {node.jar.name}, {node.jar.stat().st_size // 1024} KiB")
        print(f"    tags      {', '.join(names)}")
        print(f"    mods      {', '.join(link['slug'] for link in links) or 'none'}")
        if args.dry_run:
            print("    -> would upload\n")
            continue
        metadata = {
            "changelog": changelog,
            "changelogType": "markdown",
            "displayName": f"{TITLE} {number}",
            "gameVersions": [ids[name] for name in names],
            "releaseType": "release",
        }
        if links:
            metadata["relations"] = {"projects": links}
        body, content_type = multipart(metadata, node.jar)
        try:
            created = request(f"/projects/{project_id}/upload-file", token, body, content_type)
        except urllib.error.HTTPError as error:
            detail = error.read().decode("utf-8", "replace")
            fail(f"CurseForge refused `{number}` ({error.code}): {detail}")
        print(f"    -> uploaded, file id {created['id']}\n")

    if args.dry_run:
        print("Dry run: nothing was uploaded.")


if __name__ == "__main__":
    main()
