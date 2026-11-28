package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.platform.event.MobEffectEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.Map;

/** Forge 47's effect events: added, removed (one, or all), and run out. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityEffectsMixin {

    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;

    @Shadow protected abstract void onEffectRemoved(MobEffectInstance effect);

    @Shadow public abstract MobEffectInstance getEffect(MobEffect effect);

    private LivingEntity dungeontotems$living() {
        return (LivingEntity) (Object) this;
    }

    /** Forge: {@code post(new MobEffectEvent.Added(this, old, new, source))} once {@code canBeAffected} said yes. */
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void dungeontotems$added(MobEffectInstance instance, Entity source, CallbackInfoReturnable<Boolean> cir) {
        TotemEvents.post(new MobEffectEvent.Added(dungeontotems$living(), getEffect(instance.getEffect()), instance, source));
    }

    /** Forge: {@code if (post(new MobEffectEvent.Remove(this, effect))) return false;} first in {@code removeEffect}. */
    @Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$remove(MobEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (TotemEvents.post(new MobEffectEvent.Remove(dungeontotems$living(), effect, getEffect(effect)))) cir.setReturnValue(false);
    }

    /** Forge's {@code removeAllEffects}: each effect is asked first ({@link MobEffectEvent.Remove}) and may stay. */
    @Inject(method = "removeAllEffects", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$removeAll(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = dungeontotems$living();
        if (self.level().isClientSide) {
            cir.setReturnValue(false);
            return;
        }
        boolean any = false;
        Iterator<MobEffectInstance> iterator = activeEffects.values().iterator();
        while (iterator.hasNext()) {
            MobEffectInstance effect = iterator.next();
            any = true; // Forge's loop marks every effect it sees, kept or not
            if (TotemEvents.post(new MobEffectEvent.Remove(self, effect.getEffect(), effect))) continue;
            onEffectRemoved(effect);
            iterator.remove();
        }
        cir.setReturnValue(any);
    }

    /** Forge: {@code post(new MobEffectEvent.Expired(this, effect))} just before {@code tickEffects} drops a finished one. */
    @Inject(method = "tickEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void dungeontotems$expired(CallbackInfo ci, @Local MobEffectInstance effect) {
        TotemEvents.post(new MobEffectEvent.Expired(dungeontotems$living(), effect));
    }
}
