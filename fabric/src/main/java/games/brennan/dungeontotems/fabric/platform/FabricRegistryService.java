package games.brennan.dungeontotems.fabric.platform;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.platform.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/** Registers at once: Fabric's registries stay open until the game has started. */
public final class FabricRegistryService implements RegistryService {

    @Override
    @SuppressWarnings("unchecked")
    public <T, V extends T> Supplier<V> register(ResourceKey<? extends Registry<T>> registry, String name, Supplier<V> factory) {
        Registry<T> target = (Registry<T>) BuiltInRegistries.REGISTRY.get(registry.location());
        if (target == null) throw new IllegalStateException("No registry " + registry.location());
        V value = Registry.register(target, DungeonTotems.id(name), factory.get());
        return () -> value;
    }
}
