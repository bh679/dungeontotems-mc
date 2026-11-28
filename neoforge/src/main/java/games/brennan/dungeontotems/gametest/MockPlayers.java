package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.morph.Morphs;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;

import java.util.UUID;

/** GameTest helpers that 26.x's own helper no longer gives. */
final class MockPlayers {

    /** A lane mob made whatever the difficulty (26.x refuses hostile mobs on Peaceful otherwise). */
    static final net.minecraft.world.entity.EntitySpawnRequest STAND_IN = games.brennan.dungeontotems.morph.Morphs.STAND_IN;

    private MockPlayers() {}

    /**
     * A mock player in the level. 26.x's {@code makeMockServerPlayerInLevel} pins {@code gameMode()} to
     * creative, which mobs refuse to target and survival rules skip, so this is that helper without the pin.
     */
    static ServerPlayer mock(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-mock-player"), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    /** {@code /time set}: 26.x keeps the day on the overworld clock, not on the level. */
    static void setDayTime(ServerLevel level, long ticks) {
        Holder<WorldClock> clock = level.registryAccess().get(WorldClocks.OVERWORLD).orElseThrow();
        level.getServer().clockManager().setTotalTicks(clock, ticks);
    }
}
