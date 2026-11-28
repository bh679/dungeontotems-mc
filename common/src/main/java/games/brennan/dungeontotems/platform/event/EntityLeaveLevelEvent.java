package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** An entity removed from a level (killed, discarded or unloaded), on both sides. */
public final class EntityLeaveLevelEvent extends EntityEvent {
    private final Level level;

    public EntityLeaveLevelEvent(Entity entity, Level level) {
        super(entity);
        this.level = level;
    }

    public Level getLevel() { return level; }
}
