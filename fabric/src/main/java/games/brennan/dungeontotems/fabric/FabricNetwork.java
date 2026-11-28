package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.net.TotemPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link TotemNetwork}'s packets on Fabric: one channel per packet, {@code dungeontotems:v<protocol>/<name>}.
 * The protocol is in every id, so a client and server on different versions share no channel
 * ({@link ProtocolCheck} then turns the client away, as Forge's channel check does).
 */
public final class FabricNetwork {

    private static final Map<Class<?>, ResourceLocation> IDS = TotemNetwork.packets().stream()
        .collect(Collectors.toUnmodifiableMap(TotemNetwork.Packet::type, packet -> id(packet.name())));

    private FabricNetwork() {}

    public static ResourceLocation id(String name) {
        return DungeonTotems.id("v" + TotemNetwork.PROTOCOL_VERSION + "/" + name);
    }

    public static ResourceLocation idOf(TotemPayload payload) {
        ResourceLocation id = IDS.get(payload.getClass());
        if (id == null) throw new IllegalArgumentException("Not a TotemNetwork packet: " + payload.getClass());
        return id;
    }

    /** Server side: the packets clients send us, handled on the server thread. */
    static void registerServerReceivers() {
        for (TotemNetwork.Packet<?> packet : TotemNetwork.packets()) {
            if (packet.direction() == TotemNetwork.Direction.TO_SERVER) toServer(packet);
        }
    }

    private static <T extends TotemPayload> void toServer(TotemNetwork.Packet<T> packet) {
        Function<FriendlyByteBuf, T> read = packet.read();
        ServerPlayNetworking.registerGlobalReceiver(id(packet.name()), (server, player, handler, buf, sender) -> {
            T payload = read.apply(buf);
            server.execute(() -> packet.handler().accept(payload, player));
        });
    }
}
