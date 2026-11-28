package games.brennan.dungeontotems.net;

import games.brennan.dungeontotems.platform.Services;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Every packet. Each loader carries them on its own channel ({@link Services#NETWORK}): Forge one
 * {@code SimpleChannel}, Fabric one id per packet. Handlers reach the client code only inside the lambda
 * body, so a dedicated server never loads a client class.
 */
public final class TotemNetwork {

    /** Bump on any packet change: a client and server on different versions refuse to connect. */
    public static final String PROTOCOL_VERSION = "6";

    /** Which way a packet goes. */
    public enum Direction { TO_CLIENT, TO_SERVER }

    /**
     * One packet: {@code name} is its id on Fabric ({@code dungeontotems:v<protocol>/<name>}); the list order is
     * its index on Forge's channel, so never reorder it without a protocol bump. {@code handler} runs on the
     * receiving side's main thread; its player is the sender on the server, else null.
     */
    public record Packet<T extends TotemPayload>(String name, Class<T> type, Function<FriendlyByteBuf, T> read,
                                                 BiConsumer<T, ServerPlayer> handler, Direction direction) {}

    private static final List<Packet<?>> PACKETS = List.of(
        toClient("totem_pop", TotemPopPayload.class, TotemPopPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onTotemPop(payload)),
        toClient("morph_sync", MorphSyncPayload.class, MorphSyncPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onMorphSync(payload)),
        toClient("curse_timer", CurseTimerPayload.class, CurseTimerPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onCurseTimer(payload)),
        toServer("form_use", FormUsePayload.class, FormUsePayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.use(player)),
        toServer("form_attack", FormAttackPayload.class, FormAttackPayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormAttacks.attackAir(player)),
        toServer("form_jump", FormJumpPayload.class, FormJumpPayload::read,
            (payload, player) -> games.brennan.dungeontotems.morph.FormUse.jump(player)),
        toClient("morph_action", MorphActionPayload.class, MorphActionPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onMorphAction(payload)),
        toClient("zombie_carried", ZombieCarriedPayload.class, ZombieCarriedPayload::read,
            (payload, player) -> games.brennan.dungeontotems.client.ClientPayloads.onZombieCarried(payload))
    );

    private TotemNetwork() {}

    public static List<Packet<?>> packets() {
        return PACKETS;
    }

    private static <T extends TotemPayload> Packet<T> toClient(String name, Class<T> type, Function<FriendlyByteBuf, T> read,
                                                               BiConsumer<T, ServerPlayer> handler) {
        return new Packet<>(name, type, read, handler, Direction.TO_CLIENT);
    }

    private static <T extends TotemPayload> Packet<T> toServer(String name, Class<T> type, Function<FriendlyByteBuf, T> read,
                                                               BiConsumer<T, ServerPlayer> handler) {
        return new Packet<>(name, type, read, handler, Direction.TO_SERVER);
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
        Services.NETWORK.sendTo(player, payload);
    }

    /** Client only: sends to the server. */
    public static void sendToServer(TotemPayload payload) {
        Services.NETWORK.sendToServer(payload);
    }

    private static double trackingRangeSqr(ServerPlayer player) {
        double blocks = player.getType().clientTrackingRange() * 16.0;
        return blocks * blocks;
    }
}
