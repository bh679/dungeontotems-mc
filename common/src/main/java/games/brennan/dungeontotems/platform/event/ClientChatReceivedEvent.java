package games.brennan.dungeontotems.platform.event;

import net.minecraft.network.chat.Component;

import java.util.UUID;

/** Client: a chat line about to be shown. Change it with {@link #setMessage}. */
public class ClientChatReceivedEvent extends Event {
    private final UUID sender;
    private Component message;

    public ClientChatReceivedEvent(Component message, UUID sender) {
        this.message = message;
        this.sender = sender;
    }

    public Component getMessage() { return message; }
    public void setMessage(Component message) { this.message = message; }
    /** The player who sent it; null for the game's own messages. */
    public UUID getSender() { return sender; }
    public boolean isSystem() { return false; }

    /** The game's own message; {@link #isOverlay} if it goes to the action bar. */
    public static final class System extends ClientChatReceivedEvent {
        private final boolean overlay;

        public System(Component message, boolean overlay) {
            super(message, null);
            this.overlay = overlay;
        }

        public boolean isOverlay() { return overlay; }

        @Override
        public boolean isSystem() { return true; }
    }
}
