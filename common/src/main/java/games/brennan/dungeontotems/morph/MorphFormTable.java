package games.brennan.dungeontotems.morph;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Every morph form's {@link MorphForm} entry, from {@code data/<namespace>/morph_forms/*.json}: the 1.20.1
 * stand-in for main's {@code dungeontotems:morph_forms} data map, same file shape
 * ({@code {"replace": false, "values": {"minecraft:pig": {...}}}}). Files load in pack order; a later
 * pack's entry wins, and {@code "replace": true} clears what came before. Unknown mobs are skipped.
 */
public final class MorphFormTable extends SimpleJsonResourceReloadListener {

    public static final MorphFormTable INSTANCE = new MorphFormTable();

    private static volatile Map<ResourceKey<EntityType<?>>, MorphForm> forms = Map.of();

    private MorphFormTable() {
        super(new com.google.gson.Gson(), "morph_forms");
    }

    /** Every form with an entry, by entity type. */
    public static Map<ResourceKey<EntityType<?>>, MorphForm> all() {
        return forms;
    }

    public static Optional<MorphForm> get(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).map(forms::get);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceKey<EntityType<?>>, MorphForm> loaded = new LinkedHashMap<>();
        files.forEach((file, json) -> {
            try {
                read(file, GsonHelper.convertToJsonObject(json, "morph forms"), loaded);
            } catch (RuntimeException e) {
                DungeonTotems.LOGGER.error("Skipping morph forms file {}: {}", file, e.getMessage());
            }
        });
        forms = Collections.unmodifiableMap(loaded);
        DungeonTotems.LOGGER.debug("Loaded {} morph forms", loaded.size());
    }

    private static void read(ResourceLocation file, JsonObject root, Map<ResourceKey<EntityType<?>>, MorphForm> into) {
        if (GsonHelper.getAsBoolean(root, "replace", false)) into.clear();
        JsonObject values = GsonHelper.getAsJsonObject(root, "values");
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                DungeonTotems.LOGGER.debug("Morph forms {}: no mob {}, skipped", file, entry.getKey());
                continue;
            }
            MorphForm.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                .resultOrPartial(error -> DungeonTotems.LOGGER.error("Morph forms {}: {} {}", file, entry.getKey(), error))
                .ifPresent(form -> into.put(ResourceKey.create(Registries.ENTITY_TYPE, id), form));
        }
    }
}
