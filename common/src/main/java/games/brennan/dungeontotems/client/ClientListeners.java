package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.event.DtClientEvents;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.Priority;

/** Every client-side event handler. Each loader's client entry point calls {@link #register()} once. */
public final class ClientListeners {

    private static boolean registered;

    private ClientListeners() {}

    public static synchronized void register() {
        if (registered) return;
        registered = true;
        DtClientEvents.CHAR_TYPED.register(ZombieWriting::onCharTyped);
        DtClientEvents.CHAT_RECEIVED.register(Priority.LOWEST, ZombieReading::onChat);
        DtClientEvents.CHAT_SEND.register(ZombieWriting::onChatSent);
        DtClientEvents.EFFECT_TOOLTIP.register(CurseTooltips::onEffectTooltip);
        DtClientEvents.INTERACTION_KEY.register(FormInput::onAttackKey);
        DtClientEvents.INTERACTION_KEY.register(FormInput::onUseKey);
        DtClientEvents.ITEM_TOOLTIP.register(Priority.LOWEST, ZombieReading::onTooltip);
        DtEvents.PLAYER_TICK_PRE.register(FormFlop::onPlayerTick);
        DtClientEvents.RENDER_ARM.register(MorphRenderer::onRenderArm);
        DtClientEvents.RENDER_HAND.register(MorphRenderer::onRenderHand);
        DtClientEvents.RENDER_PLAYER.register(MorphRenderer::onRenderPlayer);
        DtClientEvents.SCREEN_FOREGROUND.register(ZombiePackOverlay::onForeground);
        DtClientEvents.TICK.register(AdvancementSelfTest::onTick);
        DtClientEvents.TICK.register(FormInput::onClientTick);
        DtClientEvents.TICK.register(FormSelfTest::onTick);
        DtClientEvents.TICK.register(MorphRenderer::onClientTick);
        DtClientEvents.TICK.register(PopSelfTest::onTick);
        DtClientEvents.TICK.register(StackingSelfTest::onTick);
        DtClientEvents.TICK.register(ZombieSelfTest::onTick);    }
}
