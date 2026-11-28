package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Registers every {@link DtGameTest} method as a 26.x test function plus a test instance on the
 * {@code dungeontotems:empty} structure, named after the method in snake case
 * ({@code dungeontotems:curse_totem_saves_and_curses}). Only when NeoForge has GameTests on
 * (dev runs and the GameTest server), so a normal game's registries never see them.
 */
public final class DtGameTestRegistry {

    private static final List<Class<?>> TEST_CLASSES =
        List.of(DungeonTotemsGameTests.class, StackingGameTests.class, ZombieGameTests.class);

    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
        DeferredRegister.create(Registries.TEST_FUNCTION, DungeonTotems.MOD_ID);

    private record Registered(DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> function, int maxTicks) {}

    private static final List<Registered> TESTS = new ArrayList<>();

    private DtGameTestRegistry() {}

    public static void register(IEventBus modBus) {
        if (!GameTestHooks.isGametestEnabled()) return;
        Set<String> names = new HashSet<>();
        for (Class<?> type : TEST_CLASSES) {
            Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(DtGameTest.class))
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> registerFunction(method, names));
        }
        FUNCTIONS.register(modBus);
        modBus.addListener(DtGameTestRegistry::registerInstances);
    }

    private static void registerFunction(Method method, Set<String> names) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("@DtGameTest method must be static: " + method);
        }
        String name = snakeCase(method.getName());
        if (!names.add(name)) throw new IllegalStateException("Two GameTests named " + name);
        var function = FUNCTIONS.register(name, () -> helper -> invoke(method, helper));
        TESTS.add(new Registered(function, method.getAnnotation(DtGameTest.class).timeoutTicks()));
    }

    private static void invoke(Method method, GameTestHelper helper) {
        try {
            method.invoke(null, helper);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            if (e.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void registerInstances(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(DungeonTotems.id("default"));
        for (Registered test : TESTS) {
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(environment, Level.OVERWORLD,
                DungeonTotems.id("empty"), test.maxTicks(), 0, true, Rotation.NONE, false, 1, 1, false, 0);
            event.registerTest(test.function().getId(), new FunctionGameTestInstance(test.function().getKey(), data));
        }
    }

    static String snakeCase(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT);
    }
}
