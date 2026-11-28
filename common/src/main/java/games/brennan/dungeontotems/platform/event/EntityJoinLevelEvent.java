package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** An entity added to a level (spawned or loaded), on both sides. */
public final class EntityJoinLevelEvent extends EntityEvent {
    private final Level level;

    public EntityJoinLevelEvent(Entity entity, Level level) {
        super(entity);
        this.level = level;
    }

    public Level getLevel() { return level; }
}
