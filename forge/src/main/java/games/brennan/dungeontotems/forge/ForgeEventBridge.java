package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.platform.event.AdvancementEvent;
import games.brennan.dungeontotems.platform.event.AnimalTameEvent;
import games.brennan.dungeontotems.platform.event.AttackEntityEvent;
import games.brennan.dungeontotems.platform.event.BlockEvent;
import games.brennan.dungeontotems.platform.event.EntityEvent;
import games.brennan.dungeontotems.platform.event.EntityJoinLevelEvent;
import games.brennan.dungeontotems.platform.event.EntityLeaveLevelEvent;
import games.brennan.dungeontotems.platform.event.ItemTooltipEvent;
import games.brennan.dungeontotems.platform.event.LivingBreatheEvent;
import games.brennan.dungeontotems.platform.event.LivingChangeTargetEvent;
import games.brennan.dungeontotems.platform.event.LivingDamageEvent;
import games.brennan.dungeontotems.platform.event.LivingDeathEvent;
import games.brennan.dungeontotems.platform.event.LivingEntityUseItemEvent;
import games.brennan.dungeontotems.platform.event.LivingEvent;
import games.brennan.dungeontotems.platform.event.LivingFallEvent;
import games.brennan.dungeontotems.platform.event.LivingHurtEvent;
import games.brennan.dungeontotems.platform.event.MobEffectEvent;
import games.brennan.dungeontotems.platform.event.PlayerContainerEvent;
import games.brennan.dungeontotems.platform.event.PlayerEvent;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.RegisterCommandsEvent;
import games.brennan.dungeontotems.platform.event.ServerStartedEvent;
import games.brennan.dungeontotems.platform.event.TickEvent;
import games.brennan.dungeontotems.platform.event.TradeWithVillagerEvent;

import static games.brennan.dungeontotems.forge.ForgeBridges.forward;

/**
 * Every Forge game-bus event the shared code listens to, forwarded to {@code TotemEvents} ({@link ForgeBridges}).
 * Forge's own types are written out in full: they share their simple names with ours.
 */
final class ForgeEventBridge {

    private ForgeEventBridge() {}

    static void register() {
        ticks();
        living();
        effects();
        players();
        interactions();
        misc();
    }

    private static TickEvent.Phase phase(net.minecraftforge.event.TickEvent.Phase phase) {
        return phase == net.minecraftforge.event.TickEvent.Phase.START ? TickEvent.Phase.START : TickEvent.Phase.END;
    }

    private static void ticks() {
        forward(net.minecraftforge.event.TickEvent.PlayerTickEvent.class, TickEvent.PlayerTickEvent.class,
            f -> new TickEvent.PlayerTickEvent(phase(f.phase), f.player));
        forward(net.minecraftforge.event.TickEvent.ClientTickEvent.class, TickEvent.ClientTickEvent.class,
            f -> new TickEvent.ClientTickEvent(phase(f.phase)));
    }

