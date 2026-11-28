package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.platform.Services;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The mod is needed on both sides. Forge refuses a client whose channel is missing or on another protocol;
 * Fabric has no such check, so a client that hasn't opened our channel ({@link FabricNetwork}, protocol in its
 * id) {@link #GRACE_TICKS} after joining is disconnected with a message saying what to install.
 */
final class ProtocolCheck {

    /** Five seconds: a client registers its channels just after joining. */
    static final int GRACE_TICKS = 100;
    private static final ResourceLocation PROBE = FabricNetwork.id("totem_pop");
    private static final Map<UUID, Long> PENDING = new ConcurrentHashMap<>();

    private ProtocolCheck() {}

    static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (Services.PLATFORM.isGameTestServer()) return;
            if (server.isSingleplayerOwner(handler.player.getGameProfile())) return; // our own client
            PENDING.put(handler.player.getUUID(), server.getTickCount() + (long) GRACE_TICKS);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> PENDING.remove(handler.player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(ProtocolCheck::tick);
    }

    private static void tick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;
        long now = server.getTickCount();
        PENDING.entrySet().removeIf(entry -> {
            if (entry.getValue() > now) return false;
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null && !ServerPlayNetworking.canSend(player, PROBE)) {
                // Literal: a client without the mod has none of our translations.
                player.connection.disconnect(Component.literal("This server needs Dungeon Totems "
                    + DungeonTotemsFabric.version() + " on your client (Fabric, Minecraft 1.20.1)."));
            }
            return true;
        });
    }
}
