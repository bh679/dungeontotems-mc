package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.fabric.FabricAttributes;
import games.brennan.dungeontotems.platform.event.EntityEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Forge 47's size event, and its player step-height bonus. */
@Mixin(Entity.class)
public abstract class EntityHooksMixin {

    @Shadow private EntityDimensions dimensions;

    /**
     * Forge: {@code refreshDimensions} sets {@code dimensions} from {@link EntityEvent.Size} and keeps the eye height
     * worked out from the vanilla size. So: after vanilla sets both, put the event's size in.
     */
    @Inject(method = "refreshDimensions", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/world/entity/Entity;eyeHeight:F"))
    private void dungeontotems$size(CallbackInfo ci, @Local(ordinal = 0) EntityDimensions old) {
        Entity self = (Entity) (Object) this;
        EntityEvent.Size event = new EntityEvent.Size(self, self.getPose(), old, dimensions);
        TotemEvents.post(event);
        dimensions = event.getNewSize();
    }

    /** Forge's {@code getStepHeight}: a player's step is vanilla's plus {@code step_height_addition}, never below 0. */
    @Inject(method = "maxUpStep", at = @At("RETURN"), cancellable = true)
    private void dungeontotems$step(CallbackInfoReturnable<Float> cir) {
        if (!((Object) this instanceof Player player)) return;
        AttributeInstance bonus = player.getAttribute(FabricAttributes.stepHeight());
        if (bonus == null) return;
        cir.setReturnValue(Math.max(0.0F, cir.getReturnValueF() + (float) bonus.getValue()));
    }
}
