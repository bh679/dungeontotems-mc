package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Server to client: player {@code entityId} now looks like {@code form} (an entity type id), or themself when empty. */
public record MorphSyncPayload(int entityId, String form) implements TotemPayload {

    public static MorphSyncPayload read(FriendlyByteBuf buf) {
        return new MorphSyncPayload(
            buf.readVarInt(),
            buf.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUtf(form);
    }
}
