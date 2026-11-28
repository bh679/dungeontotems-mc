package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.core.component.DataComponents;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

/**
 * Gifts for the Lonely curse, each restarting its timer ({@link CurseTimers#gift}): a trade with a
 * villager, food you threw that a villager picked up, or a gift to a PlayerMob
 * ({@link games.brennan.dungeontotems.compat.PlayerMobGifts}). Villagers always count: giving harms nobody.
 */
public final class LonelyGifts {

    /** How close a villager must be to a vanished food item to have picked it up. */
    private static final double PICKUP_REACH = 1.5;

    private LonelyGifts() {}

    public static void onTrade(PlayerEvents.Trade event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAbstractVillager() instanceof Villager) {
            CurseTimers.gift(player);
        }
    }

    /** Food a lonely player threw, gone because a villager next to it picked it up. */
    public static void onItemGone(LivingEvents.LeaveLevel event) {
        if (!(event.getEntity() instanceof ItemEntity item) || item.getRemovalReason() != Entity.RemovalReason.DISCARDED) return;
        if (!(item.getOwner() instanceof ServerPlayer player) || !player.hasEffect(TotemRegistries.LONELY)) return;
        if (!item.getItem().has(DataComponents.VILLAGER_FOOD)) return;
        if (!event.getLevel().getEntitiesOfClass(Player.class, item.getBoundingBox().inflate(PICKUP_REACH)).isEmpty()) return;
        if (!event.getLevel().getEntitiesOfClass(Villager.class, item.getBoundingBox().inflate(PICKUP_REACH)).isEmpty()) {
            CurseTimers.gift(player);
        }
    }
}
