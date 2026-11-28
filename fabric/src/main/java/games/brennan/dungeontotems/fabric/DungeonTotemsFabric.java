package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.advancement.TotemAdvancements;
import games.brennan.dungeontotems.advancement.TotemTriggers;
import games.brennan.dungeontotems.fabric.loot.SwapTotemsFunction;
import games.brennan.dungeontotems.platform.Services;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;

/** Fabric's entry point: the shared mod ({@link DungeonTotems#init}) plus what Fabric does its own way. */
public final class DungeonTotemsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FabricAttributes.stepHeight();
        DungeonTotems.init(Services.PLATFORM.isClient());
        TotemTriggers.register();
        SwapTotemsFunction.register();
        FabricNetwork.registerServerReceivers();
        FabricEvents.register();
        ProtocolCheck.register();

        // The advancement tab's load condition; Forge reads the same id from its own "conditions".
        ResourceConditions.register(DungeonTotems.id("advancements_launched"), json -> TotemAdvancements.launched());
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new MorphFormReloader());
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> TotemRegistries.addToCreativeTab(entries::accept));
    }

    static String version() {
        return FabricLoader.getInstance().getModContainer(DungeonTotems.MOD_ID)
            .map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("?");
    }
}
