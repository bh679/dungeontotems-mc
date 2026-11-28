package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * NeoForge's {@code LivingChangeTargetEvent} for brain-driven mobs (piglins, hoglins, zoglins...): where
 * {@code StartAttacking} checks it may attack the target it found, a refused target is one it can't.
 */
@Mixin(StartAttacking.class)
public abstract class StartAttackingHooksMixin {

    @WrapOperation(method = "lambda$create$3", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean dungeontotems$changeTarget(Mob body, LivingEntity target, Operation<Boolean> original) {
        if (!original.call(body, target)) return false;
        return !DtEvents.CHANGE_TARGET.post(new LivingEvents.ChangeTarget(body, target)).isCanceled();
    }
}
