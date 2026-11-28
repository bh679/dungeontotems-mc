package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.fabric.FabricHooks;
import games.brennan.dungeontotems.fabric.HealthDamageSink;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The common living-entity events, each at the spot NeoForge's patch fires its event (see the NeoForge
 * sources' {@code LivingEntity}): death after vanilla's totem, incoming damage after the invulnerability
 * checks, damage taken at the end of {@code hurtServer} (a fatal hit too), effects added, removed and
 * run out, falls, jumps, item use, and breathing.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHooksMixin implements HealthDamageSink {

    @Shadow protected boolean dead;
    @Shadow @Final private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    @Shadow protected abstract void onEffectsRemoved(Collection<MobEffectInstance> effects);

    @Shadow public abstract ItemStack getUseItem();

    /** What the current hit took off health, after armour and absorption ({@code actuallyHurt}). */
    @Unique private float dungeontotems$healthDamage;

    private LivingEntity dungeontotems$self() {
        return (LivingEntity) (Object) this;
    }

    // ---- death ----

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$death(DamageSource source, CallbackInfo ci) {
        if (dungeontotems$self().isRemoved() || this.dead) return;
        if (DtEvents.DEATH.post(new LivingEvents.Death(dungeontotems$self(), source)).isCanceled()) ci.cancel();
    }

    @Inject(method = "checkTotemDeathProtection", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;"))
    private void dungeontotems$vanillaTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        FabricHooks.useTotem(dungeontotems$self());
    }

    // ---- damage ----

    @Inject(method = "hurtServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void dungeontotems$incomingDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir,
                                             @Local(argsOnly = true) LocalFloatRef amount) {
        this.dungeontotems$healthDamage = 0.0F;
        LivingEvents.IncomingDamage event = DtEvents.INCOMING_DAMAGE.post(new LivingEvents.IncomingDamage(dungeontotems$self(), source, amount.get()));
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return;
        }
        amount.set(event.getAmount());
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void dungeontotems$healthDamage(ServerLevel level, DamageSource source, float dmg, CallbackInfo ci,
                                            @Local(argsOnly = true) float taken) {
        this.dungeontotems$healthDamage = taken;
    }

    @Override
    public void dungeontotems$setHealthDamage(float taken) {
        this.dungeontotems$healthDamage = taken;
    }

    @Inject(method = "hurtServer", at = @At("TAIL"))
    private void dungeontotems$damagePost(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        float taken = this.dungeontotems$healthDamage;
        this.dungeontotems$healthDamage = 0.0F;
        DtEvents.DAMAGE_POST.post(new LivingEvents.DamagePost(dungeontotems$self(), source, taken));
    }

    // ---- effects ----

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void dungeontotems$effectAdded(MobEffectInstance newEffect, @Nullable Entity source, CallbackInfoReturnable<Boolean> cir) {
        MobEffectInstance old = this.activeEffects.get(newEffect.getEffect());
        DtEvents.EFFECT_ADDED.post(new LivingEvents.EffectAdded(dungeontotems$self(), newEffect, old));
    }

    @Inject(method = "removeEffect", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$effectRemove(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (dungeontotems$removeRefused(effect)) cir.setReturnValue(false);
    }

    /** As NeoForge does: each effect may refuse to go, and only the rest are cleared. */
    @Inject(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap(Ljava/util/Map;)Ljava/util/HashMap;"), cancellable = true)
    private void dungeontotems$removeAll(CallbackInfoReturnable<Boolean> cir) {
        Map<Holder<MobEffect>, MobEffectInstance> removed = new HashMap<>(this.activeEffects.size());
        for (Map.Entry<Holder<MobEffect>, MobEffectInstance> entry : Map.copyOf(this.activeEffects).entrySet()) {
            if (!dungeontotems$removeRefused(entry.getKey())) removed.put(entry.getKey(), entry.getValue());
        }
        removed.keySet().forEach(this.activeEffects::remove);
        this.onEffectsRemoved(removed.values());
        cir.setReturnValue(true);
    }

    @Unique
    private boolean dungeontotems$removeRefused(Holder<MobEffect> effect) {
        return DtEvents.EFFECT_REMOVE.post(new LivingEvents.EffectRemove(dungeontotems$self(), effect, this.activeEffects.get(effect))).isCanceled();
    }

    @Inject(method = "tickEffects", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V"))
    private void dungeontotems$effectExpired(CallbackInfo ci, @Local(name = "effect") MobEffectInstance effect) {
        DtEvents.EFFECT_EXPIRED.post(new LivingEvents.EffectExpired(dungeontotems$self(), effect));
    }

    // ---- moving ----

    @Inject(method = "causeFallDamage", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/Entity;causeFallDamage(DFLnet/minecraft/world/damagesource/DamageSource;)Z"), cancellable = true)
    private void dungeontotems$fall(double fallDistance, float damageModifier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir,
                                    @Local(name = "effectiveFallDistance") LocalDoubleRef distance) {
        LivingEvents.Fall event = DtEvents.FALL.post(new LivingEvents.Fall(dungeontotems$self(), distance.get()));
        if (event.isCanceled()) {
            cir.setReturnValue(false);
            return;
        }
        distance.set(event.getDistance());
    }

    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void dungeontotems$jump(CallbackInfo ci) {
        DtEvents.JUMP.post(new LivingEvents.Jump(dungeontotems$self()));
    }

    /** Whether the entity drowns: a water form breathes there. */
    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canBreatheUnderwater()Z"))
    private boolean dungeontotems$breathe(boolean canBreathe) {
        return DtEvents.BREATHE.post(new LivingEvents.Breathe(dungeontotems$self(), canBreathe)).canBreathe();
    }

    // ---- using items ----

    @Inject(method = "startUsingItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;useItem:Lnet/minecraft/world/item/ItemStack;", opcode = org.objectweb.asm.Opcodes.PUTFIELD), cancellable = true)
    private void dungeontotems$startUsing(net.minecraft.world.InteractionHand hand, CallbackInfo ci, @Local(name = "itemStack") ItemStack stack) {
        if (FabricHooks.startUsingItem(dungeontotems$self(), stack)) ci.cancel();
    }

    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dungeontotems$finishUsing(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
        ItemStack before = stack.copy();
        ItemStack result = original.call(stack, level, entity);
        FabricHooks.finishUsingItem(dungeontotems$self(), before);
        return result;
    }

    @Inject(method = "releaseUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    private void dungeontotems$stopUsing(CallbackInfo ci) {
        DtEvents.USE_ITEM_STOP.post(new LivingEvents.UseItemStop(dungeontotems$self(), getUseItem()));
    }
}
