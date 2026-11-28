package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Client to server: a morphed player pressed "use" with an empty hand (a chicken lays, an armadillo rolls). */
public record FormUsePayload() implements TotemPayload {

    public static final FormUsePayload INSTANCE = new FormUsePayload();

    public static FormUsePayload read(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
}
