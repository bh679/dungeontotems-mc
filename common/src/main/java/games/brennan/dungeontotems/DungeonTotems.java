package games.brennan.dungeontotems;

import com.mojang.logging.LogUtils;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Dungeon Totems: Totems of Undying that save you like the vanilla totem, then leave a curse.
 *
 * <p>Each totem is a {@link Curse}. The pop itself is {@link TotemPop}, triggered by
 * {@link TotemTriggerEvents}; the two body-changing curses share the morph engine in
 * {@code morph/}; the Void Totem's landing spot comes from {@link games.brennan.dungeontotems.api.DungeonTotemsApi}.
 * Each loader's entry point calls {@link #init()}, then fires the common events ({@code event/}).</p>
 */
public final class DungeonTotems {

    public static final String MOD_ID = "dungeontotems";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialised;

    private DungeonTotems() {}

    /** Registers every entry and every common event handler. Once, while the loader sets the mod up. */
    public static synchronized void init() {
        if (initialised) return;
        initialised = true;
        TotemRegistries.init();
        DungeonTotemsListeners.register();
        LOGGER.info("Dungeon Totems loaded");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
