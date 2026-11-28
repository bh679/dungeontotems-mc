package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import games.brennan.dungeontotems.platform.event.EventHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Forge: {@code if (random.nextInt(10) == 0 && !onAnimalTame(this, player))}. A refused tame is a failed roll. */
@Mixin(Parrot.class)
public abstract class ParrotTameMixin {

    @ModifyExpressionValue(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
    private int dungeontotems$tame(int roll, Player player, InteractionHand hand) {
        return roll == 0 && EventHooks.onAnimalTame((Parrot) (Object) this, player) ? 1 : roll;
    }
}
