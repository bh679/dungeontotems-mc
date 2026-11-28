package games.brennan.dungeontotems.net;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Every packet, on one Forge channel. Handlers reach the client code only inside the lambda body, so a
 * dedicated server never loads a client class.
 */
public final class TotemNetwork {

    /** Bump on any packet change: a client and server on different versions refuse to connect. */
    private static final String PROTOCOL_VERSION = "6";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(DungeonTotems.id("main"))
        .networkProtocolVersion(() -> PROTOCOL_VERSION)
        .clientAcceptedVersions(PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(PROTOCOL_VERSION::equals)
        .simpleChannel();

    private static int nextId = 0;

    private TotemNetwork() {}

    /** Called once from {@link DungeonTotems}'s constructor. */
    public static void register() {
        toClient(TotemPopPayload.class, TotemPopPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onTotemPop(payload));
        toClient(MorphSyncPayload.class, MorphSyncPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onMorphSync(payload));
        toClient(CurseTimerPayload.class, CurseTimerPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onCurseTimer(payload));
        toServer(FormUsePayload.class, FormUsePayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.use(player));
        toServer(FormAttackPayload.class, FormAttackPayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormAttacks.attackAir(player));
        toServer(FormJumpPayload.class, FormJumpPayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.jump(player));
        toClient(MorphActionPayload.class, MorphActionPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onMorphAction(payload));
        toClient(ZombieCarriedPayload.class, ZombieCarriedPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onZombieCarried(payload));
    }

    private static <T extends TotemPayload> void toClient(Class<T> type, Function<FriendlyByteBuf, T> read,
                                                          BiConsumer<T, ServerPlayer> handler) {
        add(type, read, handler, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <T extends TotemPayload> void toServer(Class<T> type, Function<FriendlyByteBuf, T> read,
                                                          BiConsumer<T, ServerPlayer> handler) {
        add(type, read, handler, NetworkDirection.PLAY_TO_SERVER);
    }

    /** {@code handler} runs on the receiving side's main thread; its player is the sender on the server, else null. */
    private static <T extends TotemPayload> void add(Class<T> type, Function<FriendlyByteBuf, T> read,
                                                     BiConsumer<T, ServerPlayer> handler, NetworkDirection direction) {
        CHANNEL.messageBuilder(type, nextId++, direction)
            .encoder(TotemPayload::write)
            .decoder(read)
            .consumerMainThread((T payload, Supplier<NetworkEvent.Context> context) -> {
                ServerPlayer sender = context.get().getSender();
                if (direction == NetworkDirection.PLAY_TO_SERVER && sender == null) return;
                handler.accept(payload, sender);
            })
            .add();
    }

    /**
     * Sends to {@code player} and everyone who can see them, skipping any connection without our
     * channel (a vanilla client, or a GameTest mock player).
     */
    public static void sendToTrackingAndSelf(ServerPlayer player, TotemPayload payload) {
        sendTo(player, payload);
        for (ServerPlayer watcher : player.serverLevel().getChunkSource().chunkMap.getPlayers(
                player.chunkPosition(), false)) {
            if (watcher != player && watcher.distanceToSqr(player) <= trackingRangeSqr(player)) sendTo(watcher, payload);
        }
    }

    public static void sendTo(ServerPlayer player, TotemPayload payload) {
        // A GameTest mock player's connection has no netty channel, and Forge reads it to check ours.
        if (player.connection == null || player.connection.connection == null
            || player.connection.connection.channel() == null) return;
        if (!CHANNEL.isRemotePresent(player.connection.connection)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    /** Client only: sends to the server. */
    public static void sendToServer(TotemPayload payload) {
        CHANNEL.sendToServer(payload);
    }

    private static double trackingRangeSqr(ServerPlayer player) {
        double blocks = player.getType().clientTrackingRange() * 16.0;
        return blocks * blocks;
    }
}
