package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.platform.event.Event;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The client side of {@code ServerPlayerGameModeMixin}: Forge's {@code RightClickBlock} first in {@code performUseItemOn}. */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {

    @Unique private PlayerInteractEvent.RightClickBlock dungeontotems$click;

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$rightClick(LocalPlayer player, InteractionHand hand, BlockHitResult hit,
                                          CallbackInfoReturnable<InteractionResult> cir) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit);
        dungeontotems$click = event;
        if (TotemEvents.post(event)) cir.setReturnValue(event.getCancellationResult());
    }

    @WrapOperation(method = "performUseItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$useBlock(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hit,
                                                     Operation<InteractionResult> original) {
        if (dungeontotems$click != null && dungeontotems$click.getUseBlock() == Event.Result.DENY) return InteractionResult.PASS;
        return original.call(state, level, player, hand, hit);
    }

    @WrapOperation(method = "performUseItemOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$useItem(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        if (dungeontotems$click != null && dungeontotems$click.getUseItem() == Event.Result.DENY) return InteractionResult.PASS;
        return original.call(stack, context);
    }
}
