package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.morph.FormTraits;
import games.brennan.dungeontotems.registry.TotemAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A player in strider form stands on lava as a strider does ({@code Strider#canStandOnFluid}): lava's
 * surface holds them up and they walk on it with ground movement, not lava's drag.
 *
 * <p>1.20.1 only: a player's jump comes from {@link TotemAttributes#jumpStrength()} (base 0.42, vanilla's
 * fixed jump), so morph forms can change it as main's forms change vanilla 1.21's jump strength.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    protected abstract float getBlockJumpFactor();

    @Inject(method = "getJumpPower", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$playerJumpStrength(CallbackInfoReturnable<Float> result) {
        if (!((Object) this instanceof Player player)) return;
        AttributeInstance jump = player.getAttribute(TotemAttributes.jumpStrength());
        if (jump != null) result.setReturnValue((float) jump.getValue() * getBlockJumpFactor());
    }

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$standOnLavaAsAStrider(FluidState fluid, CallbackInfoReturnable<Boolean> result) {
        if ((Object) this instanceof Player player && FormTraits.standsOn(player, fluid)) result.setReturnValue(true);
    }
}
