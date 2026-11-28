package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.gametest.DtGameTest;
import games.brennan.dungeontotems.gametest.DtGameTests;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.List;

/**
 * Registers every {@link DtGameTest} ({@link DtGameTests}) the way Fabric API's GameTest module registers
 * its own {@code @GameTest}s: test functions while the mod sets up, test instances when the data registries
 * load ({@code mixin/FabricGameTestModInitializerMixin}). Only in dev runs or with {@code -Dfabric-api.gametest}.
 */
public final class FabricGameTests {

    /** Vanilla's default test environment, as Fabric's own tests use. */
    private static final ResourceKey<TestEnvironmentDefinition<?>> ENVIRONMENT =
        ResourceKey.create(Registries.TEST_ENVIRONMENT, Identifier.withDefaultNamespace("default"));

    private static List<DtGameTests.Test> tests = List.of();

    private FabricGameTests() {}

    static boolean enabled() {
        return System.getProperty("fabric-api.gametest") != null || FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    static void register() {
        if (!enabled()) return;
        tests = DtGameTests.all();
        for (DtGameTests.Test test : tests) Registry.register(BuiltInRegistries.TEST_FUNCTION, test.id(), test.function());
    }

    /** Adds the instances to the loading registries, before they freeze. */
    public static void registerInstances(Registry<GameTestInstance> instances, Registry<TestEnvironmentDefinition<?>> environments) {
        if (tests.isEmpty()) return;
        Holder<TestEnvironmentDefinition<?>> environment = environments.getOrThrow(ENVIRONMENT);
        for (DtGameTests.Test test : tests) {
            Registry.register(instances, test.id(), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, test.id()), test.data(environment)));
        }
    }
}
