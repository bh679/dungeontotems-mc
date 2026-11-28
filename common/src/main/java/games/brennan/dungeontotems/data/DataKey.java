package games.brennan.dungeontotems.data;

import com.mojang.serialization.Codec;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A value the mod keeps on an entity, saved with it: the 1.20.1 stand-in for main's NeoForge
 * attachment types. Read and write through {@link TotemData}.
 *
 * @param name    saved under this name, so never rename one
 * @param initial what {@link TotemData#get} returns while nothing is set
 * @param codec   how the value is saved
 */
public record DataKey<T>(String name, Supplier<T> initial, Codec<T> codec) {

    private static final Map<String, DataKey<?>> BY_NAME = new ConcurrentHashMap<>();

    public DataKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(initial, "initial");
        Objects.requireNonNull(codec, "codec");
    }

    /** Makes a key and remembers it by name, so saved values can be read back. */
    public static <T> DataKey<T> create(String name, Supplier<T> initial, Codec<T> codec) {
        DataKey<T> key = new DataKey<>(name, initial, codec);
        if (BY_NAME.putIfAbsent(name, key) != null) {
            throw new IllegalStateException("Duplicate data key: " + name);
        }
        return key;
    }

    static DataKey<?> byName(String name) {
        return BY_NAME.get(name);
    }
}
