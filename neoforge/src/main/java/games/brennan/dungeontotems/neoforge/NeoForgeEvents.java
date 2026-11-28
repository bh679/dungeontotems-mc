package games.brennan.dungeontotems.neoforge;

import games.brennan.dungeontotems.event.DtBus;
import games.brennan.dungeontotems.event.DtEvent;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.event.Priority;
import games.brennan.dungeontotems.event.ServerEvents;
import net.minecraft.util.TriState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.LivingBreatheEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingUseTotemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Fires the common events ({@link DtEvents}) from NeoForge's. Each common priority in use gets its own
 * NeoForge listener at the same priority, so other mods' listeners fall in between exactly as they did
 * when the handlers were {@code @SubscribeEvent}s. A cancelled NeoForge event reaches none of them.
 */
public final class NeoForgeEvents {

    private NeoForgeEvents() {}

    /**
     * Listens on {@code type} for every priority {@code bus} uses: wraps the NeoForge event (null: skip it), runs that
     * priority's handlers, then copies changes (and a cancel) back.
     */
    static <N extends Event, C extends DtEvent> void bridge(IEventBus neo, DtBus<C> bus, Class<N> type,
                                                           Function<N, C> wrap, BiConsumer<N, C> copyBack) {
        for (Priority priority : bus.priorities()) {
            neo.addListener(neoPriority(priority), false, type, event -> {
                C common = wrap.apply(event);
                if (common == null) return; // not one of ours
                bus.post(common, priority);
                copyBack.accept(event, common);
                if (common.isCanceled() && event instanceof ICancellableEvent cancellable) cancellable.setCanceled(true);
            });
        }
    }

    static <N extends Event, C extends DtEvent> void bridge(IEventBus neo, DtBus<C> bus, Class<N> type, Function<N, C> wrap) {
        bridge(neo, bus, type, wrap, (event, common) -> {});
    }

    static EventPriority neoPriority(Priority priority) {
        return EventPriority.valueOf(priority.name());
    }

    public static void register() {
        IEventBus neo = NeoForge.EVENT_BUS;
        living(neo);
        player(neo);
        bridge(neo, DtEvents.SERVER_STARTED, ServerStartedEvent.class, e -> new ServerEvents.Started(e.getServer()));
        bridge(neo, DtEvents.REGISTER_COMMANDS, RegisterCommandsEvent.class,
            e -> new ServerEvents.RegisterCommands(e.getDispatcher(), e.getBuildContext()));
    }

    private static void living(IEventBus neo) {
        bridge(neo, DtEvents.DEATH, LivingDeathEvent.class, e -> new LivingEvents.Death(e.getEntity(), e.getSource()));
        bridge(neo, DtEvents.INCOMING_DAMAGE, LivingIncomingDamageEvent.class,
            e -> new LivingEvents.IncomingDamage(e.getEntity(), e.getSource(), e.getAmount()),
            (e, c) -> { if (c.getAmount() != e.getAmount()) e.setAmount(c.getAmount()); });
        bridge(neo, DtEvents.DAMAGE_POST, LivingDamageEvent.Post.class,
            e -> new LivingEvents.DamagePost(e.getEntity(), e.getSource(), e.getHealthDamage()));
        bridge(neo, DtEvents.USE_TOTEM, LivingUseTotemEvent.class, e -> new LivingEvents.UseTotem(e.getEntity()));
        bridge(neo, DtEvents.EFFECT_ADDED, MobEffectEvent.Added.class,
            e -> new LivingEvents.EffectAdded(e.getEntity(), e.getEffectInstance(), e.getOldEffectInstance()));
        bridge(neo, DtEvents.EFFECT_REMOVE, MobEffectEvent.Remove.class,
            e -> new LivingEvents.EffectRemove(e.getEntity(), e.getEffect(), e.getEffectInstance()));
        bridge(neo, DtEvents.EFFECT_EXPIRED, MobEffectEvent.Expired.class,
            e -> new LivingEvents.EffectExpired(e.getEntity(), e.getEffectInstance()));
        bridge(neo, DtEvents.BREATHE, LivingBreatheEvent.class,
            e -> new LivingEvents.Breathe(e.getEntity(), e.canBreathe()), (e, c) -> e.setCanBreathe(c.canBreathe()));
        bridge(neo, DtEvents.FALL, LivingFallEvent.class,
            e -> new LivingEvents.Fall(e.getEntity(), e.getDistance()), (e, c) -> e.setDistance(c.getDistance()));
        bridge(neo, DtEvents.JUMP, LivingEvent.LivingJumpEvent.class, e -> new LivingEvents.Jump(e.getEntity()));
        bridge(neo, DtEvents.CHANGE_TARGET, LivingChangeTargetEvent.class,
            e -> new LivingEvents.ChangeTarget(e.getEntity(), e.getNewAboutToBeSetTarget()));
        bridge(neo, DtEvents.USE_ITEM_START, LivingEntityUseItemEvent.Start.class,
            e -> new LivingEvents.UseItemStart(e.getEntity(), e.getItem()));
        bridge(neo, DtEvents.USE_ITEM_FINISH, LivingEntityUseItemEvent.Finish.class,
            e -> new LivingEvents.UseItemFinish(e.getEntity(), e.getItem()));
        bridge(neo, DtEvents.USE_ITEM_STOP, LivingEntityUseItemEvent.Stop.class,
            e -> new LivingEvents.UseItemStop(e.getEntity(), e.getItem()));
        bridge(neo, DtEvents.SIZE, EntityEvent.Size.class,
            e -> new LivingEvents.Size(e.getEntity(), e.getPose(), e.getNewSize()),
            (e, c) -> { if (c.getNewSize() != e.getNewSize()) e.setNewSize(c.getNewSize()); });
        bridge(neo, DtEvents.JOIN_LEVEL, EntityJoinLevelEvent.class, e -> new LivingEvents.JoinLevel(e.getEntity(), e.getLevel()));
        bridge(neo, DtEvents.LEAVE_LEVEL, EntityLeaveLevelEvent.class, e -> new LivingEvents.LeaveLevel(e.getEntity(), e.getLevel()));
    }

