package games.brennan.dungeontotems.platform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** An event about a player. */
public abstract class PlayerEvent extends LivingEvent {
    private final Player player;

    protected PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    /** How fast the player breaks a block ({@code Player#getDestroySpeed}). Cancel for not at all. */
    public static final class BreakSpeed extends PlayerEvent {
        private final BlockState state;
        private final float originalSpeed;
        private final BlockPos pos;
        private float newSpeed;

        public BreakSpeed(Player player, BlockState state, float originalSpeed, BlockPos pos) {
            super(player);
            this.state = state;
            this.originalSpeed = originalSpeed;
            this.newSpeed = originalSpeed;
            this.pos = pos;
        }

        public BlockState getState() { return state; }
        public float getOriginalSpeed() { return originalSpeed; }
        public float getNewSpeed() { return newSpeed; }
        public void setNewSpeed(float speed) { this.newSpeed = speed; }
        public BlockPos getPosition() { return pos; }
    }

    /** A new player object copied from the old one: after death, or on return from the End. */
    public static final class Clone extends PlayerEvent {
        private final Player original;
        private final boolean wasDeath;

        public Clone(Player player, Player original, boolean wasDeath) {
            super(player);
            this.original = original;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() { return original; }
        public boolean isWasDeath() { return wasDeath; }
    }

    public static final class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) { super(player); }
    }

    public static final class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) { super(player); }
    }

    public static final class PlayerRespawnEvent extends PlayerEvent {
        private final boolean endConquered;

        public PlayerRespawnEvent(Player player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        public boolean isEndConquered() { return endConquered; }
    }

    /** A player starts seeing an entity (it is sent to their client). */
    public static final class StartTracking extends PlayerEvent {
        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() { return target; }
    }
}
