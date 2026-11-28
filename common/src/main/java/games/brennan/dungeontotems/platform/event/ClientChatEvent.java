package games.brennan.dungeontotems.platform.event;

/** Client: a chat message about to be sent. Change it with {@link #setMessage}. */
public final class ClientChatEvent extends Event {
    private final String originalMessage;
    private String message;

    public ClientChatEvent(String message) {
        this.originalMessage = message;
        this.message = message;
    }

    public String getOriginalMessage() { return originalMessage; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
