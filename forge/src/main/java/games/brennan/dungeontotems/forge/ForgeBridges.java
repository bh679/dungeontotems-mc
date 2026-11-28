package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.platform.event.Event;
import games.brennan.dungeontotems.platform.event.EventPriority;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraftforge.common.MinecraftForge;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Forwards a Forge event into {@link TotemEvents}. One Forge listener per priority our listeners use, each
 * building our event fresh from Forge's, posting it to that priority only, then writing its changes (cancel,
 * amount...) back. So our HIGHEST and LOWEST listeners keep their place among other mods' listeners, and a
 * Forge event another mod cancelled never reaches ours, as before.
 */
final class ForgeBridges {

    private ForgeBridges() {}

    /** Call after {@code CommonEvents.register}: only priorities with listeners get a Forge listener. */
    static <F extends net.minecraftforge.eventbus.api.Event, E extends Event> void forward(
            Class<F> forgeType, Class<E> ourType, Function<F, E> in, BiConsumer<F, E> out) {
        for (EventPriority priority : EventPriority.values()) {
            if (!TotemEvents.has(ourType, priority)) continue;
            MinecraftForge.EVENT_BUS.addListener(forge(priority), false, forgeType, forgeEvent -> {
                E event = in.apply(forgeEvent);
                TotemEvents.post(event, priority);
                out.accept(forgeEvent, event);
                if (event.isCanceled() && forgeEvent.isCancelable()) forgeEvent.setCanceled(true);
            });
        }
    }

    /** No changes to write back but a cancel. */
    static <F extends net.minecraftforge.eventbus.api.Event, E extends Event> void forward(
            Class<F> forgeType, Class<E> ourType, Function<F, E> in) {
        forward(forgeType, ourType, in, (forgeEvent, event) -> {});
    }

    private static net.minecraftforge.eventbus.api.EventPriority forge(EventPriority priority) {
        return net.minecraftforge.eventbus.api.EventPriority.valueOf(priority.name());
    }

    static net.minecraftforge.eventbus.api.Event.Result forge(Event.Result result) {
        return net.minecraftforge.eventbus.api.Event.Result.valueOf(result.name());
    }

    static Event.Result ours(net.minecraftforge.eventbus.api.Event.Result result) {
        return Event.Result.valueOf(result.name());
    }
}
