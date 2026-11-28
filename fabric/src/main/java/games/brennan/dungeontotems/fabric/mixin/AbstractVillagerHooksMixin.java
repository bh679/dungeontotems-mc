package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

/** NeoForge's {@code TradeWithVillagerEvent}: after each trade. */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerHooksMixin {

    @Shadow @Nullable public abstract Player getTradingPlayer();

    @Inject(method = "notifyTrade", at = @At("TAIL"))
    private void dungeontotems$trade(MerchantOffer offer, CallbackInfo ci) {
        Player player = getTradingPlayer();
        if (player != null) DtEvents.TRADE.post(new PlayerEvents.Trade(player, (AbstractVillager) (Object) this));
    }
}
