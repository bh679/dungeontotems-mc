package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.Services;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Milk cures what milk cures, as on Forge: not an uncurable curse ({@code LoaderHooks#cureWithMilk}). */
@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {

    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean dungeontotems$cure(LivingEntity entity) {
        Services.HOOKS.cureWithMilk(entity);
        return true;
    }
}
