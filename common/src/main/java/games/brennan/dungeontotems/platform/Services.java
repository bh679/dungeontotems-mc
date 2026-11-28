package games.brennan.dungeontotems.platform;

import java.util.ServiceLoader;

/** The loader's implementations of {@link Platform} and {@link TestHooks}, from {@code META-INF/services}. */
public final class Services {

    public static final Platform PLATFORM = load(Platform.class);

    private Services() {}

    /** Loaded on first use: only tests touch it. */
    public static TestHooks testHooks() {
        return TestHooksHolder.INSTANCE;
    }

    static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
            .orElseThrow(() -> new IllegalStateException("No " + type.getName() + " for this loader"));
    }

    private static final class TestHooksHolder {
        static final TestHooks INSTANCE = load(TestHooks.class);
    }
}
