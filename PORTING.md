# Dungeon Totems for Minecraft 1.20.1 (Forge, NeoForge, Fabric)

This branch (`mc1.20.1`) is the 1.20.1 line of Dungeon Totems, ported from main (NeoForge 1.21.1).
Two jars: one runs on **Forge 47** and **NeoForge 20.1**, the other on **Fabric** (Loader 0.16.10+, Fabric API 0.92.6+).

**Dungeon Train never uses this line.** Its releases are tagged `v<version>+mc1.20.1` with the jars
`dungeontotems-forge-1.20.1-<version>.jar` and `dungeontotems-fabric-1.20.1-<version>.jar`; Dungeon Train
resolves only `dungeontotems-neoforge-<v>.jar` under `v<v>`, and its CurseForge dependency picks the 1.21.1
file. Never rename either jar to the neoforge pattern, never tag it plain `v<version>`, and never mark its
GitHub release latest.

## Layout

| Where | What |
|---|---|
| `common/` | The whole mod, loader-free: gameplay, client, GameTests, data and assets, the shared mixins (`dungeontotems.mixins.json`), unit tests. Compiled into both jars. |
| `common/.../platform/event/` | The mod's own event bus (`TotemEvents`), with Forge-shaped event classes (`LivingHurtEvent`, `TickEvent.PlayerTickEvent`...) and `@SubscribeEvent`/`@EventBusSubscriber`. Every subscriber class is listed in `CommonEvents` (a unit test checks). |
| `common/.../platform/Services` | What common asks of the loader, by ServiceLoader: `RegistryService`, `NetworkService`, `PlatformService`, `LoaderHooks`. |
| `forge/` | The Forge entry point, `ForgeEventBridge` (each Forge event forwarded per priority, so HIGHEST/LOWEST keep their place among other mods), services, GLM, the advancement condition, `dungeontotems.forge.mixins.json`. |
| `fabric/` | The Fabric entry points, Fabric API callbacks (`FabricEvents`, `DungeonTotemsFabricClient`), services, and mixins at Forge 47's exact patch points (`fabric/mixin`, `dungeontotems.fabric.mixins.json`). |
| `scripts/to_1_20_1.py` | Converts data copied from main to 1.20.1's format and folders. |
| `scripts/neoforge_to_forge.py` | First-pass Java renames (imports, simple names) for code copied from main. |

`mod_version` names main's minor this line was ported from (0.26); the patch is this line's own changes.

## Bringing a change across from main

1. Copy the changed files from main into `common/src/...`.
2. `python3 scripts/neoforge_to_forge.py common/src` for the renames, then fix what's left by hand with
   the table below. Forge event imports become `games.brennan.dungeontotems.platform.event.<Name>` (same
   names); a new event type needs a class there, a forward in `ForgeEventBridge` and a Fabric hook. A new
   `@EventBusSubscriber` class goes in `CommonEvents`. Anything else loader-specific goes behind `Services`. A main `PlayerTickEvent.Pre`/`Post` handler needs `if (event.phase != TickEvent.Phase.START/END) return;`
   and `event.player` for `event.getEntity()`: Forge's one tick event fires twice.
3. `python3 scripts/to_1_20_1.py` after copying any data files.
4. `./gradlew build`, `./gradlew :forge:runGameTestServer` and `./gradlew :fabric:runGametest` (all GameTests
   must pass on both).

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

## Fabric

| Forge 47 | Fabric |
|---|---|
| `LivingHurtEvent` / `LivingDamageEvent` | `@ModifyVariable` in `actuallyHurt` (`LivingEntity` and `Player`, which overrides it) at Forge's spots: the first read of the amount (before armour), and just before health drops (vanilla keeps that amount in the argument's slot: ordinals from javap). Fabric API's `ALLOW_DAMAGE` fires before armour and `ALLOW_DEATH` before the vanilla totem, so neither is used |
| `LivingDeathEvent` | `die` HEAD (`LivingEntity`; `ServerPlayer` after its death game event) |
| `PlayerTickEvent`, login/logout | `Player.tick` HEAD/TAIL; `PlayerList.placeNewPlayer` TAIL / `remove` HEAD |
| Effect added/removed/expired, milk | `LivingEntity` mixins; milk keeps an uncurable curse (`MilkBucketItemMixin` → `LoaderHooks.cureWithMilk`) |
| Target, tame, trade, container open, advancement earned, break speed, size, use item, fall, jump, breathe | Mixins at Forge's patch lines (see each class's javadoc) |
| Right-click block (`useBlock`/`useItem`) | `ServerPlayerGameMode.useItemOn` / `MultiPlayerGameMode.performUseItemOn` |
| Right-click item, left-click block, block break, respawn, clone, tracking, entity load/unload, commands, tooltip, chat sent, screen render, client tick | Fabric API callbacks |
| `SimpleChannel` (protocol 6) | One channel per packet, `dungeontotems:v6/<name>`; a client without them is disconnected after 5 s (`ProtocolCheck`) |
| GLM (evoker totems) | `dungeontotems:swap_totems` loot function on every pool of the evoker table (`LootTableEvents.MODIFY`) |
| Advancement `conditions` | `fabric:load_conditions` with the same id (`to_1_20_1.py` writes both) |
| `forge:step_height_addition` | `dungeontotems:step_height`, added in `Entity.maxUpStep` for players |
| Rage/Lonely timer icons (client extension) | `GuiEffectsMixin`, `EffectRenderingInventoryScreenMixin` |
| GameTest `@GameTestHolder` | `fabric-gametest` entrypoints; `GameTestRegistryMixin` gives the `empty` template our namespace (on Forge, `ForgeGameTestHooksMixin`) |
| Jade `@WailaPlugin` | `jade` entrypoint, same class |
| PlayerMob gift compat | Off (PlayerMob is Forge-only) |

GameTests: `gametest/LoaderHookGameTests` drives each loader hook through vanilla's own entry points (the
other suites post events straight to the bus). Fabric's GameTest world is vanilla's, which puts the tests in a
pocket under stone: a test that needs open sky or open air makes it (`openTheSky`, the gait lanes).

Runs: `./gradlew :fabric:runClient`, `:fabric:runJoinServer` (a client joining `runServer` as "Tester"), `:fabric:runServer` (in `fabric/run-server`), `:fabric:runGametest`,
`:fabric:runSelfTest` (same `-D` options as Forge's; needs a clean "New World" save in `fabric/run-selftest/saves`).

## Releasing

```bash
gh workflow run release.yml --ref mc1.20.1 -f tag=v<mod_version>+mc1.20.1
```

It refuses any other branch or tag shape, builds, makes a GitHub release with both jars that is never
marked latest, and uploads them to the same CurseForge and Modrinth projects as main, tagged Minecraft
1.20.1: the Forge jar with the Forge and NeoForge loaders, the Fabric jar with Fabric (Fabric API required).
Files only: no listing text or settings change.
