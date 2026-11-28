package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.player.Player;

/** Forge's tick events: one class fired at the start and the end of each tick. */
public abstract class TickEvent extends Event {

    public enum Phase { START, END }

    public final Phase phase;

    protected TickEvent(Phase phase) {
        this.phase = phase;
    }

    /** A player's tick, on both sides: the start and end of {@code Player#tick}. */
    public static final class PlayerTickEvent extends TickEvent {
        public final Player player;

        public PlayerTickEvent(Phase phase, Player player) {
            super(phase);
            this.player = player;
        }
    }

    /** The client's tick. */
    public static final class ClientTickEvent extends TickEvent {
        public ClientTickEvent(Phase phase) {
            super(phase);
        }
    }
}
