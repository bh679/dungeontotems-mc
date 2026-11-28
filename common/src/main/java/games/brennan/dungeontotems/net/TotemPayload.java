package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/**
 * A Dungeon Totems packet. Each one is a record with a {@code write} here and a static
 * {@code read(FriendlyByteBuf)}; the loader's network class registers them (Forge: {@code TotemNetwork}).
 */
public interface TotemPayload {
    void write(FriendlyByteBuf buf);
}
