package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's {@code RenderArmEvent}: a first-person bare arm, which a morphed player sees as their form's. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonArmHooksMixin {

    @WrapOperation(method = "renderPlayerHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderRightHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void dungeontotems$rightArm(AvatarRenderer<?> renderer, PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve, Operation<Void> original) {
        if (!dungeontotems$refused(HumanoidArm.RIGHT, pose, collector, light)) original.call(renderer, pose, collector, light, skin, sleeve);
    }

    @WrapOperation(method = "renderPlayerHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/player/AvatarRenderer;renderLeftHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;Z)V"))
    private void dungeontotems$leftArm(AvatarRenderer<?> renderer, PoseStack pose, SubmitNodeCollector collector, int light, Identifier skin, boolean sleeve, Operation<Void> original) {
        if (!dungeontotems$refused(HumanoidArm.LEFT, pose, collector, light)) original.call(renderer, pose, collector, light, skin, sleeve);
    }

    private static boolean dungeontotems$refused(HumanoidArm arm, PoseStack pose, SubmitNodeCollector collector, int light) {
        return DtClientEvents.RENDER_ARM.post(new ClientEvents.RenderArm(arm, pose, collector, light)).isCanceled();
    }
}
