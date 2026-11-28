package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

/** NeoForge's {@code LivingChangeTargetEvent} for goal-based mobs: a refused target leaves the old one. */
@Mixin(Mob.class)
public abstract class MobHooksMixin {

    @Shadow @Nullable private LivingEntity target;

    @ModifyExpressionValue(method = "setTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;asValidTarget(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/entity/LivingEntity;"))
    @Nullable
    private LivingEntity dungeontotems$changeTarget(@Nullable LivingEntity newTarget) {
        boolean refused = DtEvents.CHANGE_TARGET.post(new LivingEvents.ChangeTarget((Mob) (Object) this, newTarget)).isCanceled();
        return refused ? this.target : newTarget;
    }
}
