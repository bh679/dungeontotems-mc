package games.brennan.dungeontotems;

import com.mojang.logging.LogUtils;
import games.brennan.dungeontotems.loot.EvokerTotemLootModifier;
import games.brennan.dungeontotems.morph.MorphFormTable;
import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * Dungeon Totems: Totems of Undying that save you like the vanilla totem, then leave a curse.
 *
 * <p>Each totem is a {@link Curse}. The pop itself is {@link TotemPop}, triggered by
 * {@link TotemTriggerEvents}; the two body-changing curses share the morph engine in
 * {@code morph/}; the Void Totem's landing spot comes from {@link games.brennan.dungeontotems.api.DungeonTotemsApi}.</p>
 */
@Mod(DungeonTotems.MOD_ID)
public final class DungeonTotems {

    public static final String MOD_ID = "dungeontotems";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DungeonTotems() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        TotemRegistries.register(modBus);
        EvokerTotemLootModifier.register(modBus);
        TotemNetwork.register();
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(MorphFormTable.INSTANCE));
        LOGGER.info("Dungeon Totems loaded");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
