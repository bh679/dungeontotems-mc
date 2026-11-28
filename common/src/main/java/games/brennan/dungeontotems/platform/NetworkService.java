package games.brennan.dungeontotems.platform;

import games.brennan.dungeontotems.net.TotemPayload;
import net.minecraft.server.level.ServerPlayer;

/** Sends {@link games.brennan.dungeontotems.net.TotemNetwork}'s packets. */
public interface NetworkService {

    /** Skips a player whose client lacks our channel (a vanilla client, a GameTest mock player). */
    void sendTo(ServerPlayer player, TotemPayload payload);

    /** Client only. */
    void sendToServer(TotemPayload payload);
}
