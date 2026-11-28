package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;

/** In {@code baseTick}: whether this entity can breathe this tick (air refills) or not (air drains). */
public final class LivingBreatheEvent extends LivingEvent {
    private boolean canBreathe;

    public LivingBreatheEvent(LivingEntity entity, boolean canBreathe) {
        super(entity);
        this.canBreathe = canBreathe;
    }

    public boolean canBreathe() { return canBreathe; }
    public void setCanBreathe(boolean canBreathe) { this.canBreathe = canBreathe; }
}
