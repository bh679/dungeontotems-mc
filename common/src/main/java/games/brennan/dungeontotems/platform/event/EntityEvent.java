package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;

/** An event about an entity. */
public abstract class EntityEvent extends Event {

    private final Entity entity;

    protected EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    /** An entity's size as {@code Entity#refreshDimensions} works it out. */
    public static final class Size extends EntityEvent {
        private final Pose pose;
        private final EntityDimensions oldSize;
        private EntityDimensions newSize;

        public Size(Entity entity, Pose pose, EntityDimensions oldSize, EntityDimensions newSize) {
            super(entity);
            this.pose = pose;
            this.oldSize = oldSize;
            this.newSize = newSize;
        }

        public Pose getPose() { return pose; }
        public EntityDimensions getOldSize() { return oldSize; }
        public EntityDimensions getNewSize() { return newSize; }
        public void setNewSize(EntityDimensions size) { this.newSize = size; }
    }
}
