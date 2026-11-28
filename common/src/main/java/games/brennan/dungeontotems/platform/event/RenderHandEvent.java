package games.brennan.dungeontotems.platform.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Client: one first-person hand (and its item) about to be drawn. Cancel to skip it. */
public final class RenderHandEvent extends Event {
    private final InteractionHand hand;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int packedLight;
    private final float partialTick;
    private final ItemStack stack;

    public RenderHandEvent(InteractionHand hand, PoseStack poseStack, MultiBufferSource buffers, int packedLight,
                           float partialTick, ItemStack stack) {
        this.hand = hand;
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.packedLight = packedLight;
        this.partialTick = partialTick;
        this.stack = stack;
    }

    public InteractionHand getHand() { return hand; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return buffers; }
    public int getPackedLight() { return packedLight; }
    public float getPartialTick() { return partialTick; }
    public ItemStack getItemStack() { return stack; }
}
