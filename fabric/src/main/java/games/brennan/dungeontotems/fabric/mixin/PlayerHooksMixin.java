package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.fabric.FabricHooks;
import games.brennan.dungeontotems.fabric.HealthDamageSink;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The common player events at NeoForge's spots in {@code Player}: the tick's start and end (both sides),
 * the hit's health damage, break speed, right-clicking an entity and attacking one.
 */
@Mixin(Player.class)
public abstract class PlayerHooksMixin {

    private Player dungeontotems$self() {
        return (Player) (Object) this;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void dungeontotems$tickPre(CallbackInfo ci) {
        DtEvents.PLAYER_TICK_PRE.post(new PlayerEvents.TickPre(dungeontotems$self()));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dungeontotems$tickPost(CallbackInfo ci) {
        DtEvents.PLAYER_TICK_POST.post(new PlayerEvents.TickPost(dungeontotems$self()));
    }

    @Inject(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
    private void dungeontotems$healthDamage(ServerLevel level, DamageSource source, float dmg, CallbackInfo ci,
                                            @Local(argsOnly = true) float taken) {
        ((HealthDamageSink) this).dungeontotems$setHealthDamage(taken);
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void dungeontotems$breakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (FabricHooks.breakSpeedRefused(dungeontotems$self(), state)) cir.setReturnValue(0.0F);
    }

    @Inject(method = "interactOn", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$interact(Entity entity, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (dungeontotems$self().isSpectator()) return; // vanilla's spectator branch comes first, as on NeoForge
        InteractionResult refused = FabricHooks.interactEntity(dungeontotems$self(), hand, entity);
        if (refused != null) cir.setReturnValue(refused);
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$attack(Entity entity, CallbackInfo ci) {
        if (FabricHooks.attack(dungeontotems$self(), entity)) ci.cancel();
    }
}
