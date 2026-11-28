package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Development only ({@code ./gradlew runSelfTest -Dpop=true}): a real client is saved by one totem after
 * another, in a fresh flat world. Each pop is screenshotted in first person as the totem flies up
 * ({@code screenshots/selftest_pop_<curse>_0_pop.png}) and again from behind once its curse has set in
 * ({@code _1_after}), and reported to {@code selftest/pop.txt}; then the client quits.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class PopSelfTest {

    static final boolean ENABLED = !System.getProperty("dungeontotems.selftest.pop", "").isEmpty();
    private static final List<Curse> CURSES = List.of(Curse.WEAKNESS, Curse.HEARTBREAK, Curse.BLINDNESS, Curse.MORPHING, Curse.ZOMBIFIED);
    private static final int WARM_UP_TICKS = 80;
    private static final int STEP_TICKS = 80;
    private static final int POP_AT = 10;
    /** The totem's own flight: vanilla's item activation is 40 ticks; a few in, it fills the screen. */
    private static final int POP_SHOT_AT = POP_AT + 6;
    private static final int AFTER_SHOT_AT = POP_AT + 60;
    private static final long NOON = 6000L;
    private static final float LETHAL = 1000.0F;
    private static final List<String> REPORT = new ArrayList<>();
    private static int tick;
    private static int failures;
    private static boolean worldRequested;

    private PopSelfTest() {}

    private static void createWorld(Minecraft minecraft) {
        if (!(minecraft.gui.screen() instanceof TitleScreen || minecraft.gui.screen() instanceof CreateWorldScreen)) return;
        worldRequested = true;
        LevelSettings settings = new LevelSettings("Pop Self Test " + System.currentTimeMillis(), GameType.SURVIVAL,
            new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(settings.levelName(), settings, WorldOptions.defaultWithRandomSeed(),
            registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            new TitleScreen());
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (minecraft.player == null && server == null && !worldRequested) createWorld(minecraft);
        if (minecraft.player == null || server == null) return;
        minecraft.options.pauseOnLostFocus = false; // an unfocused window would pause the server for good
        if (minecraft.gui.screen() != null) minecraft.gui.setScreen(null);
        tick++;
        ServerPlayer player = server.getPlayerList().getPlayer(minecraft.player.getUUID());
        if (player == null || tick < WARM_UP_TICKS) return;
        int t = tick - WARM_UP_TICKS;
        int index = t / STEP_TICKS;
        if (index >= CURSES.size()) {
            finish(minecraft);
            return;
        }
        Curse curse = CURSES.get(index);
        int at = t % STEP_TICKS;
        if (at == 0) {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            server.execute(() -> {
                player.removeAllEffects();
                player.setHealth(player.getMaxHealth());
                player.level().getServer().clockManager().setTotalTicks(
                    player.level().registryAccess().get(WorldClocks.OVERWORLD).orElseThrow(), NOON);
                player.setGameMode(GameType.SURVIVAL);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TotemRegistries.TOTEMS.get(curse).get()));
            });
        }
        if (at == POP_AT) {
            server.execute(() -> {
                player.damageCooldownTime = 0;
                player.hurt(player.damageSources().generic(), LETHAL);
            });
        }
        if (at == POP_SHOT_AT) shot(minecraft, curse, "0_pop");
        if (at == POP_SHOT_AT + 1) minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (at == AFTER_SHOT_AT) {
            shot(minecraft, curse, "1_after");
            server.execute(() -> {
                boolean saved = player.isAlive() && player.getMainHandItem().isEmpty();
                boolean cursed = curse.effect() == null || player.hasEffect(curse.effect());
                if (!saved || !cursed) failures++;
                REPORT.add(String.format("%s  %-12s alive %s, totem used %s, cursed %s, health %.1f", saved && cursed ? "PASS" : "FAIL",
                    curse.name(), player.isAlive(), player.getMainHandItem().isEmpty(), cursed, player.getHealth()));
            });
        }
    }

    private static void shot(Minecraft minecraft, Curse curse, String moment) {
        String name = "selftest_pop_" + curse.itemName() + "_" + moment + ".png";
        Screenshot.grab(minecraft.gameDirectory, name, minecraft.gameRenderer.mainRenderTarget(), 1, message -> {});
    }

    private static void finish(Minecraft minecraft) {
        REPORT.add(failures == 0 ? "ALL PASSED" : failures + " FAILED");
        try {
            Path out = minecraft.gameDirectory.toPath().resolve("selftest");
            Files.createDirectories(out);
            Files.write(out.resolve("pop.txt"), REPORT);
        } catch (IOException e) {
            DungeonTotems.LOGGER.error("Pop self-test report couldn't be written", e);
        }
        DungeonTotems.LOGGER.info("Pop self-test done:\n{}", String.join("\n", REPORT));
        minecraft.stop();
    }
}
