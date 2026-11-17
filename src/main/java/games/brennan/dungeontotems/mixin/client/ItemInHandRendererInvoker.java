package games.brennan.dungeontotems.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla's first-person hand drawing, reused for a morphed player: an armed form's own arm, and held items. */
@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererInvoker {

    @Invoker("renderPlayerArm")
    void dungeontotems$renderPlayerArm(PoseStack pose, MultiBufferSource buffers, int light, float equippedProgress,
                                       float swingProgress, HumanoidArm side);

    @Invoker("renderArmWithItem")
    void dungeontotems$renderArmWithItem(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand,
                                         float swingProgress, ItemStack stack, float equippedProgress, PoseStack pose,
                                         MultiBufferSource buffers, int light);
}
