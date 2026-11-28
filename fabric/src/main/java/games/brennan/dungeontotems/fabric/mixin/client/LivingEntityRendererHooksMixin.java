package games.brennan.dungeontotems.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import games.brennan.dungeontotems.fabric.client.PartialTickHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's {@code RenderPlayerEvent.Pre}: a player about to be drawn (vanilla's avatar renderer uses this submit). */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererHooksMixin {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD"), cancellable = true)
    private void dungeontotems$renderPlayer(LivingEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        if (!((Object) this instanceof AvatarRenderer<?>) || !(state instanceof AvatarRenderState avatar)) return;
        float partialTick = ((PartialTickHolder) avatar).dungeontotems$partialTick();
        if (DtClientEvents.RENDER_PLAYER.post(new ClientEvents.RenderPlayerPre(avatar, pose, collector, partialTick)).isCanceled()) ci.cancel();
    }
}
