package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.event.LivingDeathEvent;
import games.brennan.dungeontotems.platform.event.PlayerContainerEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

/** Forge 47's {@code ServerPlayer} patches: death (it overrides {@code die} without super) and menus opening. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerHooksMixin {

    private ServerPlayer dungeontotems$self() {
        return (ServerPlayer) (Object) this;
    }

    /** Forge: {@code if (onLivingDeath(this, cause)) return;} right after the death game event. */
    @Inject(method = "die", cancellable = true, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V", shift = At.Shift.AFTER))
    private void dungeontotems$die(DamageSource source, CallbackInfo ci) {
        if (TotemEvents.post(new LivingDeathEvent(dungeontotems$self(), source))) ci.cancel();
    }

    /** Forge: {@code PlayerContainerEvent.Open} once {@code openMenu} has set the new menu. */
    @Inject(method = "openMenu", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
        target = "Lnet/minecraft/server/level/ServerPlayer;containerMenu:Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private void dungeontotems$openMenu(MenuProvider provider, CallbackInfoReturnable<OptionalInt> cir) {
        TotemEvents.post(new PlayerContainerEvent.Open(dungeontotems$self(), dungeontotems$self().containerMenu));
    }

    @Inject(method = "openHorseInventory", at = @At("TAIL"))
    private void dungeontotems$openHorse(AbstractHorse horse, Container inventory, CallbackInfo ci) {
        TotemEvents.post(new PlayerContainerEvent.Open(dungeontotems$self(), dungeontotems$self().containerMenu));
    }
}
