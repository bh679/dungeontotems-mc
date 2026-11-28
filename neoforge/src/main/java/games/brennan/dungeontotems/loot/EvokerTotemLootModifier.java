package games.brennan.dungeontotems.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import games.brennan.dungeontotems.DungeonTotems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Swaps each Totem of Undying in a loot drop for a random item from {@link EvokerTotems#EVOKER_TOTEMS}, keeping
 * the count. {@code data/dungeontotems/loot_modifiers/evoker_totems.json} limits it to
 * {@code minecraft:entities/evoker}, so the evoker's emeralds are untouched. An empty tag leaves the
 * vanilla totem in place.
 */
public final class EvokerTotemLootModifier extends LootModifier {

    public static final MapCodec<EvokerTotemLootModifier> CODEC = RecordCodecBuilder.mapCodec(
        inst -> codecStart(inst).apply(inst, EvokerTotemLootModifier::new));

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DungeonTotems.MOD_ID);

    public static final Supplier<MapCodec<EvokerTotemLootModifier>> SERIALIZER =
        SERIALIZERS.register("evoker_totems", () -> CODEC);

    public EvokerTotemLootModifier(Optional<Holder<LootItemCondition>> condition, int priority) {
        super(condition, priority);
    }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        return new ObjectArrayList<>(EvokerTotems.swap(generatedLoot, context.getRandom()));
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
