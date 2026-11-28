package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.event.AdvancementEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Forge: {@code onAdvancementEarnedEvent} in {@code award}, once the advancement is newly done (after its rewards). */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {

    @Shadow private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void dungeontotems$earned(Advancement advancement, String criterion, CallbackInfoReturnable<Boolean> cir) {
        TotemEvents.post(new AdvancementEvent.AdvancementEarnEvent(player, advancement));
    }
}
