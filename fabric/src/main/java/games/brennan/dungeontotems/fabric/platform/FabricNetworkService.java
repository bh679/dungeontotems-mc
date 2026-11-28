package games.brennan.dungeontotems.fabric.platform;

import games.brennan.dungeontotems.fabric.FabricNetwork;
import games.brennan.dungeontotems.net.TotemPayload;
import games.brennan.dungeontotems.platform.NetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Sends on Fabric's per-packet channels. A joining client's channels are already known when the login event
 * fires (checked on a dedicated server: the morph and zombie-pack syncs at login go straight out), as on Forge.
 */
public final class FabricNetworkService implements NetworkService {

    @Override
    public void sendTo(ServerPlayer player, TotemPayload payload) {
        if (player.connection == null) return;
        ResourceLocation id = FabricNetwork.idOf(payload);
        if (!ServerPlayNetworking.canSend(player, id)) return; // a vanilla client, or a GameTest mock player
        ServerPlayNetworking.send(player, id, encode(payload));
    }

    @Override
    public void sendToServer(TotemPayload payload) {
        ResourceLocation id = FabricNetwork.idOf(payload);
        if (ClientPlayNetworking.canSend(id)) ClientPlayNetworking.send(id, encode(payload));
    }

    private static FriendlyByteBuf encode(TotemPayload payload) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        return buf;
    }
}
