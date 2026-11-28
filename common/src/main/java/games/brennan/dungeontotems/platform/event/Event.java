package games.brennan.dungeontotems.platform.event;

/**
 * Base of Dungeon Totems' own events, shaped like Forge's so the gameplay code in {@code common/} reads the
 * same on every loader. Each loader fires them from its own hooks (Forge: {@code ForgeEventBridge}; Fabric:
 * callbacks and mixins), and {@link TotemEvents} hands them to the {@link SubscribeEvent} methods.
 */
public abstract class Event {

    /** Forge's three-way answer, used by {@link PlayerInteractEvent.RightClickBlock#setUseItem}. */
    public enum Result { DENY, DEFAULT, ALLOW }

    private boolean canceled;

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
