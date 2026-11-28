package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.dungeontotems.platform.event.EventHooks;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forge: {@code if (j > 0 && random.nextInt(j) < temper && !onAnimalTame(horse, player))} as a ridden horse settles.
 * A refused tame is a roll too high to tame.
 */
@Mixin(RunAroundLikeCrazyGoal.class)
public abstract class HorseTameMixin {

    @Shadow @Final private AbstractHorse horse;

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 1))
    private int dungeontotems$tame(int roll) {
        if (roll >= horse.getTemper() || !(horse.getFirstPassenger() instanceof Player player)) return roll;
        return EventHooks.onAnimalTame(horse, player) ? Integer.MAX_VALUE : roll;
    }
}
