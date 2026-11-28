package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.platform.event.LivingDamageEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Shared bits of the mixins (a mixin package may hold only mixins). */
public final class FabricHooks {

    private FabricHooks() {}

    /** Forge's {@code onLivingDamage}: the amount health is about to drop by; 0 if a listener cancelled. */
    public static float damage(LivingEntity entity, DamageSource source, float amount) {
        LivingDamageEvent event = new LivingDamageEvent(entity, source, amount);
        return TotemEvents.post(event) ? 0.0F : event.getAmount();
    }
}
