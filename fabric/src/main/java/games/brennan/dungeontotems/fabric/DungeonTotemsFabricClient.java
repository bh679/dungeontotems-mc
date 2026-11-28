package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.net.TotemPayload;
import games.brennan.dungeontotems.platform.event.ClientChatEvent;
import games.brennan.dungeontotems.platform.event.EntityJoinLevelEvent;
import games.brennan.dungeontotems.platform.event.EntityLeaveLevelEvent;
import games.brennan.dungeontotems.platform.event.ItemTooltipEvent;
import games.brennan.dungeontotems.platform.event.ScreenEvent;
import games.brennan.dungeontotems.platform.event.TickEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

/** The client half: our packets' receivers and the client events Fabric API fires. */
public final class DungeonTotemsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        for (TotemNetwork.Packet<?> packet : TotemNetwork.packets()) {
            if (packet.direction() == TotemNetwork.Direction.TO_CLIENT) toClient(packet);
        }

        ClientTickEvents.START_CLIENT_TICK.register(client -> TotemEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> TotemEvents.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END)));
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> TotemEvents.post(new EntityJoinLevelEvent(entity, level)));
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> TotemEvents.post(new EntityLeaveLevelEvent(entity, level)));
        // At the end of ItemStack#getTooltipLines, as Forge's.
        ItemTooltipCallback.EVENT.register((stack, flag, lines) ->
            TotemEvents.post(new ItemTooltipEvent(stack, Minecraft.getInstance().player, lines, flag)));
        // In ClientPacketListener#sendChat, as Forge's.
        ClientSendMessageEvents.MODIFY_CHAT.register(message -> {
            ClientChatEvent event = new ClientChatEvent(message);
            TotemEvents.post(event);
            return event.getMessage();
        });
        // Around the screen's render in GameRenderer, as Forge's.
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            ScreenEvents.beforeRender(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
                TotemEvents.post(new ScreenEvent.Render.Pre(s, graphics, mouseX, mouseY, partialTick)));
            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, partialTick) ->
                TotemEvents.post(new ScreenEvent.Render.Post(s, graphics, mouseX, mouseY, partialTick)));
        });
    }

    private static <T extends TotemPayload> void toClient(TotemNetwork.Packet<T> packet) {
        Function<FriendlyByteBuf, T> read = packet.read();
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.id(packet.name()), (client, handler, buf, sender) -> {
            T payload = read.apply(buf);
            client.execute(() -> packet.handler().accept(payload, null));
        });
    }
}
