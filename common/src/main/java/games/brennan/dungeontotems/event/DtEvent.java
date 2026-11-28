package games.brennan.dungeontotems.event;

/**
 * A game event as the mod's own code sees it, the same on every loader. Each loader fires these from its
 * own hooks (NeoForge events, Fabric callbacks and mixins) and copies the result back. The getters
 * mirror NeoForge's events of the same name, so a handler reads the same on either loader.
 */
public abstract class DtEvent {

    private boolean canceled;

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
