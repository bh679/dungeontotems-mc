package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Server to client: ticks left of the whole timer on your Rage or Lonely curse ({@code curse} is a Curse name), for the hover text. */
public record CurseTimerPayload(String curse, int ticksLeft, int totalTicks) implements TotemPayload {

    public static CurseTimerPayload read(FriendlyByteBuf buf) {
        return new CurseTimerPayload(
            buf.readUtf(),
            buf.readVarInt(),
            buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(curse);
        buf.writeVarInt(ticksLeft);
        buf.writeVarInt(totalTicks);
    }
}
