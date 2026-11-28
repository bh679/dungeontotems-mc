package games.brennan.dungeontotems.platform.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A player's click, on both sides. Cancel to stop it; {@link #setCancellationResult} is then what the click
 * returns.
 */
public abstract class PlayerInteractEvent extends PlayerEvent {
    private final InteractionHand hand;
    private final BlockPos pos;
    private final Direction face;
    private InteractionResult cancellationResult = InteractionResult.PASS;

    protected PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, Direction face) {
        super(player);
        this.hand = hand;
        this.pos = pos;
        this.face = face;
    }

    public InteractionHand getHand() { return hand; }
    public ItemStack getItemStack() { return getEntity().getItemInHand(hand); }
    public BlockPos getPos() { return pos; }
    public Direction getFace() { return face; }
    public Level getLevel() { return getEntity().level(); }
    public InteractionResult getCancellationResult() { return cancellationResult; }
    public void setCancellationResult(InteractionResult result) { this.cancellationResult = result; }

    /** Right-click on a block. {@link #setUseItem(Result) setUseItem(DENY)} stops only the item's use on it. */
    public static final class RightClickBlock extends PlayerInteractEvent {
        private final BlockHitResult hitVec;
        private Result useBlock = Result.DEFAULT;
        private Result useItem = Result.DEFAULT;

        public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
            super(player, hand, pos, hitVec.getDirection());
            this.hitVec = hitVec;
        }

        public BlockHitResult getHitVec() { return hitVec; }
        public Result getUseBlock() { return useBlock; }
        public Result getUseItem() { return useItem; }
        public void setUseBlock(Result result) { this.useBlock = result; }
        public void setUseItem(Result result) { this.useItem = result; }

        @Override
        public void setCanceled(boolean canceled) {
            super.setCanceled(canceled);
            if (canceled) {
                useBlock = Result.DENY;
                useItem = Result.DENY;
            }
        }
    }

    /** Right-click with an item, at the air or after the block let it through. */
    public static final class RightClickItem extends PlayerInteractEvent {
        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand, player.blockPosition(), null);
        }
    }

    /** Right-click on an entity ({@code Player#interactOn}). */
    public static final class EntityInteract extends PlayerInteractEvent {
        private final Entity target;

        public EntityInteract(Player player, InteractionHand hand, Entity target) {
            super(player, hand, target.blockPosition(), null);
            this.target = target;
        }

        public Entity getTarget() { return target; }
    }

    /** Left-click on a block (starting to break it). */
    public static final class LeftClickBlock extends PlayerInteractEvent {
        public enum Action { START, STOP, ABORT, CLIENT_HOLD }

        private final Action action;

        public LeftClickBlock(Player player, BlockPos pos, Direction face, Action action) {
            super(player, InteractionHand.MAIN_HAND, pos, face);
            this.action = action;
        }

        public Action getAction() { return action; }
    }
}
