package games.brennan.dungeontotems.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Server to client: a Dungeon Totem popped on {@code entityId}. Stands in for vanilla's entity
 * event 35, which always shows the vanilla totem in the pop animation.
 */
public record TotemPopPayload(int entityId, ItemStack totem) implements TotemPayload {

    public static TotemPopPayload read(FriendlyByteBuf buf) {
        return new TotemPopPayload(
            buf.readVarInt(),
            buf.readItem());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeItem(totem);
    }
}
