package games.brennan.dungeontotems.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.brennan.dungeontotems.DungeonTotems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Forge's side of {@link EvokerTotems}: swaps each Totem of Undying in a loot drop for a random item from its tag, keeping
 * the count. {@code data/dungeontotems/loot_modifiers/evoker_totems.json} limits it to
 * {@code minecraft:entities/evoker}, so the evoker's emeralds are untouched. An empty tag leaves the
 * vanilla totem in place.
 */
public final class EvokerTotemLootModifier extends LootModifier {

    public static final Codec<EvokerTotemLootModifier> CODEC = RecordCodecBuilder.create(
        inst -> codecStart(inst).apply(inst, EvokerTotemLootModifier::new));

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DungeonTotems.MOD_ID);

    public static final Supplier<Codec<EvokerTotemLootModifier>> SERIALIZER =
        SERIALIZERS.register("evoker_totems", () -> CODEC);

    public EvokerTotemLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return new ObjectArrayList<>(EvokerTotems.swapAll(generatedLoot, context.getRandom()));
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
