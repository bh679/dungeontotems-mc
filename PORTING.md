# Dungeon Totems for Minecraft 26.3 (NeoForge and Fabric)

This branch (`mc26.3`) is the 26.3 line of Dungeon Totems, ported from main (NeoForge 1.21.1).
It runs on **NeoForge 26.3** (built against `26.3.0.118`) and **Fabric 26.3** (Loader 0.20.1, Fabric API
`0.167.0+26.3`), Java 25. Gradle 9.8.2 (Loom 1.19 needs at least 9.8).

**Dungeon Train never uses this line.** Its releases are tagged `v<version>+mc26.3` with the jars
`dungeontotems-neoforge-26.3-<version>.jar` and `dungeontotems-fabric-26.3-<version>.jar`; Dungeon Train resolves only `dungeontotems-neoforge-<v>.jar`
under `v<v>`, and its CurseForge dependency picks the 1.21.1 file. Never rename this jar to the bare
neoforge pattern, never tag it plain `v<version>`, and never mark its GitHub release latest.

## Layout

MultiLoader: the mod is written once, in `common/`, and each loader is a thin shell around it.

| Where | What |
|---|---|
| `common/` | The whole mod, plain Minecraft only (no loader imports): compiled on its own against NeoForm (proving it), and its sources and resources also compiled into each loader's jar. |
| `common/.../event/` | The mod's own game events (`DtEvents`, `DtClientEvents`), named after NeoForge's (`LivingEvents.Death`, `PlayerEvents.RightClickBlock`...) with the same getters, so handlers read as they did on main. Handlers register by priority in `DungeonTotemsListeners` / `client/ClientListeners`. |
| `common/.../platform/` | What only a loader can do, found by `ServiceLoader` (`Services`): `Platform` (registries, per-entity data, network, the morph table, mod-loaded checks) and `TestHooks` (GameTests and self-tests fire each loader's real hook). |
| `neoforge/` | `DungeonTotemsNeoForge` (`@Mod`), `neoforge/NeoForgeEvents` + `NeoForgeClientEvents` (one NeoForge listener per common priority in use, so the order against other mods is unchanged), `NeoForgePlatform` (deferred registers, attachments, the `morph_forms` data map), the evoker loot modifier, `DtGameTestRegistry`, `neoforge.mods.toml`, and the two unit tests that need Minecraft on the test classpath. |
| `fabric/` | `DungeonTotemsFabric` / `DungeonTotemsFabricClient`, `FabricPlatform` (built-in registries, Data Attachments, Fabric networking, `MorphFormTable` reading the data map's own JSON), Fabric API callbacks where one fires where NeoForge's event does, and `fabric/mixin/` for every other event (table below). `FabricGameTests` registers the GameTests the way Fabric API's own module does. |

### Where each event comes from on Fabric

Each mixin sits at the spot NeoForge's patch fires its event (compare the NeoForge sources in
`neoforge/build/moddev/artifacts/minecraft-patched-*-sources.jar`), so timing matches main.

| Common event | Fabric |
|---|---|
| `Death` | `LivingEntity#die` head (after its guard), `ServerPlayer#die` after its death game event. **Not** `ALLOW_DEATH`, which fires before vanilla's totem. |
| `IncomingDamage` | `hurtServer` after the invulnerability checks (at `isSleeping`), amount changeable |
| `DamagePost` | end of `hurtServer`, a fatal hit included (Fabric's `AFTER_DAMAGE` skips those); health damage from `actuallyHurt` |
| `UseTotem`, effects added/removed/expired, `Fall`, `Jump`, `Breathe`, item use start/finish/stop | `LivingEntityHooksMixin` |
| `PlayerTickPre/Post`, `BreakSpeed`, `EntityInteract`, `AttackEntity` | `PlayerHooksMixin` |
| `ChangeTarget` | `Mob#setTarget` and brain mobs' `StartAttacking` |
| `Size`, `Trade`, `AnimalTame`, `ContainerOpen`, `AdvancementEarn` | `EntityHooksMixin`, `AbstractVillagerHooksMixin`, the tame-roll mixins (wolf, cat, parrot, ocelot, horses), `ServerPlayerHooksMixin`, `PlayerAdvancementsHooksMixin` |
| `LeftClickBlock`, `RightClickBlock` | `ServerPlayerGameModeHooksMixin` (and the client's `MultiPlayerGameModeHooksMixin`) |
| `RightClickItem`, `BreakBlock`, login/logout, clone, respawn, tracking, join/leave level, commands, server started | Fabric API (`UseItemCallback`, `PlayerBlockBreakEvents.BEFORE`, `ServerPlayerEvents`, `EntityTrackingEvents`, `ServerEntityEvents`, ...) |
| Client: tick, chat sent, system chat, item tooltips | Fabric API |
| Client: keys, player render, first-person arm, container foreground, typed characters, player chat, effect tooltips and timer icons | `fabric/mixin/client/` |

Data on Fabric: the evoker drop is `LootTableEvents.MODIFY_DROPS`; the advancement tab's load condition is
a Fabric resource condition with the same id (`make_advancements.py` writes both loaders' keys); the morph
table is the same `data_maps/entity_type/morph_forms.json`. Jade's plugin is the same class, found by
Fabric's `jade` entrypoint.

`mod_version` is the main version this line was ported from (0.26.0).

## Bringing a change across from main

1. Copy the changed files from main into `common/src/...`. A new `@SubscribeEvent` becomes a handler taking
   the common event (`event/`) and a line in `DungeonTotemsListeners`; a NeoForge event the mod never used
   before needs a common event, a bridge line in `NeoForgeEvents` and a Fabric hook at NeoForge's spot.
2. Fix what no longer compiles with the table below; the 26.3 sources are in
   `neoforge/build/moddev/artifacts/minecraft-patched-*-sources.jar` after a build.
3. `./gradlew build`, `./gradlew :neoforge:runGameTestServer` and `./gradlew :fabric:runGameTestServer`
   (all GameTests must pass on both).

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
Every self-test runs on Fabric too: `./gradlew :fabric:runSelfTest -D...` (run dir `fabric/run-selftest`).

**Fabric differences (small):** no first-person `RenderHandEvent` backstop (the hand mixin already skips
the whole pass when morphed); no living-conversion event (`Platform#canLivingConvert` is always true);
GameTests run in vanilla's `minecraft:default` environment rather than `dungeontotems:default`.

## Releasing

```bash
gh workflow run release.yml --ref mc26.3 -f tag=v<mod_version>+mc26.3
```

It refuses any other branch or tag shape, builds, makes a GitHub release that is never marked latest,
and uploads both jars to the same CurseForge (1800270) and Modrinth (`dungeontotems`) projects as main,
tagged Minecraft 26.3: the NeoForge jar with the NeoForge loader (version `<v>+mc26.3`), the Fabric jar
with the Fabric loader and Fabric API as a required dependency (version `<v>+mc26.3-fabric`). Files only: no listing text or settings change.
Nothing is released without Brennan's yes.
