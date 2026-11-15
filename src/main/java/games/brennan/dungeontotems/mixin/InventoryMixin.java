package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.TotemStacking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A stackable totem picked up (or given, or put back) goes to a free slot rather than onto a stack of its
 * kind; with no free slot it joins the stack as vanilla does. See {@link TotemStacking}.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    public abstract int getFreeSlot();

    @Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$freeSlotFirst(ItemStack stack, CallbackInfoReturnable<Integer> result) {
        if (TotemStacking.keptApart(stack) && getFreeSlot() != -1) {
            result.setReturnValue(-1);
        }
    }
}
