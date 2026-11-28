package games.brennan.dungeontotems.platform.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;

/** Client: the bare first-person arm about to be drawn. Cancel to skip it. */
public final class RenderArmEvent extends Event {
    private final PoseStack poseStack;
    private final MultiBufferSource buffers;
    private final int packedLight;
    private final AbstractClientPlayer player;
    private final HumanoidArm arm;

    public RenderArmEvent(PoseStack poseStack, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
                          HumanoidArm arm) {
        this.poseStack = poseStack;
        this.buffers = buffers;
        this.packedLight = packedLight;
        this.player = player;
        this.arm = arm;
    }

    public PoseStack getPoseStack() { return poseStack; }
    public MultiBufferSource getMultiBufferSource() { return buffers; }
    public int getPackedLight() { return packedLight; }
    public AbstractClientPlayer getPlayer() { return player; }
    public HumanoidArm getArm() { return arm; }
}
