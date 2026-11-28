package games.brennan.dungeontotems.platform.event;

import net.minecraft.server.MinecraftServer;

/** The server finished starting. */
public final class ServerStartedEvent extends Event {
    private final MinecraftServer server;

    public ServerStartedEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() { return server; }
}
