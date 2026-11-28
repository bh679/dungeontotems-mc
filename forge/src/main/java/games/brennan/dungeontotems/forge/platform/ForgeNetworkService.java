package games.brennan.dungeontotems.forge.platform;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.net.TotemPayload;
import games.brennan.dungeontotems.platform.NetworkService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

/**
 * {@link TotemNetwork}'s packets on one Forge channel, at {@link TotemNetwork#PROTOCOL_VERSION}: a client and
 * server on different versions refuse to connect.
 */
public final class ForgeNetworkService implements NetworkService {

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(DungeonTotems.id("main"))
        .networkProtocolVersion(() -> TotemNetwork.PROTOCOL_VERSION)
        .clientAcceptedVersions(TotemNetwork.PROTOCOL_VERSION::equals)
        .serverAcceptedVersions(TotemNetwork.PROTOCOL_VERSION::equals)
        .simpleChannel();

    /** Called once from the mod's constructor. */
    public static void register() {
        int id = 0;
        for (TotemNetwork.Packet<?> packet : TotemNetwork.packets()) add(packet, id++);
    }

    private static <T extends TotemPayload> void add(TotemNetwork.Packet<T> packet, int id) {
        NetworkDirection direction = packet.direction() == TotemNetwork.Direction.TO_CLIENT
            ? NetworkDirection.PLAY_TO_CLIENT : NetworkDirection.PLAY_TO_SERVER;
        CHANNEL.messageBuilder(packet.type(), id, direction)
            .encoder(TotemPayload::write)
            .decoder(packet.read())
            .consumerMainThread((T payload, Supplier<NetworkEvent.Context> context) -> {
                ServerPlayer sender = context.get().getSender();
                if (direction == NetworkDirection.PLAY_TO_SERVER && sender == null) return;
                packet.handler().accept(payload, sender);
            })
            .add();
    }

    @Override
    public void sendTo(ServerPlayer player, TotemPayload payload) {
        // A GameTest mock player's connection has no netty channel, and Forge reads it to check ours.
        if (player.connection == null || player.connection.connection == null
            || player.connection.connection.channel() == null) return;
        if (!CHANNEL.isRemotePresent(player.connection.connection)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    @Override
    public void sendToServer(TotemPayload payload) {
        CHANNEL.sendToServer(payload);
    }
}
