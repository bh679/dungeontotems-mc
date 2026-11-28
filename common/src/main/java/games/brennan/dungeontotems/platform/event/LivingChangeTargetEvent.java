package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;

/** A mob picking a new attack target ({@code Mob#setTarget}, or a brain's attack target). Cancel to keep the old one. */
public final class LivingChangeTargetEvent extends LivingEvent {
    private final LivingEntity originalTarget;
    private LivingEntity newTarget;

    public LivingChangeTargetEvent(LivingEntity entity, LivingEntity newTarget) {
        super(entity);
        this.originalTarget = newTarget;
        this.newTarget = newTarget;
    }

    public LivingEntity getOriginalTarget() { return originalTarget; }
    public LivingEntity getNewTarget() { return newTarget; }
    public void setNewTarget(LivingEntity target) { this.newTarget = target; }
}
