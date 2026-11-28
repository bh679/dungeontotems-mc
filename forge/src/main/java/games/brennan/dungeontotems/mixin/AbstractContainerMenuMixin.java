package games.brennan.dungeontotems.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.TotemStacking;
import games.brennan.dungeontotems.curse.ZombiePack;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Shift-clicking a stackable totem into the player's inventory never merges it into a stack there: it takes
 * an empty slot or stays put. Other containers (chests) still merge. See {@link TotemStacking}.
 * Nor does anything merge into a Zombified player's locked pack that they didn't carry when they turned
 * (this merge pass doesn't ask {@code Slot#mayPlace}; see {@link ZombiePack}).
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {

    @WrapOperation(method = "moveItemStackTo", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameTags(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean dungeontotems$noMergeIntoInventory(ItemStack moving, ItemStack inSlot, Operation<Boolean> original,
                                                       @Local Slot slot) {
        if (slot.container instanceof Inventory && TotemStacking.keptApart(moving)) {
            return false;
        }
        if (ZombiePack.refuses(slot, moving)) {
            return false;
        }
        return original.call(moving, inSlot);
    }
}
