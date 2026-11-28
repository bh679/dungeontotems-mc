package games.brennan.dungeontotems.registry;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Attributes 1.21 gives players that 1.20.1 doesn't. Morph forms change them as main changes vanilla's.
 * <ul>
 *   <li>{@link #JUMP_STRENGTH}: the player's jump, 0.42 like vanilla's fixed jump; {@code mixin/LivingEntityMixin}
 *       reads it in {@code getJumpPower}. (1.20.1's {@code horse.jump_strength} is horses only.)</li>
 *   <li>Step height: Forge's {@link ForgeMod#STEP_HEIGHT_ADDITION}, added to the player's 0.6.</li>
 * </ul>
 */
public final class TotemAttributes {

    /** Vanilla's player jump power, and this attribute's base. */
    public static final double PLAYER_JUMP = 0.42;

    private static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, DungeonTotems.MOD_ID);

    private static final RegistryObject<Attribute> JUMP = ATTRIBUTES.register("jump_strength",
        () -> new RangedAttribute("attribute.name.dungeontotems.jump_strength", PLAYER_JUMP, 0.0, 32.0).setSyncable(true));

    private TotemAttributes() {}

    public static Attribute jumpStrength() {
        return JUMP.get();
    }

    public static Attribute stepHeight() {
        return ForgeMod.STEP_HEIGHT_ADDITION.get();
    }

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener((EntityAttributeModificationEvent event) -> {
            if (!event.has(EntityType.PLAYER, JUMP.get())) event.add(EntityType.PLAYER, JUMP.get());
        });
    }
}
