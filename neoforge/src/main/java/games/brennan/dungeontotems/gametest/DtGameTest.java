package games.brennan.dungeontotems.gametest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code public static void (GameTestHelper)} method in one of the GameTest classes as a test.
 * 26.x dropped vanilla's {@code @GameTest}; {@link DtGameTestRegistry} registers these instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DtGameTest {
    /** Ticks before the test times out (vanilla's old default, 100). */
    int timeoutTicks() default 100;
}
