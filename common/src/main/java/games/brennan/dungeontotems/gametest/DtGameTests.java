package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Every {@link DtGameTest} method as a test: its id (the method in snake case,
 * {@code dungeontotems:curse_totem_saves_and_curses}), its function and its test data on the
 * {@code dungeontotems:empty} structure. Each loader registers them its own way, only when GameTests are on.
 */
public final class DtGameTests {

    private static final List<Class<?>> TEST_CLASSES =
        List.of(DungeonTotemsGameTests.class, StackingGameTests.class, ZombieGameTests.class);

    /** One test. */
    public record Test(Identifier id, Consumer<GameTestHelper> function, int maxTicks) {
        public TestData<Holder<TestEnvironmentDefinition<?>>> data(Holder<TestEnvironmentDefinition<?>> environment) {
            return new TestData<>(environment, Level.OVERWORLD, DungeonTotems.id("empty"), maxTicks, 0, true,
                Rotation.NONE, false, 1, 1, false, 0);
        }
    }

    private DtGameTests() {}

    public static List<Test> all() {
        List<Test> tests = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Class<?> type : TEST_CLASSES) {
            Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(DtGameTest.class))
                .sorted(Comparator.comparing(Method::getName))
                .forEach(method -> tests.add(test(method, names)));
        }
        return List.copyOf(tests);
    }

    private static Test test(Method method, Set<String> names) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException("@DtGameTest method must be static: " + method);
        }
        String name = snakeCase(method.getName());
        if (!names.add(name)) throw new IllegalStateException("Two GameTests named " + name);
        return new Test(DungeonTotems.id(name), helper -> invoke(method, helper), method.getAnnotation(DtGameTest.class).timeoutTicks());
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

    static String snakeCase(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("([A-Z])([A-Z][a-z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
}
