package games.brennan.dungeontotems;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.Priority;

/**
 * Every common-side event handler, by event and priority (NeoForge's {@code @SubscribeEvent}s, listed once
 * for both loaders). Within a priority they run in this order.
 */
final class DungeonTotemsListeners {

    private DungeonTotemsListeners() {}

    static void register() {
        DtEvents.ADVANCEMENT_EARN.register(games.brennan.dungeontotems.advancement.TabComplete::onEarned);
        DtEvents.ANIMAL_TAME.register(games.brennan.dungeontotems.curse.ZombieLimits::onTame);
        DtEvents.ATTACK_ENTITY.register(games.brennan.dungeontotems.morph.FormAttacks::onAttack);
        DtEvents.BREAK_BLOCK.register(games.brennan.dungeontotems.curse.ZombieLimits::onBreak);
        DtEvents.BREAK_SPEED.register(games.brennan.dungeontotems.curse.ZombieLimits::onBreakSpeed);
        DtEvents.BREATHE.register(games.brennan.dungeontotems.morph.WaterLife::onBreathe);
        DtEvents.CHANGE_TARGET.register(games.brennan.dungeontotems.morph.FormAllies::onTarget);
        DtEvents.CHANGE_TARGET.register(games.brennan.dungeontotems.morph.MorphEvents::onTarget);
        DtEvents.CLONE.register(games.brennan.dungeontotems.curse.CurseMemory::onClone);
        DtEvents.CONTAINER_OPEN.register(games.brennan.dungeontotems.curse.ZombieLimits::onContainerOpen);
        DtEvents.DAMAGE_POST.register(games.brennan.dungeontotems.curse.CurseTimers::onHit);
        DtEvents.DAMAGE_POST.register(games.brennan.dungeontotems.curse.VowOfPeace::onDamage);
        DtEvents.DAMAGE_POST.register(games.brennan.dungeontotems.morph.FormAllies::onHurt);
        DtEvents.DAMAGE_POST.register(games.brennan.dungeontotems.morph.FormUse::onHurtAfter);
        DtEvents.DEATH.register(Priority.HIGHEST, TotemTriggerEvents::onDeath);
        DtEvents.DEATH.register(Priority.LOWEST, games.brennan.dungeontotems.curse.CurseTrials::onDeath);
        DtEvents.EFFECT_ADDED.register(games.brennan.dungeontotems.curse.CurseGuard::onAdded);
        DtEvents.EFFECT_ADDED.register(games.brennan.dungeontotems.curse.ZombiePack::onEffectAdded);
        DtEvents.EFFECT_EXPIRED.register(games.brennan.dungeontotems.curse.ZombiePack::onEffectExpired);
        DtEvents.EFFECT_EXPIRED.register(games.brennan.dungeontotems.morph.MorphEvents::onEffectExpired);
        DtEvents.EFFECT_EXPIRED.register(Priority.LOWEST, games.brennan.dungeontotems.curse.CurseGuard::onExpired);
        DtEvents.EFFECT_REMOVE.register(Priority.HIGH, games.brennan.dungeontotems.curse.CurseGuard::onRemove);
        DtEvents.EFFECT_REMOVE.register(games.brennan.dungeontotems.curse.ZombiePack::onEffectRemoved);
        DtEvents.EFFECT_REMOVE.register(games.brennan.dungeontotems.morph.MorphEvents::onEffectRemoved);
        DtEvents.EFFECT_REMOVE.register(Priority.LOWEST, games.brennan.dungeontotems.curse.CurseGuard::onRemoved);
        DtEvents.ENTITY_INTERACT.register(games.brennan.dungeontotems.curse.ZombieLimits::onFeed);
        DtEvents.ENTITY_INTERACT.register(games.brennan.dungeontotems.curse.ZombieTrade::onInteract);
        DtEvents.ENTITY_INTERACT.register(games.brennan.dungeontotems.morph.FormAllies::onInteract);
        DtEvents.FALL.register(games.brennan.dungeontotems.morph.FormTraits::onFall);
        DtEvents.INCOMING_DAMAGE.register(games.brennan.dungeontotems.morph.FormUse::onHurt);
        DtEvents.INCOMING_DAMAGE.register(Priority.LOWEST, games.brennan.dungeontotems.curse.SavedCurseShield::onIncomingDamage);
        DtEvents.JOIN_LEVEL.register(games.brennan.dungeontotems.curse.ZombieTrade::onJoin);
        DtEvents.JOIN_LEVEL.register(games.brennan.dungeontotems.morph.FormAllies::onJoin);
        DtEvents.JUMP.register(games.brennan.dungeontotems.morph.FormAir::onJump);
        DtEvents.LEAVE_LEVEL.register(games.brennan.dungeontotems.curse.LonelyGifts::onItemGone);
        DtEvents.LEFT_CLICK_BLOCK.register(games.brennan.dungeontotems.morph.FormAttacks::onHitBlock);
        DtEvents.LOGGED_IN.register(games.brennan.dungeontotems.curse.ZombiePack::onLogin);
        DtEvents.LOGGED_IN.register(games.brennan.dungeontotems.morph.MorphEvents::onLogin);
        DtEvents.LOGGED_OUT.register(games.brennan.dungeontotems.voidtotem.VoidRescue::onLogout);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.curse.Breathless::onTickEnd);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.curse.CurseTimers::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.curse.DeadlineCurses::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.curse.HeldCurses::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.curse.InsomniaPhantoms::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.FormAttacks::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.FormTimers::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.FormTraits::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.FormUse::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.MorphEvents::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.morph.WaterLife::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(games.brennan.dungeontotems.voidtotem.VoidRescue::onPlayerTick);
        DtEvents.PLAYER_TICK_POST.register(Priority.LOWEST, games.brennan.dungeontotems.curse.CurseTrials::onPlayerTick);
        DtEvents.PLAYER_TICK_PRE.register(games.brennan.dungeontotems.curse.Breathless::onTickStart);
        DtEvents.PLAYER_TICK_PRE.register(games.brennan.dungeontotems.morph.FormAllies::onPlayerTick);
        DtEvents.PLAYER_TICK_PRE.register(games.brennan.dungeontotems.morph.MorphFlight::onPlayerTick);
        DtEvents.REGISTER_COMMANDS.register(games.brennan.dungeontotems.morph.MorphCommand::onRegisterCommands);
        DtEvents.RESPAWN.register(games.brennan.dungeontotems.curse.CurseMemory::onRespawn);
        DtEvents.RESPAWN.register(games.brennan.dungeontotems.curse.ZombiePack::onRespawn);
        DtEvents.RIGHT_CLICK_BLOCK.register(games.brennan.dungeontotems.curse.RuleCurses::onRightClickBlock);
        DtEvents.RIGHT_CLICK_BLOCK.register(games.brennan.dungeontotems.curse.ZombieDiet::onRightClickBlock);
        DtEvents.RIGHT_CLICK_BLOCK.register(games.brennan.dungeontotems.curse.ZombieLimits::onRightClickBlock);
        DtEvents.RIGHT_CLICK_ITEM.register(games.brennan.dungeontotems.curse.ZombieDiet::onRightClickItem);
        DtEvents.RIGHT_CLICK_ITEM.register(games.brennan.dungeontotems.curse.ZombieLimits::onRightClickItem);
        DtEvents.SERVER_STARTED.register(games.brennan.dungeontotems.compat.PlayerMobGifts::onServerStarted);
        DtEvents.SIZE.register(games.brennan.dungeontotems.morph.MorphEvents::onSize);
        DtEvents.START_TRACKING.register(games.brennan.dungeontotems.morph.MorphEvents::onStartTracking);
        DtEvents.TRADE.register(games.brennan.dungeontotems.curse.LonelyGifts::onTrade);
        DtEvents.USE_ITEM_FINISH.register(games.brennan.dungeontotems.curse.DeadlineCurses::onFinishUsing);
        DtEvents.USE_ITEM_FINISH.register(games.brennan.dungeontotems.curse.RuleCurses::onFinishEating);
        DtEvents.USE_ITEM_FINISH.register(games.brennan.dungeontotems.curse.ZombieDiet::onFinishEating);
        DtEvents.USE_ITEM_START.register(games.brennan.dungeontotems.curse.RuleCurses::onStartEating);
        DtEvents.USE_ITEM_START.register(games.brennan.dungeontotems.curse.ZombieDiet::onStartEating);
        DtEvents.USE_ITEM_STOP.register(games.brennan.dungeontotems.curse.ZombieDiet::onStopEating);
        DtEvents.USE_TOTEM.register(Priority.LOWEST, games.brennan.dungeontotems.curse.CurseGuard::onVanillaTotem);
    }
}
