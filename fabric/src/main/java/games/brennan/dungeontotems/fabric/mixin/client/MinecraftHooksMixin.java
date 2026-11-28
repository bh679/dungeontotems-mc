package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

/**
 * NeoForge's {@code InputEvent.InteractionKeyMappingTriggered} for the attack and use keys, at its spots:
 * a cancelled press skips vanilla's action (swinging only if asked).
 */
@Mixin(Minecraft.class)
public abstract class MinecraftHooksMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getAttackAnimation()Lnet/minecraft/world/item/component/SwingAnimation;"), cancellable = true)
    private void dungeontotems$attackKey(CallbackInfoReturnable<Boolean> cir, @Local(name = "heldItem") ItemStack heldItem) {
        ClientEvents.InteractionKey event = DtClientEvents.INTERACTION_KEY.post(new ClientEvents.InteractionKey(false, InteractionHand.MAIN_HAND));
        if (!event.isCanceled()) return;
        if (event.shouldSwingHand() && this.player != null) this.player.swing(InteractionHand.MAIN_HAND, heldItem.getAttackAnimation(), false);
        cir.setReturnValue(false);
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getInteractAnimation()Lnet/minecraft/world/item/component/SwingAnimation;"), cancellable = true)
    private void dungeontotems$useKey(CallbackInfo ci, @Local(name = "hand") InteractionHand hand, @Local(name = "heldItem") ItemStack heldItem) {
        ClientEvents.InteractionKey event = DtClientEvents.INTERACTION_KEY.post(new ClientEvents.InteractionKey(true, hand));
        if (!event.isCanceled()) return;
        if (event.shouldSwingHand() && this.player != null) this.player.swing(hand, heldItem.getInteractAnimation(), false);
        ci.cancel();
    }
}
