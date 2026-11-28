package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.fabric.FabricHooks;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's {@code AnimalTameEvent} spot for horses, donkeys and llamas: a refused rider's temper roll fails. */
@Mixin(RunAroundLikeCrazyGoal.class)
public abstract class HorseTameMixin {

    @Shadow @Final private AbstractHorse horse;

    @ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 1))
    private int dungeontotems$tame(int roll, @Local(name = "player") Player player, @Local(name = "temper") int temper) {
        return roll < temper && FabricHooks.tameRefused(this.horse, player) ? Integer.MAX_VALUE : roll;
    }
}
