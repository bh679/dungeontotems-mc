package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.OptionalInt;

/**
 * {@code ServerPlayer}'s own death (it doesn't call {@code LivingEntity#die}), after its death game event as
 * on NeoForge; and NeoForge's {@code PlayerContainerEvent.Open}, once the new menu is the player's.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerHooksMixin {

    private ServerPlayer dungeontotems$self() {
        return (ServerPlayer) (Object) this;
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER), cancellable = true)
    private void dungeontotems$death(DamageSource source, CallbackInfo ci) {
        if (DtEvents.DEATH.post(new LivingEvents.Death(dungeontotems$self(), source)).isCanceled()) ci.cancel();
    }

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;of(I)Ljava/util/OptionalInt;"))
    private void dungeontotems$openMenu(@Nullable MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        dungeontotems$opened();
    }

    @Inject(method = "openHorseInventory", at = @At("TAIL"))
    private void dungeontotems$openHorse(AbstractHorse horse, Container container, CallbackInfo ci) {
        dungeontotems$opened();
    }

    @Inject(method = "openNautilusInventory", at = @At("TAIL"))
    private void dungeontotems$openNautilus(AbstractNautilus nautilus, Container container, CallbackInfo ci) {
        dungeontotems$opened();
    }

    private void dungeontotems$opened() {
        ServerPlayer self = dungeontotems$self();
        DtEvents.CONTAINER_OPEN.post(new PlayerEvents.ContainerOpen(self, self.containerMenu));
    }
}
