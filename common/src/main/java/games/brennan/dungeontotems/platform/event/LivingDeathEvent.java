package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** Top of {@code die()}: the vanilla totem has already had its chance. Cancel to live. */
public final class LivingDeathEvent extends LivingEvent {
    private final DamageSource source;

    public LivingDeathEvent(LivingEntity entity, DamageSource source) {
        super(entity);
        this.source = source;
    }

    public DamageSource getSource() { return source; }
}
