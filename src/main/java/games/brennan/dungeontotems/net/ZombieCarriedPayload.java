package games.brennan.dungeontotems.net;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Server to client: what you carried when you were Zombified, so your client agrees on what the locked pack takes. */
public record ZombieCarriedPayload(List<ItemStack> carried) implements CustomPacketPayload {

    public static final Type<ZombieCarriedPayload> TYPE = new Type<>(DungeonTotems.id("zombie_carried"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ZombieCarriedPayload> STREAM_CODEC = StreamCodec.composite(
        ItemStack.LIST_STREAM_CODEC, ZombieCarriedPayload::carried,
        ZombieCarriedPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
