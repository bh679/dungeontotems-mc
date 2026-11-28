package games.brennan.dungeontotems;

import net.minecraft.world.item.ItemStack;

/**
 * Stackable totems only stack when you stack them: a held stack adds a curse level per totem, so a bigger
 * curse is always your choice. Picking one up or shift-clicking it into your inventory puts it in a slot of
 * its own (a pickup still joins a stack when no slot is free, so it isn't left on the ground); putting one on
 * another with the cursor stacks them. See {@code mixin/InventoryMixin} and {@code mixin/AbstractContainerMenuMixin}.
 */
public final class TotemStacking {

    private TotemStacking() {
    }

    /** Whether this stack is a stackable totem, kept out of other stacks unless you put it there. */
    public static boolean keptApart(ItemStack stack) {
        return stack.getItem() instanceof TotemItem totem && totem.curse().isStackable();
    }
}
