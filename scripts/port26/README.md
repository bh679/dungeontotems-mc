# First-pass helpers for code copied from main (1.21.1) to this line (26.3)

Run on the copied files, then fix what is left by hand with `PORTING.md`'s table.

```bash
# unzip the 26.3 sources once (after a build): minecraft + neoforge
mkdir -p /tmp/mc263 /tmp/nf263
unzip -qo neoforge/build/moddev/artifacts/minecraft-patched-*-sources.jar -d /tmp/mc263
unzip -qo ~/.gradle/caches/modules-2/files-2.1/net.neoforged/neoforge/<neo_version>/*/neoforge-<neo_version>-sources.jar -d /tmp/nf263

python3 scripts/port26/fix_imports.py neoforge/src /tmp/mc263 /tmp/nf263   # moved classes, by simple name
python3 scripts/port26/renames.py neoforge/src                             # renamed methods and constants
```

`fix_imports.py` prints the imports it could not place. Neither script touches `EntityType.X`
constants (now `EntityTypes.X`), GameTest annotations or rendering; see `PORTING.md`.
