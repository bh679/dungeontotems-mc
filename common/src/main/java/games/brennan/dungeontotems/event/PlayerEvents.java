package games.brennan.dungeontotems.event;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Events on players, named after NeoForge's. Every one's {@code getEntity()} is the player. */
public final class PlayerEvents {

    private PlayerEvents() {}

    public abstract static class OfPlayer extends LivingEvents.OfEntity<Player> {
        protected OfPlayer(Player player) {
            super(player);
        }
    }

    /** Before the player's tick, on both sides. */
    public static final class TickPre extends OfPlayer {
        public TickPre(Player player) {
            super(player);
        }
    }

    /** After the player's tick, on both sides. */
    public static final class TickPost extends OfPlayer {
        public TickPost(Player player) {
            super(player);
        }
    }

    public static final class LoggedIn extends OfPlayer {
        public LoggedIn(Player player) {
            super(player);
        }
    }

    public static final class LoggedOut extends OfPlayer {
        public LoggedOut(Player player) {
            super(player);
        }
    }

    public static final class Respawn extends OfPlayer {
        public Respawn(Player player) {
            super(player);
        }
    }

    /** The new player made on respawn (or return from the End), from {@code original}. */
    public static final class Clone extends OfPlayer {
        private final Player original;
        private final boolean wasDeath;

        public Clone(Player player, Player original, boolean wasDeath) {
            super(player);
            this.original = original;
            this.wasDeath = wasDeath;
        }

        public Player getOriginal() {
            return original;
        }

        public boolean isWasDeath() {
            return wasDeath;
        }
    }

    /** The player starts seeing {@code target}. */
    public static final class StartTracking extends OfPlayer {
        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    /** About to attack {@code target}. Cancel and the attack doesn't happen. */
    public static final class AttackEntity extends OfPlayer {
        private final Entity target;

        public AttackEntity(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    /** Base of the click events: a hand, its item, and what to answer when cancelled. */
    public abstract static class Interact extends OfPlayer {
        private final InteractionHand hand;
        private InteractionResult cancellationResult = InteractionResult.PASS;

        protected Interact(Player player, InteractionHand hand) {
            super(player);
            this.hand = hand;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public ItemStack getItemStack() {
            return getEntity().getItemInHand(hand);
        }

        public Level getLevel() {
            return getEntity().level();
        }

        public InteractionResult getCancellationResult() {
            return cancellationResult;
        }

        public void setCancellationResult(InteractionResult result) {
            this.cancellationResult = result;
        }
    }

    /** Left-clicking (starting to break) a block. Cancel and nothing happens. */
    public static final class LeftClickBlock extends Interact {
        private final BlockPos pos;
        private final Direction face;

        public LeftClickBlock(Player player, BlockPos pos, Direction face) {
            super(player, InteractionHand.MAIN_HAND);
            this.pos = pos;
            this.face = face;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getFace() {
            return face;
        }
    }

    /**
     * Right-clicking a block. Cancel and neither the block nor the item is used; {@link #setUseItem}
     * {@code false} stops only the item (a block item placed, say).
     */
    public static final class RightClickBlock extends Interact {
        private final BlockHitResult hit;
        private boolean useItem = true;

        public RightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
            super(player, hand);
            this.hit = hit;
        }

        public BlockPos getPos() {
            return hit.getBlockPos();
        }

        public BlockHitResult getHitVec() {
            return hit;
        }

        public boolean useItem() {
            return useItem;
        }

        public void setUseItem(boolean useItem) {
            this.useItem = useItem;
        }
    }

    /** Right-clicking with an item in the air. Cancel and it isn't used. */
    public static final class RightClickItem extends Interact {
        public RightClickItem(Player player, InteractionHand hand) {
            super(player, hand);
        }
    }

    /** Right-clicking an entity. Cancel and the entity isn't used. */
    public static final class EntityInteract extends Interact {
        private final Entity target;

        public EntityInteract(Player player, InteractionHand hand, Entity target) {
            super(player, hand);
            this.target = target;
        }

        public Entity getTarget() {
            return target;
        }
    }

    /** A container menu opened for the player. */
    public static final class ContainerOpen extends OfPlayer {
        private final AbstractContainerMenu container;

        public ContainerOpen(Player player, AbstractContainerMenu container) {
            super(player);
            this.container = container;
        }

        public AbstractContainerMenu getContainer() {
            return container;
        }
    }

    /** How fast the player breaks {@code state}. Cancel and it can't be broken. */
    public static final class BreakSpeed extends OfPlayer {
        private final BlockState state;

        public BreakSpeed(Player player, BlockState state) {
            super(player);
            this.state = state;
        }

        public BlockState getState() {
            return state;
        }
    }

    /** The player is about to break a block. Cancel and it stays. */
    public static final class BreakBlock extends OfPlayer {
        private final BlockPos pos;
        private final BlockState state;

        public BreakBlock(Player player, BlockPos pos, BlockState state) {
            super(player);
            this.pos = pos;
            this.state = state;
        }

        public Player getPlayer() {
            return getEntity();
        }

        public BlockPos getPos() {
            return pos;
        }

        public BlockState getState() {
            return state;
        }
    }

    /** {@code tamer} is about to tame {@code animal}. Cancel and the taming fails. */
    public static final class AnimalTame extends OfPlayer {
        private final Animal animal;

        public AnimalTame(Player tamer, Animal animal) {
            super(tamer);
            this.animal = animal;
        }

        public Player getTamer() {
            return getEntity();
        }

        public Animal getAnimal() {
            return animal;
        }
    }

    /** The player traded with a villager or wandering trader. */
    public static final class Trade extends OfPlayer {
        private final AbstractVillager villager;

        public Trade(Player player, AbstractVillager villager) {
            super(player);
            this.villager = villager;
        }

        public AbstractVillager getAbstractVillager() {
            return villager;
        }
    }

    /** The player earned an advancement. */
    public static final class AdvancementEarn extends OfPlayer {
        private final AdvancementHolder advancement;

        public AdvancementEarn(Player player, AdvancementHolder advancement) {
            super(player);
            this.advancement = advancement;
        }

        public AdvancementHolder getAdvancement() {
            return advancement;
        }
    }
}
