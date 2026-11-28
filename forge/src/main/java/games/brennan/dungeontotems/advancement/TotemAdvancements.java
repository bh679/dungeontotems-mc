package games.brennan.dungeontotems.advancement;

import com.google.gson.JsonObject;
import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

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

    public static void register(IEventBus modBus) {
        // Forge 47 wants condition serializers registered while recipe serializers are.
        modBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) CraftingHelper.register(Launched.SERIALIZER);
        });
    }

    /** {@code {"type": "dungeontotems:advancements_launched"}}: true once the tab is out. */
    public static final class Launched implements ICondition {
        static final ResourceLocation ID = DungeonTotems.id("advancements_launched");
        static final Launched INSTANCE = new Launched();
        static final IConditionSerializer<Launched> SERIALIZER = new IConditionSerializer<>() {
            @Override
            public void write(JsonObject json, Launched value) {}

            @Override
            public Launched read(JsonObject json) {
                return INSTANCE;
            }

            @Override
            public ResourceLocation getID() {
                return ID;
            }
        };

        private Launched() {}

        @Override
        public ResourceLocation getID() {
            return ID;
        }

        @Override
        public boolean test(IContext context) {
            return launched();
        }
    }
}
