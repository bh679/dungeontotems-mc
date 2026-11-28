package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.platform.event.LivingChangeTargetEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forge: a brain mob (piglins, hoglins...) asks {@link LivingChangeTargetEvent} once it could attack its pick
 * (in the behaviour's lambda, after {@code canAttack}); cancelled, it doesn't take it.
 */
@Mixin(StartAttacking.class)
public abstract class StartAttackingMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Mob;canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private static boolean dungeontotems$changeTarget(Mob mob, LivingEntity target, Operation<Boolean> original) {
        if (!original.call(mob, target)) return false;
        return !TotemEvents.post(new LivingChangeTargetEvent(mob, target));
    }
}
