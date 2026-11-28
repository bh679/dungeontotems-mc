package games.brennan.dungeontotems.advancement;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.resources.ResourceLocation;

/**
 * Whether the advancement tab is out yet. Until it is, every file in {@code data/dungeontotems/advancements/}
 * fails its load condition ({@code dungeontotems:advancements_launched}), so the game never loads it: no
 * tab, no toasts, nothing to grant, nothing for another mod's capstone to count. Triggers still fire into
 * nothing. Launching is a release that sets {@link #LAUNCHED}.
 */
public final class TotemAdvancements {

    /** Set in a release to turn the tab on. */
    public static final boolean LAUNCHED = false;
    /** Dev runs only ({@code build.gradle}): load the tab before launch, for testing. */
    static final String DEV_PROPERTY = "dungeontotems.advancements";

    private TotemAdvancements() {}

    public static boolean launched() {
        return isLaunched(LAUNCHED, System.getProperty(DEV_PROPERTY));
    }

    /** Launched, or loaded early by a dev run's property. */
    public static boolean isLaunched(boolean launched, String devProperty) {
        return launched || Boolean.parseBoolean(devProperty);
    }
}
