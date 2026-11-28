package games.brennan.dungeontotems.platform;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

/**
 * One piece of saved per-entity data (NeoForge: an attachment; Fabric: a Data Attachment), made by
 * {@link Platform#registerData}. Each loader keeps its own handle in it.
 */
public final class DataKey<T> {

    private final String name;
    private final Supplier<T> defaultValue;
    private final Codec<T> codec;
    private final Object handle;

    public DataKey(String name, Supplier<T> defaultValue, Codec<T> codec, Object handle) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.codec = codec;
        this.handle = handle;
    }

    public String name() {
        return name;
    }

    public T defaultValue() {
        return defaultValue.get();
    }

    public Codec<T> codec() {
        return codec;
    }

    /** The loader's own type for this data. */
    public Object handle() {
        return handle;
    }
}
