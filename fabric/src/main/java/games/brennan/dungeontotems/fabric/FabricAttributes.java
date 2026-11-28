package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * {@code dungeontotems:step_height}: the player's step-height bonus, added to vanilla's 0.6 as Forge's
 * {@code forge:step_height_addition} is ({@code mixin/EntityStepMixin}). Registered when first asked for, which
 * may be while the player's attributes are first built ({@code mixin/PlayerAttributesMixin}), before our
 * initializer runs.
 */
public final class FabricAttributes {

    private static Attribute stepHeight;

    private FabricAttributes() {}

    public static synchronized Attribute stepHeight() {
        if (stepHeight == null) {
            stepHeight = Registry.register(BuiltInRegistries.ATTRIBUTE, DungeonTotems.id("step_height"),
                new RangedAttribute("attribute.name.dungeontotems.step_height", 0.0, -512.0, 512.0).setSyncable(true));
        }
        return stepHeight;
    }
}
