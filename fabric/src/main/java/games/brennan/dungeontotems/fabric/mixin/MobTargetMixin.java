package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.event.LivingChangeTargetEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge: {@code setTarget} asks {@link LivingChangeTargetEvent} first; cancelled keeps the old target. */
@Mixin(Mob.class)
public abstract class MobTargetMixin {

    @Shadow private LivingEntity target;

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$changeTarget(LivingEntity newTarget, CallbackInfo ci) {
        LivingChangeTargetEvent event = new LivingChangeTargetEvent((Mob) (Object) this, newTarget);
        if (TotemEvents.post(event)) {
            ci.cancel();
        } else if (event.getNewTarget() != newTarget) {
            target = event.getNewTarget();
            ci.cancel();
        }
    }
}
