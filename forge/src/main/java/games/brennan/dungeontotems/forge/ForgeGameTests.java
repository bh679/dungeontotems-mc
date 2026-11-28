package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.gametest.DungeonTotemsGameTests;
import games.brennan.dungeontotems.gametest.LoaderHookGameTests;
import games.brennan.dungeontotems.gametest.StackingGameTests;
import games.brennan.dungeontotems.gametest.ZombieGameTests;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/** Registers the shared GameTest classes (Fabric lists the same four as {@code fabric-gametest} entrypoints). */
final class ForgeGameTests {

    private ForgeGameTests() {}

    static void register(IEventBus modBus) {
        modBus.addListener((RegisterGameTestsEvent event) -> {
            event.register(DungeonTotemsGameTests.class);
            event.register(StackingGameTests.class);
            event.register(ZombieGameTests.class);
            event.register(LoaderHookGameTests.class);
        });
    }
}