    private static void living() {
        forward(net.minecraftforge.event.entity.living.LivingHurtEvent.class, LivingHurtEvent.class,
            f -> new LivingHurtEvent(f.getEntity(), f.getSource(), f.getAmount()), (f, e) -> f.setAmount(e.getAmount()));
        forward(net.minecraftforge.event.entity.living.LivingDamageEvent.class, LivingDamageEvent.class,
            f -> new LivingDamageEvent(f.getEntity(), f.getSource(), f.getAmount()), (f, e) -> f.setAmount(e.getAmount()));
        forward(net.minecraftforge.event.entity.living.LivingDeathEvent.class, LivingDeathEvent.class,
            f -> new LivingDeathEvent(f.getEntity(), f.getSource()));
        forward(net.minecraftforge.event.entity.living.LivingChangeTargetEvent.class, LivingChangeTargetEvent.class,
            f -> new LivingChangeTargetEvent(f.getEntity(), f.getNewTarget()), (f, e) -> {
                if (e.getNewTarget() != f.getNewTarget()) f.setNewTarget(e.getNewTarget());
            });
        forward(net.minecraftforge.event.entity.living.LivingFallEvent.class, LivingFallEvent.class,
            f -> new LivingFallEvent(f.getEntity(), f.getDistance(), f.getDamageMultiplier()), (f, e) -> {
                f.setDistance(e.getDistance());
                f.setDamageMultiplier(e.getDamageMultiplier());
            });
        forward(net.minecraftforge.event.entity.living.LivingBreatheEvent.class, LivingBreatheEvent.class,
            f -> new LivingBreatheEvent(f.getEntity(), f.canBreathe()), (f, e) -> f.setCanBreathe(e.canBreathe()));
        forward(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent.class, LivingEvent.LivingJumpEvent.class,
            f -> new LivingEvent.LivingJumpEvent(f.getEntity()));
        forward(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start.class, LivingEntityUseItemEvent.Start.class,
            f -> new LivingEntityUseItemEvent.Start(f.getEntity(), f.getItem(), f.getDuration()), (f, e) -> f.setDuration(e.getDuration()));
        forward(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop.class, LivingEntityUseItemEvent.Stop.class,
            f -> new LivingEntityUseItemEvent.Stop(f.getEntity(), f.getItem(), f.getDuration()));
        forward(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish.class, LivingEntityUseItemEvent.Finish.class,
            f -> new LivingEntityUseItemEvent.Finish(f.getEntity(), f.getItem(), f.getDuration(), f.getResultStack()),
            (f, e) -> f.setResultStack(e.getResultStack()));
        forward(net.minecraftforge.event.entity.living.AnimalTameEvent.class, AnimalTameEvent.class,
            f -> new AnimalTameEvent(f.getAnimal(), f.getTamer()));
    }

    private static void effects() {
        forward(net.minecraftforge.event.entity.living.MobEffectEvent.Added.class, MobEffectEvent.Added.class,
            f -> new MobEffectEvent.Added(f.getEntity(), f.getOldEffectInstance(), f.getEffectInstance(), f.getEffectSource()));
        forward(net.minecraftforge.event.entity.living.MobEffectEvent.Remove.class, MobEffectEvent.Remove.class,
            f -> new MobEffectEvent.Remove(f.getEntity(), f.getEffect(), f.getEffectInstance()));
        forward(net.minecraftforge.event.entity.living.MobEffectEvent.Expired.class, MobEffectEvent.Expired.class,
            f -> new MobEffectEvent.Expired(f.getEntity(), f.getEffectInstance()));
    }

    private static void players() {
        forward(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed.class, PlayerEvent.BreakSpeed.class,
            f -> new PlayerEvent.BreakSpeed(f.getEntity(), f.getState(), f.getOriginalSpeed(), f.getPosition().orElse(null)),
            (f, e) -> f.setNewSpeed(e.getNewSpeed()));
        forward(net.minecraftforge.event.entity.player.PlayerEvent.Clone.class, PlayerEvent.Clone.class,
            f -> new PlayerEvent.Clone(f.getEntity(), f.getOriginal(), f.isWasDeath()));
        forward(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class, PlayerEvent.PlayerLoggedInEvent.class,
            f -> new PlayerEvent.PlayerLoggedInEvent(f.getEntity()));
        forward(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.class, PlayerEvent.PlayerLoggedOutEvent.class,
            f -> new PlayerEvent.PlayerLoggedOutEvent(f.getEntity()));
        forward(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent.class, PlayerEvent.PlayerRespawnEvent.class,
            f -> new PlayerEvent.PlayerRespawnEvent(f.getEntity(), f.isEndConquered()));
        forward(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking.class, PlayerEvent.StartTracking.class,
            f -> new PlayerEvent.StartTracking(f.getEntity(), f.getTarget()));
        forward(net.minecraftforge.event.entity.player.PlayerContainerEvent.Open.class, PlayerContainerEvent.Open.class,
            f -> new PlayerContainerEvent.Open(f.getEntity(), f.getContainer()));
        forward(net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent.class, AdvancementEvent.AdvancementEarnEvent.class,
            f -> new AdvancementEvent.AdvancementEarnEvent(f.getEntity(), f.getAdvancement()));
        forward(net.minecraftforge.event.entity.player.AttackEntityEvent.class, AttackEntityEvent.class,
            f -> new AttackEntityEvent(f.getEntity(), f.getTarget()));
        forward(net.minecraftforge.event.entity.player.TradeWithVillagerEvent.class, TradeWithVillagerEvent.class,
            f -> new TradeWithVillagerEvent(f.getEntity(), f.getMerchantOffer(), f.getAbstractVillager()));
        forward(net.minecraftforge.event.entity.player.ItemTooltipEvent.class, ItemTooltipEvent.class,
            f -> new ItemTooltipEvent(f.getItemStack(), f.getEntity(), f.getToolTip(), f.getFlags()));
    }

