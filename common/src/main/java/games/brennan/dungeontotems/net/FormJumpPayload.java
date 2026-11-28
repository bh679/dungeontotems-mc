package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Client to server: a morphed player whose form jumps its own way (a camel dashes) pressed jump. */
public record FormJumpPayload() implements TotemPayload {

    public static final FormJumpPayload INSTANCE = new FormJumpPayload();

    public static FormJumpPayload read(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
}
