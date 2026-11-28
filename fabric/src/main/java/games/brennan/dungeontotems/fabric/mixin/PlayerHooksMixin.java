package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import games.brennan.dungeontotems.fabric.FabricAttributes;
import games.brennan.dungeontotems.fabric.FabricHooks;
import games.brennan.dungeontotems.platform.event.AttackEntityEvent;
import games.brennan.dungeontotems.platform.event.LivingHurtEvent;
import games.brennan.dungeontotems.platform.event.PlayerEvent;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.TickEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import games.brennan.dungeontotems.registry.TotemAttributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Forge 47's {@code Player} patches: tick, hurt and damage, attack, entity right-click, break speed, attributes. */
@Mixin(Player.class)
public abstract class PlayerHooksMixin {

    private Player dungeontotems$self() {
        return (Player) (Object) this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void dungeontotems$tickStart(CallbackInfo ci) {
        TotemEvents.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, dungeontotems$self()));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dungeontotems$tickEnd(CallbackInfo ci) {
        TotemEvents.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, dungeontotems$self()));
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
        target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private void dungeontotems$nothingLeft(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) float now) {
        if (now <= 0.0F) ci.cancel();
    }

    /**
     * Forge, in Player: {@code f1 = onLivingDamage(...)} right after the absorption is taken, before the absorbed
     * stat: the amount slot's 6th read (javap of 1.20.1's {@code Player}; see {@code LivingEntityHooksMixin}).
     */
    @ModifyVariable(method = "actuallyHurt", argsOnly = true, ordinal = 0, at = @At(value = "LOAD", ordinal = 5))
    private float dungeontotems$damage(float amount, DamageSource source, float raw) {
        return FabricHooks.damage(dungeontotems$self(), source, amount);
    }

    /** Forge: {@code if (!onPlayerAttackTarget(this, target)) return;} first in {@code attack}. */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$attack(Entity target, CallbackInfo ci) {
        if (TotemEvents.post(new AttackEntityEvent(dungeontotems$self(), target))) ci.cancel();
    }

    /** Forge: {@code onInteractEntity} once {@code interactOn} is past the spectator check; cancelled returns its result. */
    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$interact(Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Player self = dungeontotems$self();
        if (self.isSpectator()) return;
        PlayerInteractEvent.EntityInteract event = new PlayerInteractEvent.EntityInteract(self, hand, target);
        if (TotemEvents.post(event)) cir.setReturnValue(event.getCancellationResult());
    }

    /** Forge: {@code getBreakSpeed} on the way out of {@code getDestroySpeed}; cancelled is -1 (no progress). */
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void dungeontotems$breakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        PlayerEvent.BreakSpeed event = new PlayerEvent.BreakSpeed(dungeontotems$self(), state, cir.getReturnValueF(), null);
        cir.setReturnValue(TotemEvents.post(event) ? -1.0F : event.getNewSpeed());
    }

    /** What Forge's {@code EntityAttributeModificationEvent} adds on Forge: our jump and step height. */
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void dungeontotems$attributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(TotemAttributes.jumpStrength()).add(FabricAttributes.stepHeight());
    }
}
