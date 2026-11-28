package games.brennan.dungeontotems.registry;

import games.brennan.dungeontotems.platform.Services;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Supplier;

/**
 * Attributes 1.21 gives players that 1.20.1 doesn't. Morph forms change them as main changes vanilla's.
 * <ul>
 *   <li>{@link #jumpStrength()}: the player's jump, 0.42 like vanilla's fixed jump; {@code mixin/LivingEntityMixin}
 *       reads it in {@code getJumpPower}. (1.20.1's {@code horse.jump_strength} is horses only.) Each loader
 *       adds it to the player's attributes.</li>
 *   <li>Step height: added to the player's 0.6. Forge's {@code step_height_addition}; on Fabric our own
 *       {@code dungeontotems:step_height} ({@link Services#HOOKS}).</li>
 * </ul>
 */
public final class TotemAttributes {

    /** Vanilla's player jump power, and this attribute's base. */
    public static final double PLAYER_JUMP = 0.42;

    private static final Supplier<Attribute> JUMP = Services.REGISTRY.register(Registries.ATTRIBUTE, "jump_strength",
        () -> new RangedAttribute("attribute.name.dungeontotems.jump_strength", PLAYER_JUMP, 0.0, 32.0).setSyncable(true));

    private TotemAttributes() {}

    /** Loads the class, registering the attribute. */
    static void init() {}

    public static Attribute jumpStrength() {
        return JUMP.get();
    }

    public static Attribute stepHeight() {
        return Services.HOOKS.stepHeight();
    }
}
