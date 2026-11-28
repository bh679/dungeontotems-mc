package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import games.brennan.dungeontotems.platform.event.ServerStartedEvent;
import games.brennan.dungeontotems.platform.event.SubscribeEvent;
import games.brennan.dungeontotems.platform.event.EventBusSubscriber;

/**
 * Forge 47's GameTest server is a dedicated server on {@code run-gametest/server.properties} (flat, Normal:
 * written by Gradle's {@code prepareGameTestWorld}). This adds the rest of vanilla's GameTest world
 * ({@code GameTestServer.TEST_GAME_RULES}): no natural mob spawning, no weather. Only on the GameTest server.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
final class GameTestWorld {

    private GameTestWorld() {}

    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        if (!Boolean.getBoolean("forge.gameTestServer")) return;
        MinecraftServer server = event.getServer();
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        server.setDifficulty(Difficulty.NORMAL, true);
    }
}
