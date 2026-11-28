package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.fabric.FabricHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The server's left and right clicks on blocks, at NeoForge's spots: a refused left click does nothing; a
 * refused right click answers with its result; an item refused on a block (a block item placed) isn't used.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeHooksMixin {

    @Shadow @Final protected ServerPlayer player;

    /** True while the current right click may use the block but not the item. */
    @Unique private boolean dungeontotems$itemRefused;

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$leftClick(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, CallbackInfo ci) {
        if (FabricHooks.leftClickBlock(this.player, pos, direction)) ci.cancel();
    }

    @Inject(method = "useItemOn", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;gameModeForPlayer:Lnet/minecraft/world/level/GameType;", ordinal = 0), cancellable = true)
    private void dungeontotems$rightClickBlock(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
                                                   CallbackInfoReturnable<InteractionResult> cir) {
        dungeontotems$rightClick(player, hand, hit, cir);
    }

    @Unique
    private void dungeontotems$rightClick(ServerPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerEvents.RightClickBlock event = FabricHooks.rightClickBlock(player, hand, hit);
        this.dungeontotems$itemRefused = !event.useItem();
        if (event.isCanceled()) cir.setReturnValue(event.getCancellationResult());
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$itemOnBlock(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        if (this.dungeontotems$itemRefused) {
            this.dungeontotems$itemRefused = false;
            return InteractionResult.PASS;
        }
        return original.call(stack, context);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void dungeontotems$clicked(ServerPlayer player, Level level, ItemStack stack, InteractionHand hand, BlockHitResult hit,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        this.dungeontotems$itemRefused = false;
    }
}
