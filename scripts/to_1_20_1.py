#!/usr/bin/env python3
"""Convert Dungeon Totems' data files from main's 1.21.1 format to 1.20.1 (Forge 47), in place.

Run after copying data from main (for example output of make_advancements.py) into
forge/src/main/resources/data. Idempotent: already-converted files are left alone.

What changes between the two versions:
  - neoforge:conditions -> conditions (Forge's ConditionalAdvancement key)
  - advancement icons: {"id": x} -> {"item": x}
  - neoforge:loot_table_id -> forge:loot_table_id
  - mobs that 1.20.1 lacks are dropped from morph forms and advancement criteria
"""
import json
import pathlib
import sys

DATA = pathlib.Path(__file__).resolve().parent.parent / "forge/src/main/resources/data"
# Entities added after 1.20.1 that main may reference.
MISSING_MOBS = {"minecraft:armadillo", "minecraft:breeze", "minecraft:bogged", "minecraft:creaking"}


def write(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


def advancements():
    for path in sorted((DATA / "dungeontotems/advancements").glob("*.json")):
        d = json.loads(path.read_text())
        if "neoforge:conditions" in d:
            d = {"conditions": d.pop("neoforge:conditions"), **d}
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
    for path in (DATA / "dungeontotems/loot_modifiers").glob("*.json"):
        text = path.read_text().replace("neoforge:loot_table_id", "forge:loot_table_id")
        path.write_text(text)


def main():
    advancements()
    morph_forms()
    loot_modifiers()
    print("converted", DATA)
    return 0


if __name__ == "__main__":
    sys.exit(main())
