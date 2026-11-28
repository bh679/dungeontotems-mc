#!/usr/bin/env python3
"""Convert Dungeon Totems' data files from main's 1.21.1 format to 1.20.1 (Forge 47), in place.

Run after copying data from main (for example output of make_advancements.py) into
common/src/main/resources/data (Forge-only data, the loot modifiers, lives in forge/src/main/resources/data).
Idempotent: already-converted files are left alone.

What changes between the two versions:
  - folders back to 1.20.1's plural names (advancements, structures, tags/items, tags/entity_types);
    the morph_forms data map moves to data/dungeontotems/morph_forms/; neoforge/ -> forge/
  - neoforge:conditions -> conditions (Forge's ConditionalAdvancement key), plus the same check as
    fabric:load_conditions for Fabric (each loader ignores the other's key)
  - advancement icons: {"id": x} -> {"item": x}
  - neoforge:loot_table_id -> forge:loot_table_id
  - mobs that 1.20.1 lacks are dropped from morph forms and advancement criteria
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "common/src/main/resources/data"
FORGE_DATA = ROOT / "forge/src/main/resources/data"
# Entities added after 1.20.1 that main may reference.
MISSING_MOBS = {"minecraft:armadillo", "minecraft:breeze", "minecraft:bogged", "minecraft:creaking"}


def write(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


# main (1.21.1) folder -> 1.20.1 folder, under data/
FOLDERS = {
    "dungeontotems/advancement": "dungeontotems/advancements",
    "dungeontotems/structure": "dungeontotems/structures",
    "dungeontotems/tags/item": "dungeontotems/tags/items",
    "dungeontotems/tags/entity_type": "dungeontotems/tags/entity_types",
    "dungeontotems/data_maps/entity_type": "dungeontotems/morph_forms",
    "neoforge/loot_modifiers": "forge/loot_modifiers",
}
# Forge-only data goes to forge/, out of common/ (Fabric does the loot swap in code).
FORGE_ONLY = ("forge/loot_modifiers", "dungeontotems/loot_modifiers")


def folders():
    """Moves files from main's folder names into 1.20.1's (a later copy from main overwrites)."""
    for old, new in FOLDERS.items():
        src, dst = DATA / old, (FORGE_DATA if new in FORGE_ONLY else DATA) / new
        if not src.is_dir():
            continue
        dst.mkdir(parents=True, exist_ok=True)
        for f in src.iterdir():
            f.replace(dst / f.name)
        src.rmdir()
    for folder in FORGE_ONLY:
        src = DATA / folder
        if src.is_dir():
            (FORGE_DATA / folder).mkdir(parents=True, exist_ok=True)
            for f in src.iterdir():
                f.replace(FORGE_DATA / folder / f.name)
            src.rmdir()
    for empty in (DATA / "dungeontotems/data_maps", DATA / "neoforge"):
        if empty.is_dir() and not any(empty.iterdir()):
            empty.rmdir()


def advancements():
    for path in sorted((DATA / "dungeontotems/advancements").glob("*.json")):
        d = json.loads(path.read_text())
        if "neoforge:conditions" in d:
            d = {"conditions": d.pop("neoforge:conditions"), **d}
        if "conditions" in d and "fabric:load_conditions" not in d:
            fabric = [{"condition": c["type"]} for c in d["conditions"]]
            d = {"conditions": d.pop("conditions"), "fabric:load_conditions": fabric, **d}
        icon = d.get("display", {}).get("icon")
        if icon and "id" in icon:
            icon["item"] = icon.pop("id")
        crit = d.get("criteria", {})
        drop = [k for k, v in crit.items() if v.get("conditions", {}).get("form") in MISSING_MOBS]
        for k in drop:
            del crit[k]
        d["requirements"] = [r for r in d.get("requirements", []) if not any(k in drop for k in r)]
        write(path, d)


def morph_forms():
    path = DATA / "dungeontotems/morph_forms/morph_forms.json"
    d = json.loads(path.read_text())
    d["values"] = {k: v for k, v in d["values"].items() if k not in MISSING_MOBS}
    write(path, d)


def loot_modifiers():
    for path in (FORGE_DATA / "dungeontotems/loot_modifiers").glob("*.json"):
        text = path.read_text().replace("neoforge:loot_table_id", "forge:loot_table_id")
        path.write_text(text)


def main():
    folders()
    advancements()
    morph_forms()
    loot_modifiers()
    print("converted", DATA)
    return 0


if __name__ == "__main__":
    sys.exit(main())
