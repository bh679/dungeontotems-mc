package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import games.brennan.dungeontotems.platform.event.SubscribeEvent;
import games.brennan.dungeontotems.platform.event.EventBusSubscriber;
import games.brennan.dungeontotems.platform.Services;
import games.brennan.dungeontotems.platform.event.EntityJoinLevelEvent;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;

import javax.annotation.Nullable;

/**
 * Nobody trades with a Zombified player. A villager you try to trade with turns into a zombie villager,
 * as if a zombie had bitten it (profession, level, trades and gossip kept, so it can be cured). A
 * wandering trader drinks its invisibility potion and runs, and keeps away from you while you are a zombie.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class ZombieTrade {

    private static final int TRADER_INVISIBLE_TICKS = 600;
    private static final float TRADER_AVOID_DISTANCE = 8.0F;
    private static final double TRADER_WALK_SPEED = 0.5;
    private static final double TRADER_SPRINT_SPEED = 0.5;
    /** {@code LevelEvent.SOUND_ZOMBIE_INFECTED}. */
    private static final int ZOMBIE_INFECTED_EVENT = 1026;

    private ZombieTrade() {}

    /** Both sides, so the trade screen never opens. */
    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (!ZombieDiet.isZombie(player)) return;
        if (event.getTarget() instanceof Villager villager && !villager.isBaby()) {
            refuse(event);
            if (player.level() instanceof ServerLevel level) infect(level, villager);
        } else if (event.getTarget() instanceof WanderingTrader trader) {
            refuse(event);
            if (!player.level().isClientSide()) flee(trader, player);
        }
    }

    private static void refuse(PlayerInteractEvent.EntityInteract event) {
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
        event.setCanceled(true);
    }

    /** {@code Zombie#killedEntity}'s conversion, without its difficulty roll. */
    @Nullable
    static ZombieVillager infect(ServerLevel level, Villager villager) {
        if (!Services.HOOKS.canLivingConvert(villager, EntityType.ZOMBIE_VILLAGER)) return null;
        ZombieVillager zombie = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
        if (zombie == null) return null;
        zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION,
            new Zombie.ZombieGroupData(false, true), null);
        zombie.setVillagerData(villager.getVillagerData());
        zombie.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
        zombie.setTradeOffers(villager.getOffers().createTag());
        zombie.setVillagerXp(villager.getVillagerXp());
        Services.HOOKS.onLivingConvert(villager, zombie);
        if (!villager.isSilent()) level.levelEvent(null, ZOMBIE_INFECTED_EVENT, villager.blockPosition(), 0);
        return zombie;
    }

    /** It drinks its night-time potion, and being "hurt" by you sets off its {@code PanicGoal}. */
    static void flee(WanderingTrader trader, Player player) {
        if (!trader.hasEffect(MobEffects.INVISIBILITY)) {
            trader.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, TRADER_INVISIBLE_TICKS));
            trader.playSound(SoundEvents.WANDERING_TRADER_DRINK_POTION, 1.0F, 1.0F);
        }
        trader.setLastHurtByMob(player);
    }

    /** Wandering traders keep away from Zombified players, as they do from zombies. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof WanderingTrader trader)) return;
        ((games.brennan.dungeontotems.mixin.MobInvoker) trader).dungeontotems$goalSelector().addGoal(1, new AvoidEntityGoal<>(trader, Player.class, TRADER_AVOID_DISTANCE,
            TRADER_WALK_SPEED, TRADER_SPRINT_SPEED, ZombieTrade::isZombieEntity));
    }

    private static boolean isZombieEntity(LivingEntity entity) {
        return entity instanceof Player player && ZombieDiet.isZombie(player);
    }
}