    private static void interactions() {
        forward(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock.class, PlayerInteractEvent.RightClickBlock.class,
            f -> {
                PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(f.getEntity(), f.getHand(), f.getPos(), f.getHitVec());
                e.setUseBlock(ForgeBridges.ours(f.getUseBlock()));
                e.setUseItem(ForgeBridges.ours(f.getUseItem()));
                return e;
            },
            (f, e) -> {
                f.setUseBlock(ForgeBridges.forge(e.getUseBlock()));
                f.setUseItem(ForgeBridges.forge(e.getUseItem()));
                if (e.isCanceled()) f.setCancellationResult(e.getCancellationResult());
            });
        forward(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem.class, PlayerInteractEvent.RightClickItem.class,
            f -> new PlayerInteractEvent.RightClickItem(f.getEntity(), f.getHand()),
            (f, e) -> { if (e.isCanceled()) f.setCancellationResult(e.getCancellationResult()); });
        forward(net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract.class, PlayerInteractEvent.EntityInteract.class,
            f -> new PlayerInteractEvent.EntityInteract(f.getEntity(), f.getHand(), f.getTarget()),
            (f, e) -> { if (e.isCanceled()) f.setCancellationResult(e.getCancellationResult()); });
        forward(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.class, PlayerInteractEvent.LeftClickBlock.class,
            f -> new PlayerInteractEvent.LeftClickBlock(f.getEntity(), f.getPos(), f.getFace(),
                PlayerInteractEvent.LeftClickBlock.Action.valueOf(f.getAction().name())));
        forward(net.minecraftforge.event.level.BlockEvent.BreakEvent.class, BlockEvent.BreakEvent.class,
            f -> new BlockEvent.BreakEvent(f.getLevel(), f.getPos(), f.getState(), f.getPlayer()));
    }

    private static void misc() {
        forward(net.minecraftforge.event.entity.EntityJoinLevelEvent.class, EntityJoinLevelEvent.class,
            f -> new EntityJoinLevelEvent(f.getEntity(), f.getLevel()));
        forward(net.minecraftforge.event.entity.EntityLeaveLevelEvent.class, EntityLeaveLevelEvent.class,
            f -> new EntityLeaveLevelEvent(f.getEntity(), f.getLevel()));
        forward(net.minecraftforge.event.entity.EntityEvent.Size.class, EntityEvent.Size.class,
            f -> new EntityEvent.Size(f.getEntity(), f.getPose(), f.getOldSize(), f.getNewSize()), (f, e) -> f.setNewSize(e.getNewSize()));
        forward(net.minecraftforge.event.RegisterCommandsEvent.class, RegisterCommandsEvent.class,
            f -> new RegisterCommandsEvent(f.getDispatcher(), f.getCommandSelection(), f.getBuildContext()));
        forward(net.minecraftforge.event.server.ServerStartedEvent.class, ServerStartedEvent.class,
            f -> new ServerStartedEvent(f.getServer()));
    }
}
