package games.brennan.dungeontotems.event;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

/** Server lifecycle events. */
public final class ServerEvents {

    private ServerEvents() {}

    public static final class Started extends DtEvent {
        private final MinecraftServer server;

        public Started(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static final class RegisterCommands extends DtEvent {
        private final CommandDispatcher<CommandSourceStack> dispatcher;
        private final CommandBuildContext buildContext;

        public RegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
            this.dispatcher = dispatcher;
            this.buildContext = buildContext;
        }

        public CommandDispatcher<CommandSourceStack> getDispatcher() {
            return dispatcher;
        }

        public CommandBuildContext getBuildContext() {
            return buildContext;
        }
    }
}
