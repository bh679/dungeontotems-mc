package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** A container menu a player opened (server). */
public abstract class PlayerContainerEvent extends PlayerEvent {
    private final AbstractContainerMenu container;

    protected PlayerContainerEvent(Player player, AbstractContainerMenu container) {
        super(player);
        this.container = container;
    }

    public AbstractContainerMenu getContainer() { return container; }

    public static final class Open extends PlayerContainerEvent {
        public Open(Player player, AbstractContainerMenu container) {
            super(player, container);
        }
    }
}
