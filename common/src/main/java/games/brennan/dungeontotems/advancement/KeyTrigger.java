package games.brennan.dungeontotems.advancement;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

/**
 * A trigger that fires with one key (a curse, a form) and matches criteria naming that key, or any key
 * when the criterion names none:
 * <pre>{@code { "trigger": "dungeontotems:curse_survived", "conditions": { "curse": "weakness" } }}</pre>
 */
public final class KeyTrigger extends SimpleCriterionTrigger<KeyTrigger.Instance> {

    private final ResourceLocation id;
    private final String field;

    public KeyTrigger(ResourceLocation id, String field) {
        this.id = id;
        this.field = field;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        Optional<String> key = json.has(field) ? Optional.of(GsonHelper.getAsString(json, field)) : Optional.empty();
        return new Instance(id, field, player, key);
    }

    public void trigger(ServerPlayer player, String key) {
        trigger(player, instance -> instance.matches(key));
    }

    public static final class Instance extends AbstractCriterionTriggerInstance {
        private final String field;
        private final Optional<String> key;

        Instance(ResourceLocation id, String field, ContextAwarePredicate player, Optional<String> key) {
            super(id, player);
            this.field = field;
            this.key = key;
        }

        public Optional<String> key() {
            return key;
        }

        boolean matches(String fired) {
            return key.map(fired::equals).orElse(true);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            key.ifPresent(k -> json.addProperty(field, k));
            return json;
        }
    }
}
