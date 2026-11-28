package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Consumer;

/**
 * Registers every {@link DtGameTest} ({@link DtGameTests}) as a 26.x test function plus a test instance.
 * Only when NeoForge has GameTests on (dev runs and the GameTest server), so a normal game's registries
 * never see them.
 */
public final class DtGameTestRegistry {

    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, DungeonTotems.MOD_ID);

    private static List<DtGameTests.Test> tests = List.of();

    private DtGameTestRegistry() {}

    public static void register(IEventBus modBus) {
        if (!GameTestHooks.isGametestEnabled()) return;
        tests = DtGameTests.all();
        for (DtGameTests.Test test : tests) FUNCTIONS.register(test.id().getPath(), test::function);
        FUNCTIONS.register(modBus);
        modBus.addListener(DtGameTestRegistry::registerInstances);
    }

    private static void registerInstances(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(DungeonTotems.id("default"));
        for (DtGameTests.Test test : tests) {
            event.registerTest(test.id(), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, test.id()), test.data(environment)));
        }
    }
}
