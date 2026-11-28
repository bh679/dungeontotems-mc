package games.brennan.dungeontotems.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/** Registers the mod's entries under {@code dungeontotems:<name>}. */
public interface RegistryService {

    /** The returned supplier is only valid once the loader has filled the registry. */
    <T, V extends T> Supplier<V> register(ResourceKey<? extends Registry<T>> registry, String name, Supplier<V> factory);
}
