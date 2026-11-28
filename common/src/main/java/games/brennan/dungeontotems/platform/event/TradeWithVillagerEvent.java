package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffer;

/** A trade a player just took from a villager or wandering trader (server). */
public final class TradeWithVillagerEvent extends PlayerEvent {
    private final MerchantOffer offer;
    private final AbstractVillager villager;

    public TradeWithVillagerEvent(Player player, MerchantOffer offer, AbstractVillager villager) {
        super(player);
        this.offer = offer;
        this.villager = villager;
    }

    public MerchantOffer getMerchantOffer() { return offer; }
    public AbstractVillager getAbstractVillager() { return villager; }
}
