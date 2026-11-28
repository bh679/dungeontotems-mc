package games.brennan.dungeontotems.platform;

import java.util.ServiceLoader;

/**
 * What {@code common/} asks of the loader, each found once through {@link ServiceLoader}
 * ({@code META-INF/services} in the Forge and Fabric jars).
 */
public final class Services {

    public static final PlatformService PLATFORM = load(PlatformService.class);
    public static final RegistryService REGISTRY = load(RegistryService.class);
    public static final NetworkService NETWORK = load(NetworkService.class);
    public static final LoaderHooks HOOKS = load(LoaderHooks.class);

    private Services() {}

    private static <T> T load(Class<T> type) {
        return ServiceLoader.load(type, Services.class.getClassLoader()).findFirst()
            .orElseThrow(() -> new IllegalStateException("No " + type.getSimpleName() + " for this loader"));
    }
}
