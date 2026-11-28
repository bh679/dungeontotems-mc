package games.brennan.dungeontotems;

import com.mojang.serialization.MapCodec;
import games.brennan.dungeontotems.advancement.TotemAdvancements;
import games.brennan.dungeontotems.client.ClientListeners;
import games.brennan.dungeontotems.gametest.DtGameTestRegistry;
import games.brennan.dungeontotems.loot.EvokerTotemLootModifier;
import games.brennan.dungeontotems.neoforge.NeoForgeClientEvents;
import games.brennan.dungeontotems.neoforge.NeoForgeEvents;
import games.brennan.dungeontotems.neoforge.NeoForgeNetwork;
import games.brennan.dungeontotems.neoforge.NeoForgePlatform;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/** The NeoForge entry point: the common mod ({@link DungeonTotems}) plus NeoForge's registers and events. */
@Mod(DungeonTotems.MOD_ID)
public final class DungeonTotemsNeoForge {

    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
        DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, DungeonTotems.MOD_ID);

    public DungeonTotemsNeoForge(IEventBus modBus, ModContainer container) {
        DungeonTotems.init();
        NeoForgePlatform.registerAll(modBus);
        CONDITIONS.register(TotemAdvancements.CONDITION_ID.getPath(), () -> AdvancementsLaunched.CODEC);
        CONDITIONS.register(modBus);
        EvokerTotemLootModifier.register(modBus);
        DtGameTestRegistry.register(modBus);
        modBus.addListener(NeoForgeNetwork::register);
        modBus.addListener((RegisterDataMapTypesEvent event) -> event.register(NeoForgePlatform.MORPH_FORMS));
        modBus.addListener(DungeonTotemsNeoForge::addToCreativeTab);
        NeoForgeEvents.register();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientListeners.register();
            NeoForgeClientEvents.register(modBus);
        }
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) return;
        for (ItemStack stack : TotemRegistries.creativeTabStacks()) event.accept(stack);
    }

    /** {@code {"type": "dungeontotems:advancements_launched"}}: true once the tab is out ({@link TotemAdvancements}). */
    public static final class AdvancementsLaunched implements ICondition {
        static final AdvancementsLaunched INSTANCE = new AdvancementsLaunched();
        static final MapCodec<AdvancementsLaunched> CODEC = MapCodec.unit(INSTANCE).stable();

        private AdvancementsLaunched() {}

        @Override
        public boolean test(IContext context) {
            return TotemAdvancements.launched();
        }

        @Override
        public MapCodec<? extends ICondition> codec() {
            return CODEC;
        }
    }
}
