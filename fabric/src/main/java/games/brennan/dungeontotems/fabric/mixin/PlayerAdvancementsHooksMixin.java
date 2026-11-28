package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's {@code AdvancementEarnEvent}: an advancement with a display has just been earned. */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsHooksMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.AFTER))
    private void dungeontotems$earned(AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        if (holder.value().display().isPresent()) {
            DtEvents.ADVANCEMENT_EARN.post(new PlayerEvents.AdvancementEarn(this.player, holder));
        }
    }
}
