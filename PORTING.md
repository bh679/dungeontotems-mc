# Dungeon Totems for Minecraft 26.3 (NeoForge)

This branch (`mc26.3`) is the 26.3 line of Dungeon Totems, ported from main (NeoForge 1.21.1).
It runs on **NeoForge 26.3** (built against `26.3.0.118`), Java 25.

**Dungeon Train never uses this line.** Its releases are tagged `v<version>+mc26.3` with the jar
`dungeontotems-neoforge-26.3-<version>.jar`; Dungeon Train resolves only `dungeontotems-neoforge-<v>.jar`
under `v<v>`, and its CurseForge dependency picks the 1.21.1 file. Never rename this jar to the bare
neoforge pattern, never tag it plain `v<version>`, and never mark its GitHub release latest.

## Layout

The MultiLoader shape of the 1.20.1 line (`dev/port-1-20-1-forge`), so a Fabric 26.3 port can join:

| Where | What |
|---|---|
| `common/` | Vanilla-only code (no loader imports), compiled on its own against plain Minecraft (NeoForm) and also compiled into the loader jar: `curse/ZombieGibberish`, `morph/MobGaits`, `morph/MorphStats` (+ its test), `morph/FormAction`, `effect/CurseEffect`, `advancement/KeyTrigger`. The rest still needs NeoForge (events, registries, attachments); the Fabric port moves code here behind seams as it needs to. |
| `neoforge/` | Everything else: the mod, its events, registries, client, mixins, GameTests, resources. |

`mod_version` is the main version this line was ported from (0.26.0).

## Bringing a change across from main

1. Copy the changed files from main into `neoforge/src/...` (or `common/` if the file lives there here).
2. Fix what no longer compiles with the table below; the 26.3 sources are in
   `neoforge/build/moddev/artifacts/minecraft-patched-*-sources.jar` after a build.
3. `./gradlew build` and `./gradlew :neoforge:runGameTestServer` (all GameTests must pass).

## What differs from main

| Main (NeoForge 1.21.1) | Here (NeoForge 26.3) |
|---|---|
| `ResourceLocation`, `GuiGraphics`, `ClickType` | `Identifier`, `GuiGraphicsExtractor`, `ContainerInput` |
| `EntityType.PIG` etc. | `EntityTypes.PIG`; a type's tag test is `type.builtInRegistryHolder().is(tag)` |
| Effect cures (`PROTECTED_BY_TOTEM`, milk) | Gone in 26.x. `curse/CurseGuard` cancels `MobEffectEvent.Remove` for curses during a totem pop, and for uncurable curses while drinking milk; vanilla effects that are curses are marked in the `curse_marks` attachment |
| `@GameTest` / `@GameTestHolder` | `@DtGameTest`, registered as test functions + instances by `gametest/DtGameTestRegistry` (names are the method in snake case) |
| `makeMockServerPlayerInLevel` | `gametest/MockPlayers.mock`: 26.x pins the stock mock player to creative |
| `invulnerableTime` (hurt cooldown) | `damageCooldownTime` |
| `level.setDayTime` | the overworld clock (`MockPlayers.setDayTime`) |
| Player render with `MultiBufferSource` | Render states: `MorphRenderer` cancels the player's submit and submits the stand-in's own extracted state; first-person hands are `FirstPersonHandsAndItemsRenderer#submitHandsWithItems` (`ItemInHandRendererMixin`) |
| Effect icon hooks `renderInventoryIcon` / `renderGuiIcon` | `extractInventoryIcon` / `extractHudIcon` (`TimerCurseIcons`) |
| Wide effect list had no tooltip (`DrawnEffectRows`, `EffectRenderingInventoryScreenMixin`) | 26.x asks every row for a tooltip, so `CurseTooltips` only answers NeoForge's tooltip event; both classes and their test are gone |
| `ContainerScreenEvent.Render.Foreground` + z offset | `ScreenEvent.Render.Foreground` + `nextStratum()` (`ZombiePackOverlay`) |
| Loot modifier `conditions` array + `global_loot_modifiers.json` | One `condition` keyed by `type`; every `loot_modifiers/*.json` loads by itself |
| `models/item/*.json` only | Plus item definitions in `assets/dungeontotems/items/*.json` |
| Attachments `serialize(Codec)` | `serialize(codec.fieldOf("value"))` |
| Jade 15 `IElement`/`IBoxElement` | Jade 26.3 (`BoxElement`, `Tooltip.lines`, `JadeUI.text`), `compat/jade/ZombieJadePlugin` |
| Rabbit gait 0.68 / 5.55 b/s | 26.3 rabbits hop slower: 0.42 / 3.64 b/s (re-measured by `mobsWalkAtTheirOwnPace`) |

Everything else matches main 0.26.0, the armadillo included.

**Added on this line:** `client/PopSelfTest` (`./gradlew :neoforge:runSelfTest -Dpop=true`): a real client pops one totem after another and screenshots each.

## Releasing

```bash
gh workflow run release.yml --ref mc26.3 -f tag=v<mod_version>+mc26.3
```

It refuses any other branch or tag shape, builds, makes a GitHub release that is never marked latest,
and uploads the jar to the same CurseForge (1800270) and Modrinth (`dungeontotems`) projects as main,
tagged Minecraft 26.3 with the NeoForge loader. Files only: no listing text or settings change.
Nothing is released without Brennan's yes.
