package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import games.brennan.dungeontotems.platform.event.RenderHandEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Forge's {@code RenderHandEvent}: each first-person hand in {@code renderHandsWithItems} is asked first. */
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererHandsMixin {

    @WrapWithCondition(method = "renderHandsWithItems", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"))
    private boolean dungeontotems$hand(ItemInHandRenderer self, AbstractClientPlayer player, float partialTick, float pitch,
                                       InteractionHand hand, float swing, ItemStack stack, float equipped, PoseStack pose,
                                       MultiBufferSource buffers, int light) {
        return !TotemEvents.post(new RenderHandEvent(hand, pose, buffers, light, partialTick, stack));
    }
}
