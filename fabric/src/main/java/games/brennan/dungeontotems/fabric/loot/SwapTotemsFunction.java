package games.brennan.dungeontotems.fabric.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.loot.EvokerTotems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * Fabric's side of {@link EvokerTotems}: {@code dungeontotems:swap_totems}, added to every pool of the evoker's
 * loot table (a datapack's replacement too, as Forge's global loot modifier still applies there).
 */
public final class SwapTotemsFunction extends LootItemConditionalFunction {

    private static LootItemFunctionType type;

    private SwapTotemsFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static void register() {
        type = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, DungeonTotems.id("swap_totems"),
            new LootItemFunctionType(new Serializer()));
        LootTableEvents.MODIFY.register((resources, loot, id, table, source) -> {
            if (id.equals(EntityType.EVOKER.getDefaultLootTable())) {
                table.modifyPools(pool -> pool.apply(simpleBuilder(SwapTotemsFunction::new)));
            }
        });
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        return EvokerTotems.swap(stack, context.getRandom());
    }

    @Override
    public LootItemFunctionType getType() {
        return type;
    }

    private static final class Serializer extends LootItemConditionalFunction.Serializer<SwapTotemsFunction> {
        @Override
        public SwapTotemsFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new SwapTotemsFunction(conditions);
        }
    }
}
