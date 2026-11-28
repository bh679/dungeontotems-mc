package games.brennan.dungeontotems.event;

/** The client's common events; registered by {@link games.brennan.dungeontotems.client.ClientListeners}. */
public final class DtClientEvents {

    private DtClientEvents() {}

    public static final DtBus<ClientEvents.Tick> TICK = new DtBus<>();
    public static final DtBus<ClientEvents.ScreenForeground> SCREEN_FOREGROUND = new DtBus<>();
    public static final DtBus<ClientEvents.InteractionKey> INTERACTION_KEY = new DtBus<>();
    public static final DtBus<ClientEvents.RenderPlayerPre> RENDER_PLAYER = new DtBus<>();
    public static final DtBus<ClientEvents.RenderHand> RENDER_HAND = new DtBus<>();
    public static final DtBus<ClientEvents.RenderArm> RENDER_ARM = new DtBus<>();
    public static final DtBus<ClientEvents.CharTyped> CHAR_TYPED = new DtBus<>();
    public static final DtBus<ClientEvents.ChatSend> CHAT_SEND = new DtBus<>();
    public static final DtBus<ClientEvents.ChatReceived> CHAT_RECEIVED = new DtBus<>();
    public static final DtBus<ClientEvents.ItemTooltip> ITEM_TOOLTIP = new DtBus<>();
    public static final DtBus<ClientEvents.EffectTooltip> EFFECT_TOOLTIP = new DtBus<>();
}
