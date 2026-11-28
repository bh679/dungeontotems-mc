package games.brennan.dungeontotems.mixin.client;

import games.brennan.dungeontotems.client.FormArms;
import games.brennan.dungeontotems.client.FormHands;
import games.brennan.dungeontotems.morph.Morphs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A morphed player has no player hands in first person. {@code MorphRenderer} cancels NeoForge's hand event,
 * but mods that animate first-person arms (and shader packs' hand pass) draw them without it, so the whole
 * first-person hand pass is skipped here, ahead of other mods' injects, and redone without them:
 * <ul>
 *   <li>Held items are drawn as vanilla draws them (its swing, equip, eating, bow and shield poses), so a
 *       Zombified player still sees the sword they hold.</li>
 *   <li>An empty main hand shows the form's own arm ({@link FormArms}) if it has one: vanilla's empty-hand
 *       arm, which {@code MorphRenderer} draws as the form's. An armless form shows nothing there.</li>
 * </ul>
 * NeoForge's per-hand event is not fired, as that is where animation mods draw their player arms.
 */
@Mixin(value = FirstPersonHandsAndItemsRenderer.class, priority = 500)
public abstract class ItemInHandRendererMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$onlyTheFormsHands(float partialTicks, PoseStack pose, SubmitNodeCollector collector,
                                                 PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state,
                                                 CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Morphs.formOf(player).isEmpty()) return;
        ci.cancel();
        AvatarRenderState avatar = playerState.avatarRenderState;
        if (state.isScoping || avatar == null) return;
        boolean armed = FormArms.hasArms(player);
        float attack = avatar.swingAnimation;
        InteractionHand swinging = state.attackHand == null ? InteractionHand.MAIN_HAND : state.attackHand;
        pose.pushPose();
        pose.rotateDegrees(Axis.XP, (state.viewXRot - state.xBob) * 0.1F);
        pose.rotateDegrees(Axis.YP, (state.viewYRot - state.yBob) * 0.1F);
        ItemInHandRendererInvoker self = (ItemInHandRendererInvoker) this;
        for (InteractionHand hand : FormHands.handsToDraw(player)) {
            boolean main = hand == InteractionHand.MAIN_HAND;
            ItemStack stack = main ? state.mainHandItem : state.offHandItem;
            if (!FormHands.shouldDraw(stack, main, armed)) continue;
            float swing = hand == swinging ? attack : 0.0F;
            float inverseHeight = main
                ? state.mainHandSwapScale * (1.0F - Mth.lerp(partialTicks, state.oldMainHandHeight, state.mainHandHeight))
                : state.offHandSwapScale * (1.0F - Mth.lerp(partialTicks, state.oldOffHandHeight, state.offHandHeight));
            self.dungeontotems$submitArmWithItem(playerState, state, partialTicks, avatar.xRot, hand, swing, stack,
                inverseHeight, pose, collector, avatar.lightCoords);
            if (!stack.isEmpty()) FormHands.itemDrawn();
        }
        pose.popPose();
    }
}
