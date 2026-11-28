package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** A player attacking an entity ({@code Player#attack}). Cancel to not attack. */
public final class AttackEntityEvent extends PlayerEvent {
    private final Entity target;

    public AttackEntityEvent(Player player, Entity target) {
        super(player);
        this.target = target;
    }

    public Entity getTarget() { return target; }
}
