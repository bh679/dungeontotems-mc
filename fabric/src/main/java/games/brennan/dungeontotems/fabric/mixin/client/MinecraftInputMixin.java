package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.platform.event.InputEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge's {@code InteractionKeyMappingTriggered} for the attack key ({@code startAttack}, before it acts on what's
 * aimed at) and the use key ({@code startUseItem}, per hand before the item). Cancelled stops it; the hand
 * swings only if {@code shouldSwingHand}. (Not fired while holding attack on a block, nor for pick block: nothing
 * here listens for those.)
 */
@Mixin(Minecraft.class)
public abstract class MinecraftInputMixin {

    @Shadow public LocalPlayer player;

    @Inject(method = "startAttack", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"))
    private void dungeontotems$attackKey(CallbackInfoReturnable<Boolean> cir) {
        Minecraft self = (Minecraft) (Object) this;
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(
            InputEvent.InteractionKeyMappingTriggered.ATTACK, self.options.keyAttack, InteractionHand.MAIN_HAND);
        if (!TotemEvents.post(event)) return;
        if (event.shouldSwingHand()) player.swing(InteractionHand.MAIN_HAND);
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", cancellable = true, at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
    private void dungeontotems$useKey(CallbackInfo ci, @Local InteractionHand hand) {
        Minecraft self = (Minecraft) (Object) this;
        InputEvent.InteractionKeyMappingTriggered event = new InputEvent.InteractionKeyMappingTriggered(
            InputEvent.InteractionKeyMappingTriggered.USE, self.options.keyUse, hand);
        if (!TotemEvents.post(event)) return;
        if (event.shouldSwingHand()) player.swing(hand);
        ci.cancel();
    }
}