    private static void player(IEventBus neo) {
        bridge(neo, DtEvents.PLAYER_TICK_PRE, PlayerTickEvent.Pre.class, e -> new PlayerEvents.TickPre(e.getEntity()));
        bridge(neo, DtEvents.PLAYER_TICK_POST, PlayerTickEvent.Post.class, e -> new PlayerEvents.TickPost(e.getEntity()));
        bridge(neo, DtEvents.LOGGED_IN, PlayerEvent.PlayerLoggedInEvent.class, e -> new PlayerEvents.LoggedIn(e.getEntity()));
        bridge(neo, DtEvents.LOGGED_OUT, PlayerEvent.PlayerLoggedOutEvent.class, e -> new PlayerEvents.LoggedOut(e.getEntity()));
        bridge(neo, DtEvents.RESPAWN, PlayerEvent.PlayerRespawnEvent.class, e -> new PlayerEvents.Respawn(e.getEntity()));
        bridge(neo, DtEvents.CLONE, PlayerEvent.Clone.class,
            e -> new PlayerEvents.Clone(e.getEntity(), e.getOriginal(), e.isWasDeath()));
        bridge(neo, DtEvents.START_TRACKING, PlayerEvent.StartTracking.class,
            e -> new PlayerEvents.StartTracking(e.getEntity(), e.getTarget()));
        bridge(neo, DtEvents.ATTACK_ENTITY, AttackEntityEvent.class, e -> new PlayerEvents.AttackEntity(e.getEntity(), e.getTarget()));
        bridge(neo, DtEvents.LEFT_CLICK_BLOCK, PlayerInteractEvent.LeftClickBlock.class,
            e -> new PlayerEvents.LeftClickBlock(e.getEntity(), e.getPos(), e.getFace()));
        bridge(neo, DtEvents.RIGHT_CLICK_BLOCK, PlayerInteractEvent.RightClickBlock.class,
            e -> new PlayerEvents.RightClickBlock(e.getEntity(), e.getHand(), e.getHitVec()),
            (e, c) -> {
                if (!c.useItem()) e.setUseItem(TriState.FALSE);
                if (c.isCanceled()) e.setCancellationResult(c.getCancellationResult());
            });
        bridge(neo, DtEvents.RIGHT_CLICK_ITEM, PlayerInteractEvent.RightClickItem.class,
            e -> new PlayerEvents.RightClickItem(e.getEntity(), e.getHand()),
            (e, c) -> { if (c.isCanceled()) e.setCancellationResult(c.getCancellationResult()); });
        bridge(neo, DtEvents.ENTITY_INTERACT, PlayerInteractEvent.EntityInteract.class,
            e -> new PlayerEvents.EntityInteract(e.getEntity(), e.getHand(), e.getTarget()),
            (e, c) -> { if (c.isCanceled()) e.setCancellationResult(c.getCancellationResult()); });
        bridge(neo, DtEvents.CONTAINER_OPEN, PlayerContainerEvent.Open.class,
            e -> new PlayerEvents.ContainerOpen(e.getEntity(), e.getContainer()));
        bridge(neo, DtEvents.BREAK_SPEED, PlayerEvent.BreakSpeed.class, e -> new PlayerEvents.BreakSpeed(e.getEntity(), e.getState()));
        bridge(neo, DtEvents.BREAK_BLOCK, BreakBlockEvent.class,
            e -> new PlayerEvents.BreakBlock(e.getPlayer(), e.getPos(), e.getState()));
        bridge(neo, DtEvents.ANIMAL_TAME, AnimalTameEvent.class, e -> new PlayerEvents.AnimalTame(e.getTamer(), e.getAnimal()));
        bridge(neo, DtEvents.TRADE, TradeWithVillagerEvent.class,
            e -> new PlayerEvents.Trade(e.getEntity(), e.getAbstractVillager()));
        bridge(neo, DtEvents.ADVANCEMENT_EARN, AdvancementEvent.AdvancementEarnEvent.class,
            e -> new PlayerEvents.AdvancementEarn(e.getEntity(), e.getAdvancement()));
    }
}
