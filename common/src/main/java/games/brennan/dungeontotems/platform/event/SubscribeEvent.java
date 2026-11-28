package games.brennan.dungeontotems.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A static method taking one event, called by {@link TotemEvents} in priority order. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
    EventPriority priority() default EventPriority.NORMAL;

    /** Also called once an earlier listener cancelled the event. */
    boolean receiveCanceled() default false;
}
