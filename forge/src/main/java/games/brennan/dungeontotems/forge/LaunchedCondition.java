package games.brennan.dungeontotems.forge;

import com.google.gson.JsonObject;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.advancement.TotemAdvancements;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * {@code {"type": "dungeontotems:advancements_launched"}} in an advancement's Forge {@code conditions}: true once
 * the tab is out ({@link TotemAdvancements#launched}). Fabric reads the same id from {@code fabric:load_conditions}.
 */
public final class LaunchedCondition implements ICondition {

    static final ResourceLocation ID = DungeonTotems.id("advancements_launched");
    static final LaunchedCondition INSTANCE = new LaunchedCondition();
    static final IConditionSerializer<LaunchedCondition> SERIALIZER = new Serializer();

    private LaunchedCondition() {}

    static void register() {
        CraftingHelper.register(SERIALIZER);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return TotemAdvancements.launched();
    }

    private static final class Serializer implements IConditionSerializer<LaunchedCondition> {
        @Override
        public void write(JsonObject json, LaunchedCondition value) {}

        @Override
        public LaunchedCondition read(JsonObject json) {
            return INSTANCE;
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
