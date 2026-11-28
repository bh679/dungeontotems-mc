package games.brennan.dungeontotems.net;

import games.brennan.dungeontotems.platform.Services;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Every payload and what each side does with it. Each loader registers them through a {@link Registrar}
 * and runs the handler on the game thread. Client handlers reach the client code only inside the lambda
 * body, so a dedicated server never loads a client class.
 */
public final class TotemNetwork {

    /** Bump when a payload changes. */
    public static final String PROTOCOL_VERSION = "6";

    private TotemNetwork() {}

    /** A loader's payload registration. Handlers run on the game thread. */
    public interface Registrar {
        <T extends CustomPacketPayload> void playToClient(CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, Consumer<T> handler);

        <T extends CustomPacketPayload> void playToServer(CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, ServerPlayer> handler);
    }

    public static void register(Registrar registrar) {
        registrar.playToClient(TotemPopPayload.TYPE, TotemPopPayload.STREAM_CODEC,
            payload -> games.brennan.dungeontotems.client.ClientPayloads.onTotemPop(payload));
        registrar.playToClient(MorphSyncPayload.TYPE, MorphSyncPayload.STREAM_CODEC,
            payload -> games.brennan.dungeontotems.client.ClientPayloads.onMorphSync(payload));
        registrar.playToClient(CurseTimerPayload.TYPE, CurseTimerPayload.STREAM_CODEC,
            payload -> games.brennan.dungeontotems.client.ClientPayloads.onCurseTimer(payload));
        registrar.playToServer(FormUsePayload.TYPE, FormUsePayload.STREAM_CODEC,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.use(player));
        registrar.playToServer(FormAttackPayload.TYPE, FormAttackPayload.STREAM_CODEC,
            (payload, player) -> games.brennan.dungeontotems.morph.FormAttacks.attackAir(player));
        registrar.playToServer(FormJumpPayload.TYPE, FormJumpPayload.STREAM_CODEC,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.jump(player));
        registrar.playToClient(MorphActionPayload.TYPE, MorphActionPayload.STREAM_CODEC,
            payload -> games.brennan.dungeontotems.client.ClientPayloads.onMorphAction(payload));
        registrar.playToClient(ZombieCarriedPayload.TYPE, ZombieCarriedPayload.STREAM_CODEC,
            payload -> games.brennan.dungeontotems.client.ClientPayloads.onZombieCarried(payload));
    }

    /**
     * Sends to {@code player} and everyone who can see them, skipping any connection without our
     * channel (a vanilla client, or a GameTest mock player).
     */
    public static void sendToTrackingAndSelf(ServerPlayer player, CustomPacketPayload payload) {
        sendTo(player, payload);
        for (ServerPlayer watcher : Services.PLATFORM.trackingPlayers(player)) {
            if (watcher != player) sendTo(watcher, payload);
        }
    }

    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        if (player.connection != null && Services.PLATFORM.hasChannel(player, payload)) {
            Services.PLATFORM.sendToPlayer(player, payload);
        }
    }

    /** Client only. */
    public static void sendToServer(CustomPacketPayload payload) {
        Services.PLATFORM.sendToServer(payload);
    }
}
