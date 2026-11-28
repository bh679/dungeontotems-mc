package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.fabric.FabricHooks;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's client-side {@code RightClickBlock}, as on the server ({@code ServerPlayerGameModeHooksMixin}). */
@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeHooksMixin {

    @Unique private boolean dungeontotems$itemRefused;

    @Inject(method = "performUseItemOn", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$rightClickBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        PlayerEvents.RightClickBlock event = FabricHooks.rightClickBlock(player, hand, hit);
        this.dungeontotems$itemRefused = !event.useItem();
        if (event.isCanceled()) cir.setReturnValue(event.getCancellationResult());
    }

    @WrapOperation(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult dungeontotems$itemOnBlock(ItemStack stack, UseOnContext context, Operation<InteractionResult> original) {
        if (this.dungeontotems$itemRefused) {
            this.dungeontotems$itemRefused = false;
            return InteractionResult.PASS;
        }
        return original.call(stack, context);
    }

    @Inject(method = "performUseItemOn", at = @At("RETURN"))
    private void dungeontotems$clicked(LocalPlayer player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        this.dungeontotems$itemRefused = false;
    }
}
