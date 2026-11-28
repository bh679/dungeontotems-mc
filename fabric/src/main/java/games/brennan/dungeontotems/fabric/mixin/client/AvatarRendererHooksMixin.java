package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.fabric.client.PartialTickHolder;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the partial tick a player was extracted at on its render state, for the render event. */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererHooksMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void dungeontotems$partialTick(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        ((PartialTickHolder) state).dungeontotems$setPartialTick(partialTicks);
    }
}
