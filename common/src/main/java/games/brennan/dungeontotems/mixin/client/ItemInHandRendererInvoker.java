package games.brennan.dungeontotems.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Vanilla's first-person hand drawing, reused for a morphed player: an armed form's own arm, and held items. */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public interface ItemInHandRendererInvoker {

    @Invoker("submitArmWithItem")
    void dungeontotems$submitArmWithItem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state,
                                         float partialTicks, float xRot, InteractionHand hand, float attack,
                                         ItemStack stack, float inverseArmHeight, PoseStack pose,
                                         SubmitNodeCollector collector, int lightCoords);
}
