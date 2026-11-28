package games.brennan.dungeontotems.mixin.client;

import games.brennan.dungeontotems.client.FormArms;
import games.brennan.dungeontotems.client.FormHands;
import games.brennan.dungeontotems.morph.Morphs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
@Mixin(value = ItemInHandRenderer.class, priority = 500)
public abstract class ItemInHandRendererMixin {

    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;
    @Shadow private float offHandHeight;
    @Shadow private float oOffHandHeight;
    @Shadow private ItemStack mainHandItem;
    @Shadow private ItemStack offHandItem;

    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$onlyTheFormsHands(float partialTicks, PoseStack pose, MultiBufferSource.BufferSource buffers,
                                                 LocalPlayer player, int light, CallbackInfo ci) {
        if (Morphs.formOf(player).isEmpty()) return;
        ci.cancel();
        if (player.isScoping()) return;
        boolean armed = FormArms.hasArms(player);
        float attack = player.getAttackAnim(partialTicks);
        InteractionHand swinging = player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm;
        float pitch = Mth.lerp(partialTicks, player.xRotO, player.getXRot());
        float xBob = Mth.lerp(partialTicks, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partialTicks, player.yBobO, player.yBob);
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees((player.getViewXRot(partialTicks) - xBob) * 0.1F));
        pose.mulPose(Axis.YP.rotationDegrees((player.getViewYRot(partialTicks) - yBob) * 0.1F));
        ItemInHandRendererInvoker self = (ItemInHandRendererInvoker) this;
        for (InteractionHand hand : FormHands.handsToDraw(player)) {
            boolean main = hand == InteractionHand.MAIN_HAND;
            ItemStack stack = main ? mainHandItem : offHandItem;
            if (!FormHands.shouldDraw(stack, main, armed)) continue;
            float swing = hand == swinging ? attack : 0.0F;
            float equipped = 1.0F - (main ? Mth.lerp(partialTicks, oMainHandHeight, mainHandHeight)
                                          : Mth.lerp(partialTicks, oOffHandHeight, offHandHeight));
            self.dungeontotems$renderArmWithItem(player, partialTicks, pitch, hand, swing, stack, equipped, pose, buffers, light);
            if (!stack.isEmpty()) FormHands.itemDrawn();
        }
        pose.popPose();
        buffers.endBatch();
    }
}
