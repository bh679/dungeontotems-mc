package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.morph.FormAir;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A land form moves through the air at its own pace ({@link FormAir}). The value is set only when the form
 * changes it: setting it ends the method, so other mods' hooks here would never run.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getFlyingSpeed", at = @At("RETURN"), cancellable = true)
    private void dungeontotems$airSpeedOfTheForm(CallbackInfoReturnable<Float> result) {
        float vanilla = result.getReturnValueF();
        float form = FormAir.airSpeed((Player) (Object) this, vanilla);
        if (form != vanilla) result.setReturnValue(form);
    }
}
