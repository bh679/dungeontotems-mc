package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 * Where Fabric's mixins (and {@link FabricTestHooks}) fire the common events. Each method sits at the
 * spot NeoForge fires its event of the same name, so the handlers see the same timing on both loaders.
 */
public final class FabricHooks {

    private FabricHooks() {}

    /** NeoForge's {@code LivingUseTotemEvent} result: false means no totem. */
    public static void useTotem(LivingEntity entity) {
        DtEvents.USE_TOTEM.post(new LivingEvents.UseTotem(entity));
    }

    /** True when the start is refused. */
    public static boolean startUsingItem(LivingEntity entity, ItemStack stack) {
        return DtEvents.USE_ITEM_START.post(new LivingEvents.UseItemStart(entity, stack)).isCanceled();
    }

    public static void finishUsingItem(LivingEntity entity, ItemStack before) {
        DtEvents.USE_ITEM_FINISH.post(new LivingEvents.UseItemFinish(entity, before));
    }

    /** The interaction's answer when refused, else null (go on). */
    @Nullable
    public static InteractionResult interactEntity(Player player, InteractionHand hand, Entity target) {
        PlayerEvents.EntityInteract event = DtEvents.ENTITY_INTERACT.post(new PlayerEvents.EntityInteract(player, hand, target));
        return event.isCanceled() ? event.getCancellationResult() : null;
    }

    /** True when the attack is refused. */
    public static boolean attack(Player player, Entity target) {
        return DtEvents.ATTACK_ENTITY.post(new PlayerEvents.AttackEntity(player, target)).isCanceled();
    }

    public static boolean leftClickBlock(Player player, BlockPos pos, Direction face) {
        return DtEvents.LEFT_CLICK_BLOCK.post(new PlayerEvents.LeftClickBlock(player, pos, face)).isCanceled();
    }

    public static PlayerEvents.RightClickBlock rightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
        return DtEvents.RIGHT_CLICK_BLOCK.post(new PlayerEvents.RightClickBlock(player, hand, hit));
    }

    public static PlayerEvents.RightClickItem rightClickItem(Player player, InteractionHand hand) {
        return DtEvents.RIGHT_CLICK_ITEM.post(new PlayerEvents.RightClickItem(player, hand));
    }

    /** True when the player can't break {@code state} at all. */
    public static boolean breakSpeedRefused(Player player, BlockState state) {
        return DtEvents.BREAK_SPEED.post(new PlayerEvents.BreakSpeed(player, state)).isCanceled();
    }

    public static boolean breakBlockRefused(Player player, BlockPos pos, BlockState state) {
        return DtEvents.BREAK_BLOCK.post(new PlayerEvents.BreakBlock(player, pos, state)).isCanceled();
    }

    /** True when {@code tamer} may not tame {@code animal}. */
    public static boolean tameRefused(Animal animal, Player tamer) {
        return DtEvents.ANIMAL_TAME.post(new PlayerEvents.AnimalTame(tamer, animal)).isCanceled();
    }
}
