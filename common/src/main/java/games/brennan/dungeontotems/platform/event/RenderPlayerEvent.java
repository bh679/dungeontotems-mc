package games.brennan.dungeontotems.platform.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;

/** Client: a player about to be drawn ({@code PlayerRenderer#render}). Cancel to draw something else. */
public abstract class RenderPlayerEvent extends PlayerEvent {
    private final PlayerRenderer renderer;
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int packedLight;

    protected RenderPlayerEvent(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack,
                                MultiBufferSource buffers, int packedLight) {
        super(player);
        this.renderer = renderer;
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.packedLight = packedLight;
    }

    public PlayerRenderer getRenderer() { return renderer; }
    public float getPartialTick() { return partialTick; }
    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return buffers; }
    public int getPackedLight() { return packedLight; }

    public static final class Pre extends RenderPlayerEvent {
        public Pre(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack,
                   MultiBufferSource buffers, int packedLight) {
            super(player, renderer, partialTick, poseStack, buffers, packedLight);
        }
    }
}
