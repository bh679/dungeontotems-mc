package games.brennan.dungeontotems.platform.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class of {@link SubscribeEvent} methods. {@link CommonEvents} lists every such class; one with
 * {@code value = Dist.CLIENT} only registers on a client.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
    String modid() default "";

    Dist[] value() default { Dist.CLIENT, Dist.DEDICATED_SERVER };
}
