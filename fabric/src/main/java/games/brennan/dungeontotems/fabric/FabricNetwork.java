package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.net.TotemNetwork;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Registers {@link TotemNetwork}'s payloads with Fabric: their types on both sides, the server's receivers
 * here, the client's in {@link DungeonTotemsFabricClient} (from {@link #clientHandlers()}). Fabric runs
 * play-payload handlers on the game thread.
 */
public final class FabricNetwork {

    /** A payload type and what the client does with it. */
    public record ClientHandler<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, Consumer<T> handler) {}

    private static final List<ClientHandler<?>> CLIENT = new ArrayList<>();

    private FabricNetwork() {}

    static void register() {
        TotemNetwork.register(new Registrar());
    }

    static List<ClientHandler<?>> clientHandlers() {
        return List.copyOf(CLIENT);
    }

    /** Named, not anonymous, so its generic methods read plainly in stack traces. */
    private static final class Registrar implements TotemNetwork.Registrar {
        @Override
        public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Consumer<T> handler) {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
            CLIENT.add(new ClientHandler<>(type, handler));
        }

        @Override
        public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,
                StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, context.player()));
        }
    }
}
