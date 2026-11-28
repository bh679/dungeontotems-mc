package games.brennan.dungeontotems.event;

/**
 * Every common game event the mod listens to. Handlers register in
 * {@link games.brennan.dungeontotems.DungeonTotemsListeners}; each loader fires these from its own hooks.
 */
public final class DtEvents {

    private DtEvents() {}

    public static final DtBus<LivingEvents.Death> DEATH = new DtBus<>();
    public static final DtBus<LivingEvents.IncomingDamage> INCOMING_DAMAGE = new DtBus<>();
    public static final DtBus<LivingEvents.DamagePost> DAMAGE_POST = new DtBus<>();
    public static final DtBus<LivingEvents.UseTotem> USE_TOTEM = new DtBus<>();
    public static final DtBus<LivingEvents.EffectAdded> EFFECT_ADDED = new DtBus<>();
    public static final DtBus<LivingEvents.EffectRemove> EFFECT_REMOVE = new DtBus<>();
    public static final DtBus<LivingEvents.EffectExpired> EFFECT_EXPIRED = new DtBus<>();
    public static final DtBus<LivingEvents.Breathe> BREATHE = new DtBus<>();
    public static final DtBus<LivingEvents.Fall> FALL = new DtBus<>();
    public static final DtBus<LivingEvents.Jump> JUMP = new DtBus<>();
    public static final DtBus<LivingEvents.ChangeTarget> CHANGE_TARGET = new DtBus<>();
    public static final DtBus<LivingEvents.UseItemStart> USE_ITEM_START = new DtBus<>();
    public static final DtBus<LivingEvents.UseItemFinish> USE_ITEM_FINISH = new DtBus<>();
    public static final DtBus<LivingEvents.UseItemStop> USE_ITEM_STOP = new DtBus<>();
    public static final DtBus<LivingEvents.Size> SIZE = new DtBus<>();
    public static final DtBus<LivingEvents.JoinLevel> JOIN_LEVEL = new DtBus<>();
    public static final DtBus<LivingEvents.LeaveLevel> LEAVE_LEVEL = new DtBus<>();

    public static final DtBus<PlayerEvents.TickPre> PLAYER_TICK_PRE = new DtBus<>();
    public static final DtBus<PlayerEvents.TickPost> PLAYER_TICK_POST = new DtBus<>();
    public static final DtBus<PlayerEvents.LoggedIn> LOGGED_IN = new DtBus<>();
    public static final DtBus<PlayerEvents.LoggedOut> LOGGED_OUT = new DtBus<>();
    public static final DtBus<PlayerEvents.Respawn> RESPAWN = new DtBus<>();
    public static final DtBus<PlayerEvents.Clone> CLONE = new DtBus<>();
    public static final DtBus<PlayerEvents.StartTracking> START_TRACKING = new DtBus<>();
    public static final DtBus<PlayerEvents.AttackEntity> ATTACK_ENTITY = new DtBus<>();
    public static final DtBus<PlayerEvents.LeftClickBlock> LEFT_CLICK_BLOCK = new DtBus<>();
    public static final DtBus<PlayerEvents.RightClickBlock> RIGHT_CLICK_BLOCK = new DtBus<>();
    public static final DtBus<PlayerEvents.RightClickItem> RIGHT_CLICK_ITEM = new DtBus<>();
    public static final DtBus<PlayerEvents.EntityInteract> ENTITY_INTERACT = new DtBus<>();
    public static final DtBus<PlayerEvents.ContainerOpen> CONTAINER_OPEN = new DtBus<>();
    public static final DtBus<PlayerEvents.BreakSpeed> BREAK_SPEED = new DtBus<>();
    public static final DtBus<PlayerEvents.BreakBlock> BREAK_BLOCK = new DtBus<>();
    public static final DtBus<PlayerEvents.AnimalTame> ANIMAL_TAME = new DtBus<>();
    public static final DtBus<PlayerEvents.Trade> TRADE = new DtBus<>();
    public static final DtBus<PlayerEvents.AdvancementEarn> ADVANCEMENT_EARN = new DtBus<>();

    public static final DtBus<ServerEvents.Started> SERVER_STARTED = new DtBus<>();
    public static final DtBus<ServerEvents.RegisterCommands> REGISTER_COMMANDS = new DtBus<>();
}
