package games.brennan.dungeontotems.loot;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

/**
 * An evoker's Totem of Undying becomes a random item from {@link #EVOKER_TOTEMS}, keeping the count; the
 * emeralds are untouched. An empty tag leaves the vanilla totem. Forge applies it as a global loot modifier
 * ({@code EvokerTotemLootModifier}), Fabric as a loot function on the evoker's table.
 */
public final class EvokerTotems {

    public static final TagKey<Item> EVOKER_TOTEMS =
        TagKey.create(Registries.ITEM, DungeonTotems.id("evoker_totems"));

    private EvokerTotems() {}

    /** {@code stack}, or the dungeon totem it becomes. */
    public static ItemStack swap(ItemStack stack, RandomSource random) {
        if (!stack.is(Items.TOTEM_OF_UNDYING)) return stack;
        Optional<HolderSet.Named<Item>> pool = BuiltInRegistries.ITEM.getTag(EVOKER_TOTEMS);
        if (pool.isEmpty() || pool.get().size() == 0) return stack;
        Holder<Item> pick = pool.get().getRandomElement(random).orElseThrow();
        return new ItemStack(pick, stack.getCount());
    }

    /** Every stack of a drop, swapped. */
    public static List<ItemStack> swapAll(List<ItemStack> drops, RandomSource random) {
        return drops.stream().map(stack -> swap(stack, random)).toList();
    }
}
