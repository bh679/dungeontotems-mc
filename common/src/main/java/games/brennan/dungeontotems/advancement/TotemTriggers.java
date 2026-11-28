package games.brennan.dungeontotems.advancement;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import games.brennan.dungeontotems.platform.Services;

import java.util.function.Supplier;

/** The advancement triggers of the Dungeon Totems tab ({@code data/dungeontotems/advancement/}). */
public final class TotemTriggers {

    /** A dungeon totem saved you: {@code curse} is its curse id ({@code weakness}, {@code void}...). */
    public static final Supplier<KeyTrigger> TOTEM_POPPED = Services.PLATFORM.registerTrigger("totem_popped", () -> new KeyTrigger("curse"));
    /** You lived through a curse to its end, or lifted it ({@link games.brennan.dungeontotems.curse.CurseTrials}). */
    public static final Supplier<KeyTrigger> CURSE_SURVIVED = Services.PLATFORM.registerTrigger("curse_survived", () -> new KeyTrigger("curse"));
    /** A Totem of Morphing made you {@code form} (an entity type id). */
    public static final Supplier<KeyTrigger> BECAME_FORM = Services.PLATFORM.registerTrigger("became_form", () -> new KeyTrigger("form"));

    private TotemTriggers() {}

    /** Loads this class, registering the triggers above. */
    public static void init() {
        // static fields do the work
    }
}
