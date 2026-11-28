package games.brennan.dungeontotems.data;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Values kept on an entity ({@link DataKey}). Like main's attachments they are saved with the entity,
 * lost when a player dies (the respawned player is a new entity), and carried over when a player
 * comes back from the End ({@code mixin/ServerPlayerDataMixin}).
 */
public final class TotemData {

    private static final Logger LOG = LogUtils.getLogger();
    /** The compound the values are saved in, inside the entity's own save data. */
    public static final String TAG = "dungeontotems:data";

    private TotemData() {}

    @SuppressWarnings("unchecked")
    public static <T> T get(Entity entity, DataKey<T> key) {
        Object value = map(entity).get(key);
        return value != null ? (T) value : key.initial().get();
    }

    public static <T> void set(Entity entity, DataKey<T> key, T value) {
        if (value == null) {
            map(entity).remove(key);
        } else {
            map(entity).put(key, value);
        }
    }

    public static boolean has(Entity entity, DataKey<?> key) {
        return map(entity).containsKey(key);
    }

    public static void remove(Entity entity, DataKey<?> key) {
        map(entity).remove(key);
    }

    /** Copies every value from one entity to another (a player back from the End). */
    public static void copy(Entity from, Entity to) {
        map(to).putAll(map(from));
    }

    public static void save(Entity entity, CompoundTag entityTag) {
        Map<DataKey<?>, Object> values = map(entity);
        if (values.isEmpty()) return;
        CompoundTag out = new CompoundTag();
        values.forEach((key, value) -> encode(key, value, out));
        entityTag.put(TAG, out);
    }

    public static void load(Entity entity, CompoundTag entityTag) {
        if (!entityTag.contains(TAG, Tag.TAG_COMPOUND)) return;
        CompoundTag in = entityTag.getCompound(TAG);
        Map<DataKey<?>, Object> values = map(entity);
        values.clear();
        for (String name : in.getAllKeys()) {
            DataKey<?> key = DataKey.byName(name);
            if (key == null) {
                LOG.warn("Dropping unknown Dungeon Totems data {} on {}", name, entity);
                continue;
            }
            key.codec().parse(NbtOps.INSTANCE, in.get(name))
                .resultOrPartial(error -> LOG.warn("Dropping unreadable Dungeon Totems data {}: {}", name, error))
                .ifPresent(value -> values.put(key, value));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void encode(DataKey<T> key, Object value, CompoundTag out) {
        key.codec().encodeStart(NbtOps.INSTANCE, (T) value)
            .resultOrPartial(error -> LOG.warn("Not saving Dungeon Totems data {}: {}", key.name(), error))
            .ifPresent(tag -> out.put(key.name(), tag));
    }

    private static Map<DataKey<?>, Object> map(Entity entity) {
        return ((DataHolder) entity).dungeontotems$data();
    }
}
