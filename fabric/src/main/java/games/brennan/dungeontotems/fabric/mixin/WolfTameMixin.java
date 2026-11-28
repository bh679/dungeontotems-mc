package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.fabric.FabricHooks;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's {@code AnimalTameEvent} spot: a refused taming fails its 1-in-3 roll. */
@Mixin(Wolf.class)
public abstract class WolfTameMixin {

    @ModifyExpressionValue(method = "tryToTame", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I", ordinal = 0))
    private int dungeontotems$tame(int roll, @Local(argsOnly = true) Player player) {
        return roll == 0 && FabricHooks.tameRefused((Wolf) (Object) this, player) ? 1 : roll;
    }
}
