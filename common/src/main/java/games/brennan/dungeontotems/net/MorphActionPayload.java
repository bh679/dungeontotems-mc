package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Server to client: morphed player {@code entityId} did {@code action} (a {@code FormAction} ordinal), for its animation. */
public record MorphActionPayload(int entityId, int action) implements TotemPayload {

    public static MorphActionPayload read(FriendlyByteBuf buf) {
        return new MorphActionPayload(
            buf.readVarInt(),
            buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(action);
    }
}
