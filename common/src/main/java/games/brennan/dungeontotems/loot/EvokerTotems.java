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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An evoker drops a random Dungeon Totem in place of its Totem of Undying: each Totem of Undying in its drop
 * becomes a random item from {@link #EVOKER_TOTEMS}, keeping the count. Only the evoker's loot table
 * (NeoForge: {@code loot_modifiers/evoker_totems.json}; Fabric: a loot-table drop hook). An empty tag
 * leaves the vanilla totem in place.
 */
public final class EvokerTotems {

    public static final TagKey<Item> EVOKER_TOTEMS = TagKey.create(Registries.ITEM, DungeonTotems.id("evoker_totems"));

    private EvokerTotems() {}

    /** The drop with every Totem of Undying swapped; the same list when there is nothing to swap to. */
    public static List<ItemStack> swap(List<ItemStack> drop, RandomSource random) {
        Optional<HolderSet.Named<Item>> pool = BuiltInRegistries.ITEM.get(EVOKER_TOTEMS);
        if (pool.isEmpty() || pool.get().size() == 0) return drop;
        List<ItemStack> swapped = new ArrayList<>(drop.size());
        for (ItemStack stack : drop) {
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                Holder<Item> pick = pool.get().getRandomElement(random).orElseThrow();
                swapped.add(new ItemStack(pick, stack.getCount()));
            } else {
                swapped.add(stack);
            }
        }
        return swapped;
    }
}
