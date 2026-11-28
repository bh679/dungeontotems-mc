package games.brennan.dungeontotems.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The handlers for one kind of {@link DtEvent}, by {@link Priority}. As on NeoForge, a cancelled event
 * reaches no later handler.
 */
public final class DtBus<E extends DtEvent> {

    private final Map<Priority, List<Consumer<E>>> handlers = new EnumMap<>(Priority.class);

    public void register(Consumer<E> handler) {
        register(Priority.NORMAL, handler);
    }

    public synchronized void register(Priority priority, Consumer<E> handler) {
        handlers.computeIfAbsent(priority, p -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /** The priorities with handlers, first to last. */
    public synchronized Set<Priority> priorities() {
        return Set.copyOf(handlers.keySet());
    }

    /** Runs every handler, first priority to last. */
    public E post(E event) {
        for (Priority priority : Priority.values()) post(event, priority);
        return event;
    }

    /** Runs only the handlers at {@code priority} (NeoForge's bridge calls this once per priority). */
    public E post(E event, Priority priority) {
        List<Consumer<E>> at;
        synchronized (this) {
            at = handlers.get(priority);
        }
        if (at == null) return event;
        for (Consumer<E> handler : new ArrayList<>(at)) {
            if (event.isCanceled()) return event;
            handler.accept(event);
        }
        return event;
    }

    public boolean hasHandlers() {
        synchronized (this) {
            return !handlers.isEmpty();
        }
    }
}
