package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.advancement.TotemTriggers;
import games.brennan.dungeontotems.forge.platform.ForgeNetworkService;
import games.brennan.dungeontotems.loot.EvokerTotemLootModifier;
import games.brennan.dungeontotems.morph.MorphFormTable;
import games.brennan.dungeontotems.registry.TotemAttributes;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Forge 47's entry point: the shared mod ({@link DungeonTotems#init}) plus what only Forge has. */
@Mod(DungeonTotems.MOD_ID)
public final class DungeonTotemsForge {

    public DungeonTotemsForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        boolean client = FMLEnvironment.dist.isClient();
        DungeonTotems.init(client);
        EvokerTotemLootModifier.register(modBus);
        ForgeNetworkService.register();
        ForgeEventBridge.register();
        ForgeGameTests.register(modBus);
        if (client) ForgeClientEventBridge.register();

        // CriteriaTriggers' map isn't thread-safe and mods construct in parallel: register on the main thread.
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(TotemTriggers::register));
        // Forge 47 wants condition serializers registered while recipe serializers are.
        modBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) LaunchedCondition.register();
        });
        modBus.addListener((EntityAttributeModificationEvent event) -> {
            if (!event.has(EntityType.PLAYER, TotemAttributes.jumpStrength())) event.add(EntityType.PLAYER, TotemAttributes.jumpStrength());
        });
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == CreativeModeTabs.COMBAT) TotemRegistries.addToCreativeTab(event::accept);
        });
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(MorphFormTable.INSTANCE));
    }
}
