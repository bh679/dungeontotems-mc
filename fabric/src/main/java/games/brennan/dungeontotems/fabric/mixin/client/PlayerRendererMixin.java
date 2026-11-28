package games.brennan.dungeontotems.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import games.brennan.dungeontotems.platform.event.RenderArmEvent;
import games.brennan.dungeontotems.platform.event.RenderPlayerEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's {@code RenderPlayerEvent.Pre} (after the model's properties are set) and first-person {@code RenderArmEvent}. */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/renderer/entity/player/PlayerRenderer;setModelProperties(Lnet/minecraft/client/player/AbstractClientPlayer;)V"))
    private void dungeontotems$render(AbstractClientPlayer player, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers,
                                      int light, CallbackInfo ci) {
        if (TotemEvents.post(new RenderPlayerEvent.Pre(player, (PlayerRenderer) (Object) this, partialTick, pose, buffers, light))) ci.cancel();
    }

    @Inject(method = "renderRightHand", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$rightArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo ci) {
        if (TotemEvents.post(new RenderArmEvent(pose, buffers, light, player, HumanoidArm.RIGHT))) ci.cancel();
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$leftArm(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player, CallbackInfo ci) {
        if (TotemEvents.post(new RenderArmEvent(pose, buffers, light, player, HumanoidArm.LEFT))) ci.cancel();
    }
}
