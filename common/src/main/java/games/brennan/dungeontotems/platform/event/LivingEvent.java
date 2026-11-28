package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;

/** An event about a living entity. */
public abstract class LivingEvent extends EntityEvent {

    private final LivingEntity living;

    protected LivingEvent(LivingEntity entity) {
        super(entity);
        this.living = entity;
    }

    @Override
    public LivingEntity getEntity() {
        return living;
    }

    /** After {@code LivingEntity#jumpFromGround}. */
    public static final class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent(LivingEntity entity) {
            super(entity);
        }
    }
}
