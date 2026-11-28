package games.brennan.dungeontotems.util;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/** Item stack helpers 1.21 has built in. */
public final class Stacks {

    private Stacks() {}

    /** 1.21's {@code ItemStack.hashItemAndComponents}: the item and its data (NBT here), not the count. */
    public static int hash(ItemStack stack) {
        return stack.isEmpty() ? 0 : 31 * stack.getItem().hashCode() + Objects.hashCode(stack.getTag());
    }
}
