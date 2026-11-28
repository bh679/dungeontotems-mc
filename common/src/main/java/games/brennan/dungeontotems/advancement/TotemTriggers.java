package games.brennan.dungeontotems.advancement;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.mixin.CriteriaTriggersInvoker;

import java.util.function.Supplier;

/** The advancement triggers of the Dungeon Totems tab ({@code data/dungeontotems/advancements/}). */
public final class TotemTriggers {

    private static final KeyTrigger TOTEM_POPPED_TRIGGER = new KeyTrigger(DungeonTotems.id("totem_popped"), "curse");
    private static final KeyTrigger CURSE_SURVIVED_TRIGGER = new KeyTrigger(DungeonTotems.id("curse_survived"), "curse");
    private static final KeyTrigger BECAME_FORM_TRIGGER = new KeyTrigger(DungeonTotems.id("became_form"), "form");

    /** A dungeon totem saved you: {@code curse} is its curse id ({@code weakness}, {@code void}...). */
    public static final Supplier<KeyTrigger> TOTEM_POPPED = () -> TOTEM_POPPED_TRIGGER;
    /** You lived through a curse to its end, or lifted it ({@link games.brennan.dungeontotems.curse.CurseTrials}). */
    public static final Supplier<KeyTrigger> CURSE_SURVIVED = () -> CURSE_SURVIVED_TRIGGER;
    /** A Totem of Morphing made you {@code form} (an entity type id). */
    public static final Supplier<KeyTrigger> BECAME_FORM = () -> BECAME_FORM_TRIGGER;

    private TotemTriggers() {}

    /** Called once at startup, on the main thread: 1.20.1 has no trigger registry, only this static map. */
    public static void register() {
        CriteriaTriggersInvoker.dungeontotems$register(TOTEM_POPPED_TRIGGER);
        CriteriaTriggersInvoker.dungeontotems$register(CURSE_SURVIVED_TRIGGER);
        CriteriaTriggersInvoker.dungeontotems$register(BECAME_FORM_TRIGGER);
    }
}
