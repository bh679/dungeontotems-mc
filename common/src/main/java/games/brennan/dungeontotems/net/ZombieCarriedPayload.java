package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Server to client: what you carried when you were Zombified, so your client agrees on what the locked pack takes. */
public record ZombieCarriedPayload(List<ItemStack> carried) implements TotemPayload {

    public static ZombieCarriedPayload read(FriendlyByteBuf buf) {
        return new ZombieCarriedPayload(
            buf.readList(FriendlyByteBuf::readItem));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(carried, FriendlyByteBuf::writeItem);
    }
}
