package games.brennan.dungeontotems.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * 1.20.1's {@link GameTestHelper#makeMockServerPlayerInLevel}, but working under Forge 47: vanilla hands the
 * player a {@link Connection} with no netty channel, and Forge's login reads the channel's pipeline (an NPE,
 * every test failing). Here the connection sits on an {@link EmbeddedChannel}: what is sent just queues up.
 * Main (NeoForge 1.21.1) uses the vanilla helper, which works there.
 */
final class MockPlayers {

    private MockPlayers() {}

    static ServerPlayer create(GameTestHelper helper) {
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "test-mock-player")) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return true;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection); // registers the connection, which takes the channel as it goes active
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player);
        return player;
    }
}
