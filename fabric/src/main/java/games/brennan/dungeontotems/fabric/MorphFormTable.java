package games.brennan.dungeontotems.fabric;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.MorphForm;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The morph table on Fabric. NeoForge loads {@code data/dungeontotems/data_maps/entity_type/morph_forms.json}
 * as a data map; Fabric has none, so this reads the same file the same way: every data pack's copy, lowest
 * first, each one's {@code values} added on top ({@code "replace": true} starts over), an unknown entity or
 * a bad entry skipped with a warning.
 */
public final class MorphFormTable extends SimplePreparableReloadListener<Map<ResourceKey<EntityType<?>>, MorphForm>> {

    public static final Identifier ID = DungeonTotems.id("morph_forms");
    private static final Identifier FILE = MorphForm.TABLE_ID.withPath(path -> "data_maps/entity_type/" + path + ".json");

    private static volatile Map<ResourceKey<EntityType<?>>, MorphForm> current = Map.of();

    /** The table as last loaded (empty before the first data load). */
    public static Map<ResourceKey<EntityType<?>>, MorphForm> current() {
        return current;
    }

    @Override
    protected Map<ResourceKey<EntityType<?>>, MorphForm> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceKey<EntityType<?>>, MorphForm> table = new LinkedHashMap<>();
        List<Resource> stack = manager.getResourceStack(FILE);
        for (Resource resource : stack) {
            try (Reader reader = resource.openAsReader()) {
                read(JsonParser.parseReader(reader).getAsJsonObject(), table, resource.sourcePackId());
            } catch (Exception e) {
                DungeonTotems.LOGGER.warn("Skipping morph forms in pack {}: {}", resource.sourcePackId(), e.toString());
            }
        }
        return table;
    }

    static void read(JsonObject json, Map<ResourceKey<EntityType<?>>, MorphForm> table, String pack) {
        if (json.has("replace") && json.get("replace").getAsBoolean()) table.clear();
        JsonObject values = json.getAsJsonObject("values");
        if (values == null) return;
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                DungeonTotems.LOGGER.warn("Morph forms in {}: no entity {}", pack, entry.getKey());
                continue;
            }
            MorphForm.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                .resultOrPartial(error -> DungeonTotems.LOGGER.warn("Morph forms in {}: {} {}", pack, entry.getKey(), error))
                .ifPresent(form -> table.put(ResourceKey.create(Registries.ENTITY_TYPE, id), form));
        }
    }

    @Override
    protected void apply(Map<ResourceKey<EntityType<?>>, MorphForm> table, ResourceManager manager, ProfilerFiller profiler) {
        current = Map.copyOf(table);
        DungeonTotems.LOGGER.debug("Loaded {} morph forms", current.size());
    }
}
