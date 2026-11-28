package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;

/** Client to server: a morphed player attacked the air (a shot, a goat's ram, or a noise). */
public record FormAttackPayload() implements TotemPayload {

    public static final FormAttackPayload INSTANCE = new FormAttackPayload();

    public static FormAttackPayload read(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }
}
