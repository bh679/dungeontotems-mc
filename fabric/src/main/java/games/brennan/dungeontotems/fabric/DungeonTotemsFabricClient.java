package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.client.ClientListeners;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * The Fabric client entry point: the common client handlers, payload receivers, and the client events from
 * Fabric API callbacks where one sits where NeoForge's event does (the rest are this mod's client mixins).
 */
public final class DungeonTotemsFabricClient implements ClientModInitializer {

    /** Runs after every other mod's tooltip lines, as the NeoForge handler does at LOWEST. */
    private static final Identifier LAST = Identifier.fromNamespaceAndPath("dungeontotems", "last");

    @Override
    public void onInitializeClient() {
        // -Dmixinaudit=true on runSelfTest: load every mixin target now, so a missed target fails at once.
        if (Boolean.getBoolean("dungeontotems.mixinaudit")) org.spongepowered.asm.mixin.MixinEnvironment.getCurrentEnvironment().audit();
        ClientListeners.register();
        for (FabricNetwork.ClientHandler<?> handler : FabricNetwork.clientHandlers()) receive(handler);
        ClientTickEvents.END_CLIENT_TICK.register(client -> DtClientEvents.TICK.post(new ClientEvents.Tick()));
        ClientSendMessageEvents.MODIFY_CHAT.register(message ->
            DtClientEvents.CHAT_SEND.post(new ClientEvents.ChatSend(message)).getMessage());
        ClientReceiveMessageEvents.MODIFY_GAME.register((message, overlay) ->
            DtClientEvents.CHAT_RECEIVED.post(new ClientEvents.ChatReceived(message, true, overlay)).getMessage());
        ItemTooltipCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST);
        ItemTooltipCallback.EVENT.register(LAST, (stack, context, flag, lines) ->
            DtClientEvents.ITEM_TOOLTIP.post(new ClientEvents.ItemTooltip(stack, lines, Minecraft.getInstance().player)));
    }

    private static <T extends CustomPacketPayload> void receive(FabricNetwork.ClientHandler<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(handler.type(), (payload, context) -> handler.handler().accept(payload));
    }
}
