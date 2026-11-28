package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.event.TotemEvents;
import games.brennan.dungeontotems.platform.event.TradeWithVillagerEvent;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge: {@code TradeWithVillagerEvent} at the end of {@code notifyTrade}. */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {

    @Inject(method = "notifyTrade", at = @At("TAIL"))
    private void dungeontotems$traded(MerchantOffer offer, CallbackInfo ci) {
        AbstractVillager self = (AbstractVillager) (Object) this;
        TotemEvents.post(new TradeWithVillagerEvent(self.getTradingPlayer(), offer, self));
    }
}
