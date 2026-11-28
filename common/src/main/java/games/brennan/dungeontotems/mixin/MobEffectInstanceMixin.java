package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.curse.CurseMarks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The curse flag of {@link CurseMarks}: saved with the instance, and kept when it is copied or updated. */
@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements CurseMarks.Marked {

    @Unique
    private static final String DUNGEONTOTEMS_TAG = "dungeontotems:curse";

    @Unique
    private boolean dungeontotems$curse;

    @Override
    public boolean dungeontotems$isCurse() {
        return dungeontotems$curse;
    }

    @Override
    public void dungeontotems$setCurse(boolean curse) {
        dungeontotems$curse = curse;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("RETURN"))
    private void dungeontotems$copy(MobEffectInstance other, CallbackInfo ci) {
        dungeontotems$curse = CurseMarks.isCurse(other);
    }

    /** A stronger or longer instance taking over brings its own flag (a potion over a curse is no curse). */
    @Inject(method = "update", at = @At("RETURN"))
    private void dungeontotems$update(MobEffectInstance other, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) dungeontotems$curse = CurseMarks.isCurse(other);
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void dungeontotems$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (dungeontotems$curse) tag.putBoolean(DUNGEONTOTEMS_TAG, true);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private static void dungeontotems$load(CompoundTag tag, CallbackInfoReturnable<MobEffectInstance> cir) {
        MobEffectInstance loaded = cir.getReturnValue();
        if (loaded != null && tag.getBoolean(DUNGEONTOTEMS_TAG)) CurseMarks.mark(loaded);
    }
}
