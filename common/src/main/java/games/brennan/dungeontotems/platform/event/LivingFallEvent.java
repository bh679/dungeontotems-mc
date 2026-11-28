package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;

/** Top of {@code causeFallDamage}. Cancel for no fall damage, or change the distance. */
public final class LivingFallEvent extends LivingEvent {
    private float distance;
    private float damageMultiplier;

    public LivingFallEvent(LivingEntity entity, float distance, float damageMultiplier) {
        super(entity);
        this.distance = distance;
        this.damageMultiplier = damageMultiplier;
    }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
    public float getDamageMultiplier() { return damageMultiplier; }
    public void setDamageMultiplier(float multiplier) { this.damageMultiplier = multiplier; }
}
