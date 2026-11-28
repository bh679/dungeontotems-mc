package games.brennan.dungeontotems;

import com.mojang.logging.LogUtils;
import games.brennan.dungeontotems.platform.event.CommonEvents;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Dungeon Totems: Totems of Undying that save you like the vanilla totem, then leave a curse.
 *
 * <p>Each totem is a {@link Curse}. The pop itself is {@link TotemPop}, triggered by
 * {@link TotemTriggerEvents}; the two body-changing curses share the morph engine in
 * {@code morph/}; the Void Totem's landing spot comes from {@link games.brennan.dungeontotems.api.DungeonTotemsApi}.</p>
 *
 * <p>The loaders' entry points ({@code DungeonTotemsForge}, {@code DungeonTotemsFabric}) call {@link #init}.</p>
 */
public final class DungeonTotems {

    public static final String MOD_ID = "dungeontotems";
    public static final Logger LOGGER = LogUtils.getLogger();

    private DungeonTotems() {}

    /** Registers the entries and the event listeners. {@code client}: a physical client. */
    public static void init(boolean client) {
        TotemRegistries.init();
        CommonEvents.register(client);
        LOGGER.info("Dungeon Totems loaded");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
