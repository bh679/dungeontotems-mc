package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * In {@code actuallyHurt} after armour and absorption, just before health drops: {@code getHealth() - amount}
 * is the health this hit leaves. Cancel or change it.
 */
public final class LivingDamageEvent extends LivingEvent {
    private final DamageSource source;
    private float amount;

    public LivingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    public DamageSource getSource() { return source; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }
}
