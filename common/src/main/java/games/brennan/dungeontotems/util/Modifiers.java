package games.brennan.dungeontotems.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Attribute modifiers named by id, as on main (1.21 keys modifiers by {@link ResourceLocation}).
 * 1.20.1 keys them by UUID, so each id maps to a fixed UUID made from it: the same on every run.
 */
public final class Modifiers {

    private Modifiers() {}

    public static UUID uuid(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static AttributeModifier of(ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        return new AttributeModifier(uuid(id), id.toString(), amount, operation);
    }

    public static void remove(AttributeInstance instance, ResourceLocation id) {
        instance.removeModifier(uuid(id));
    }

    public static boolean has(AttributeInstance instance, ResourceLocation id) {
        return instance.getModifier(uuid(id)) != null;
    }
}
