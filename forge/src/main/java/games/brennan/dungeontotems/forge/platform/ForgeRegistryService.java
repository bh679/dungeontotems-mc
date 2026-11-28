package games.brennan.dungeontotems.forge.platform;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.platform.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** One {@link DeferredRegister} per registry, made and hooked to the mod bus on first use (in the mod's constructor). */
public final class ForgeRegistryService implements RegistryService {

    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T, V extends T> Supplier<V> register(ResourceKey<? extends Registry<T>> registry, String name, Supplier<V> factory) {
        DeferredRegister<T> register = (DeferredRegister<T>) REGISTERS.computeIfAbsent(registry, key -> {
            DeferredRegister<T> created = DeferredRegister.create(registry, DungeonTotems.MOD_ID);
            created.register(FMLJavaModLoadingContext.get().getModEventBus());
            return created;
        });
        return register.register(name, factory);
    }
}
