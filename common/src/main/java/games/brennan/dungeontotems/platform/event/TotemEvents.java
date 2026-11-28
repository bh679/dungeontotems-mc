package games.brennan.dungeontotems.platform.event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The event bus of {@code common/}. Listeners are the static {@link SubscribeEvent} methods of the classes in
 * {@link CommonEvents}, keyed by their parameter's class. As on Forge, a listener for a class also gets its
 * subclasses' events (a {@link ClientChatReceivedEvent} listener gets {@link ClientChatReceivedEvent.System}).
 *
 * <p>Fabric posts each event once ({@link #post(Event)}: every priority in turn). Forge forwards each of its
 * own events once per priority ({@link #post(Event, EventPriority)}), so our HIGHEST and LOWEST listeners keep
 * their place among other mods' listeners.</p>
 */
public final class TotemEvents {

    private record Listener(EventPriority priority, boolean receiveCanceled, MethodHandle handle, String name) {}

    private static final Map<Class<?>, List<Listener>> LISTENERS = new ConcurrentHashMap<>();
    /** Every listener an event of this class reaches (its own and its superclasses'), in priority order. */
    private static final Map<Class<?>, List<Listener>> REACHED = new ConcurrentHashMap<>();

    private TotemEvents() {}

    /** Registers every {@link SubscribeEvent} method of {@code type}, if it loads on this side. */
    public static void register(Class<?> type, boolean client) {
        EventBusSubscriber subscriber = type.getAnnotation(EventBusSubscriber.class);
        if (subscriber != null && !Arrays.asList(subscriber.value()).contains(client ? Dist.CLIENT : Dist.DEDICATED_SERVER)) return;
        for (Method method : type.getDeclaredMethods()) {
            SubscribeEvent subscribe = method.getAnnotation(SubscribeEvent.class);
            if (subscribe == null) continue;
            if (!Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1
                    || !Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                throw new IllegalStateException("@SubscribeEvent needs a static method taking one event: " + method);
            }
            method.setAccessible(true);
            MethodHandle handle;
            try {
                handle = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Can't reach " + method, e);
            }
            add(method.getParameterTypes()[0], new Listener(subscribe.priority(), subscribe.receiveCanceled(), handle,
                type.getSimpleName() + "#" + method.getName()));
        }
    }

    private static void add(Class<?> eventType, Listener listener) {
        LISTENERS.compute(eventType, (key, old) -> {
            List<Listener> list = new ArrayList<>(old == null ? List.of() : old);
            list.add(listener);
            list.sort(Comparator.comparing(Listener::priority)); // stable: registration order within a priority
            return List.copyOf(list);
        });
        REACHED.clear();
    }

    private static List<Listener> reached(Class<?> eventType) {
        return REACHED.computeIfAbsent(eventType, type -> {
            List<Listener> all = new ArrayList<>();
            for (Class<?> c = type; c != null && Event.class.isAssignableFrom(c); c = c.getSuperclass()) {
                all.addAll(LISTENERS.getOrDefault(c, List.of()));
            }
            all.sort(Comparator.comparing(Listener::priority));
            return List.copyOf(all);
        });
    }

    /** Every listener, in priority order. Returns whether the event ended cancelled. */
    public static boolean post(Event event) {
        for (Listener listener : reached(event.getClass())) call(listener, event);
        return event.isCanceled();
    }

    /** Only the listeners of one priority. Returns whether the event ended cancelled. */
    public static boolean post(Event event, EventPriority priority) {
        for (Listener listener : reached(event.getClass())) {
            if (listener.priority() == priority) call(listener, event);
        }
        return event.isCanceled();
    }

    /** Whether anything listens for this event class at this priority (Forge forwards only those). */
    public static boolean has(Class<? extends Event> eventType, EventPriority priority) {
        return reached(eventType).stream().anyMatch(l -> l.priority() == priority);
    }

    /** Whether anything listens for this event class (lets a loader skip building an event). */
    public static boolean has(Class<? extends Event> eventType) {
        return !reached(eventType).isEmpty();
    }

    private static void call(Listener listener, Event event) {
        if (event.isCanceled() && !listener.receiveCanceled()) return;
        try {
            listener.handle().invoke(event);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("Listener " + listener.name() + " failed", e);
        }
    }
}
