package games.brennan.dungeontotems.registry;

import games.brennan.dungeontotems.platform.DataKey;
import games.brennan.dungeontotems.platform.Services;
import net.minecraft.world.entity.Entity;

/** Reads and writes the mod's saved per-entity data ({@link TotemRegistries}' {@link DataKey}s) on any loader. */
public final class TotemData {

    private TotemData() {}

    public static <T> T get(Entity entity, DataKey<T> key) {
        return Services.PLATFORM.getData(entity, key);
    }

    public static <T> void set(Entity entity, DataKey<T> key, T value) {
        Services.PLATFORM.setData(entity, key, value);
    }

    public static boolean has(Entity entity, DataKey<?> key) {
        return Services.PLATFORM.hasData(entity, key);
    }
}
