package games.brennan.dungeontotems.neoforge;

import games.brennan.dungeontotems.net.TotemNetwork;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Registers {@link TotemNetwork}'s payloads with NeoForge; handlers run on the game thread. */
public final class NeoForgeNetwork {

    private NeoForgeNetwork() {}

    /** Mod-bus listener. */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(TotemNetwork.PROTOCOL_VERSION);
        TotemNetwork.register(new TotemNetwork.Registrar() {
            @Override
            public <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,
                    StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Consumer<T> handler) {
                registrar.playToClient(type, codec, (payload, context) -> context.enqueueWork(() -> handler.accept(payload)));
            }

            @Override
            public <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,
                    StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler) {
                registrar.playToServer(type, codec, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) handler.accept(payload, player);
                }));
            }
        });
    }
}
