package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.fabric.client.PartialTickHolder;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements PartialTickHolder {

    @Unique private float dungeontotems$partialTick;

    @Override
    public float dungeontotems$partialTick() {
        return dungeontotems$partialTick;
    }

    @Override
    public void dungeontotems$setPartialTick(float partialTick) {
        this.dungeontotems$partialTick = partialTick;
    }
}
