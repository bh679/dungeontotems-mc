package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.platform.event.Event;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge's server {@code RightClickBlock}, in {@code useItemOn} once the block is known enabled: cancelled returns its
 * result; {@code useBlock = DENY} skips the block's use, {@code useItem = DENY} the item's.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Unique private PlayerInteractEvent.RightClickBlock dungeontotems$click;

    @Inject(method = "useItemOn", cancellable = true, at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 0,
        target = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;"))
    private void dungeontotems$rightClick(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
                                          CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit);
        dungeontotems$click = event;
        if (TotemEvents.post(event)) cir.setReturnValue(event.getCancellationResult());
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$useBlock(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                                     Operation<InteractionResult> original) {
        if (dungeontotems$click != null && dungeontotems$click.getUseBlock() == Event.Result.DENY) return InteractionResult.PASS;
        return original.call(state, level, player, hand, hit);
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$useItem(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        if (dungeontotems$click != null && dungeontotems$click.getUseItem() == Event.Result.DENY) return InteractionResult.PASS;
        return original.call(stack, context);
    }
}
