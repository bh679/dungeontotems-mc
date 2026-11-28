package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import games.brennan.dungeontotems.platform.event.LivingBreatheEvent;
import games.brennan.dungeontotems.fabric.FabricHooks;
import games.brennan.dungeontotems.platform.event.LivingDeathEvent;
import games.brennan.dungeontotems.platform.event.LivingEntityUseItemEvent;
import games.brennan.dungeontotems.platform.event.LivingEvent;
import games.brennan.dungeontotems.platform.event.LivingFallEvent;
import games.brennan.dungeontotems.platform.event.LivingHurtEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Forge 47's {@code LivingEntity} patches, as Fabric mixins at the same spots: hurt and damage in
 * {@code actuallyHurt} (players: {@link PlayerHooksMixin}), death at the top of {@code die}, fall, jump, breath
 * and item use. See {@code forge-1.20.1-47.4.29-sources.jar} for the lines each one mirrors.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHooksMixin {

    @Shadow protected ItemStack useItem;

    @Shadow public abstract ItemStack getItemInHand(InteractionHand hand);

    @Shadow public abstract boolean isUsingItem();

    @Shadow public abstract int getUseItemRemainingTicks();

    private LivingEntity dungeontotems$self() {
        return (LivingEntity) (Object) this;
    }

    /**
     * Forge: {@code amount = onLivingHurt(...); if (amount <= 0) return;} straight after the invulnerable check, so
     * armour sees the new amount: set as the amount is first read (for armour), then stop if nothing is left.
     */
    @ModifyVariable(method = "actuallyHurt", argsOnly = true, ordinal = 0, at = @At(value = "LOAD", ordinal = 0))
    private float dungeontotems$hurt(float amount, DamageSource source, float raw) {
        LivingHurtEvent event = new LivingHurtEvent(dungeontotems$self(), source, amount);
        return TotemEvents.post(event) ? 0.0F : event.getAmount();
    }

    @Inject(method = "actuallyHurt", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private void dungeontotems$nothingLeft(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) float now) {
        if (now <= 0.0F) ci.cancel();
    }

    /**
     * Forge: {@code f1 = onLivingDamage(...)} after armour, absorption and the absorbed-damage stat, just before
     * {@code if (f1 != 0)} and {@code setHealth}. Vanilla's bytecode keeps {@code f1} in the amount argument's own
     * slot; that check is the slot's 7th read (javap of 1.20.1's {@code LivingEntity}). Cancelled is 0, as Forge's.
     */
    @ModifyVariable(method = "actuallyHurt", argsOnly = true, ordinal = 0, at = @At(value = "LOAD", ordinal = 6))
    private float dungeontotems$damage(float amount, DamageSource source, float raw) {
        return FabricHooks.damage(dungeontotems$self(), source, amount);
    }

    /** Forge: {@code if (onLivingDeath(this, source)) return;} first thing in {@code die}. */
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$die(DamageSource source, CallbackInfo ci) {
        if (TotemEvents.post(new LivingDeathEvent(dungeontotems$self(), source))) ci.cancel();
    }

    /** Forge: {@code onLivingFall} first thing in {@code causeFallDamage}; cancelled is no fall damage. */
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$fall(float distance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir,
                                    @Local(argsOnly = true, ordinal = 0) LocalFloatRef distanceRef,
                                    @Local(argsOnly = true, ordinal = 1) LocalFloatRef multiplierRef) {
        LivingFallEvent event = new LivingFallEvent(dungeontotems$self(), distance, multiplier);
        if (TotemEvents.post(event)) {
            cir.setReturnValue(false);
            return;
        }
        distanceRef.set(event.getDistance());
        multiplierRef.set(event.getDamageMultiplier());
    }

    /** Forge: {@code onLivingJump(this)} at the end of {@code jumpFromGround}. */
    @Inject(method = "jumpFromGround", at = @At("TAIL"))
    private void dungeontotems$jump(CallbackInfo ci) {
        TotemEvents.post(new LivingEvent.LivingJumpEvent(dungeontotems$self()));
    }

    /**
     * Forge's {@code onLivingBreathe} under water: whether the entity drowns there. True is no air lost (Forge
     * refills air only in air, so neither does this). On land vanilla's refill stands: no listener stops breathing there.
     */
    @ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;canBreatheUnderwater()Z"))
    private boolean dungeontotems$breathe(boolean breathesUnderwater) {
        LivingEntity self = dungeontotems$self();
        boolean vanilla = breathesUnderwater || MobEffectUtil.hasWaterBreathing(self)
            || (self instanceof Player player && player.getAbilities().invulnerable);
        LivingBreatheEvent event = new LivingBreatheEvent(self, vanilla);
        TotemEvents.post(event);
        return event.canBreathe();
    }

    /** Forge: {@code onItemUseStart} once {@code startUsingItem} knows it will start; cancelled is no start. */
    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$startUsing(InteractionHand hand, CallbackInfo ci) {
        ItemStack stack = getItemInHand(hand);
        if (stack.isEmpty() || isUsingItem()) return;
        if (TotemEvents.post(new LivingEntityUseItemEvent.Start(dungeontotems$self(), stack, stack.getUseDuration()))) ci.cancel();
    }

    /** Forge: {@code onUseItemStop} first in {@code releaseUsingItem}. */
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void dungeontotems$stopUsing(CallbackInfo ci) {
        if (!useItem.isEmpty()) {
            TotemEvents.post(new LivingEntityUseItemEvent.Stop(dungeontotems$self(), useItem, getUseItemRemainingTicks()));
        }
    }

    /** Forge: {@code onItemUseFinish(this, copy, remaining, useItem.finishUsingItem(...))} in {@code completeUsingItem}. */
    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dungeontotems$finishUsing(ItemStack stack, Level level, LivingEntity entity, Operation<ItemStack> original) {
        ItemStack copy = stack.copy();
        ItemStack result = original.call(stack, level, entity);
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(dungeontotems$self(), copy,
            getUseItemRemainingTicks(), result);
        TotemEvents.post(event);
        return event.getResultStack();
    }
}
