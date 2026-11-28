# Dungeon Totems for Minecraft 1.20.1 (Forge)

This branch (`mc1.20.1`) is the 1.20.1 line of Dungeon Totems, ported from main (NeoForge 1.21.1).
One jar runs on **Forge 47** and **NeoForge 20.1**.

**Dungeon Train never uses this line.** Its releases are tagged `v<version>+mc1.20.1` with the jar
`dungeontotems-forge-1.20.1-<version>.jar`; Dungeon Train resolves only `dungeontotems-neoforge-<v>.jar`
under `v<v>`, and its CurseForge dependency picks the 1.21.1 file. Never rename this jar to the neoforge
pattern, never tag it plain `v<version>`, and never mark its GitHub release latest.

## Layout

| Where | What |
|---|---|
| `common/` | Vanilla-only code (no loader imports), compiled into each loader's jar: entity data (`data/`), the curse marks (`curse/CurseMarks`), packets (`net/*Payload`), `util/`, and their mixins. A Fabric port reuses it. |
| `forge/` | Everything else: the mod, its events, registries, client, GameTests, resources. |
| `scripts/to_1_20_1.py` | Converts data copied from main to 1.20.1's format and folders. |
| `scripts/neoforge_to_forge.py` | First-pass Java renames (imports, simple names) for code copied from main. |

`mod_version` is the main version this line was ported from (0.26.0).

## Bringing a change across from main

1. Copy the changed files from main into `forge/src/...` (or `common/` if the file lives there here).
2. `python3 scripts/neoforge_to_forge.py forge/src` for the renames, then fix what's left by hand with
   the table below. A main `PlayerTickEvent.Pre`/`Post` handler needs `if (event.phase != TickEvent.Phase.START/END) return;`
   and `event.player` for `event.getEntity()`: Forge's one tick event fires twice.
3. `python3 scripts/to_1_20_1.py` after copying any data files.
4. `./gradlew build` and `./gradlew :forge:runGameTestServer` (all GameTests must pass).

## What differs from main

| Main (NeoForge 1.21.1) | Here (Forge 47, 1.20.1) |
|---|---|
| Attachment types | `TotemData`/`DataKey` (common): an `Entity` mixin keeps them, saved in the entity's NBT, kept on return from the End, lost on death (as main) |
| `CustomPacketPayload` + `StreamCodec` | Records with `write`/`read(FriendlyByteBuf)` (common), on one `SimpleChannel` (`TotemNetwork`). Protocol version still 6 |
| `morph_forms` data map | `MorphFormTable` reload listener, same JSON, at `data/<ns>/morph_forms/*.json` |
| Effect cures (`PROTECTED_BY_TOTEM`) | `CurseMarks`: a flag on each curse instance, saved with it; both our pop and vanilla's totem clear every effect but curses |
| Player `jump_strength` / `step_height` attributes | `dungeontotems:jump_strength` (read by a `getJumpPower` mixin) and Forge's `step_height_addition`; a mob's step height is read off a throwaway mob |
| Modifiers keyed by `ResourceLocation` | `Modifiers`: a fixed UUID per id |
| Codec-based advancement triggers, `neoforge:conditions` | `SimpleCriterionTrigger` + JSON, Forge `conditions` (Forge's `CraftingHelper` condition serializer) |
| `GatherEffectScreenTooltipsEvent` | `EffectRenderingInventoryScreenMixin` adds the curse hints to the narrow list's tooltip |
| `LivingIncomingDamageEvent` / `LivingDamageEvent.Post` | `LivingHurtEvent` / `LivingDamageEvent` |
| `#minecraft:can_breathe_under_water`, `#minecraft:parrot_food` | Fixed lists (`WaterLife`, `ZombieLimits`): 1.20.1 has no such tags |
| Jade 15 | Jade 11 (`ZombieJadePlugin`): no line re-measure needed |
| MixinExtras built in | MixinExtras bundled (jarJar) |
| `makeMockServerPlayerInLevel` | `gametest/MockPlayers`: Forge 47's login needs the mock connection's netty channel |
| GameTest world from vanilla | `run-gametest/server.properties` written per run (flat, Normal) + `GameTestWorld` game rules |

**Left out:** the armadillo morph form and its roll move (1.20.1 has no armadillo), dropped from the
morph table, the Great and Small advancement and the self-tests. Everything else matches main 0.26.0.

**Not yet in `common/`:** most of the mod still lives in `forge/` because it is tied to Forge's events and
registries. The Fabric port moves code to `common/` behind loader seams as it needs to.

## Releasing

```bash
gh workflow run release.yml --ref mc1.20.1 -f tag=v<mod_version>+mc1.20.1
```

It refuses any other branch or tag shape, builds, makes a GitHub release that is never marked latest,
and uploads the jar to the same CurseForge and Modrinth projects as main, tagged Minecraft 1.20.1 with
the Forge and NeoForge loaders. Files only: no listing text or settings change.
