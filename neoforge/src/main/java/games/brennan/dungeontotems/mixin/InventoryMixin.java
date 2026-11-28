package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.TotemStacking;
import games.brennan.dungeontotems.curse.ZombiePack;
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
 *
 * <p>A Zombified player's pickups only go to the hotbar and offhand, never their locked pack
 * ({@link ZombiePack}).</p>
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    public abstract int getFreeSlot();

    @Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$freeSlotFirst(ItemStack stack, CallbackInfoReturnable<Integer> result) {
        if (TotemStacking.keptApart(stack) && getFreeSlot() != -1) {
            result.setReturnValue(-1);
        } else if (ZombiePack.isLocked(((Inventory) (Object) this).player)) {
            result.setReturnValue(ZombiePack.slotWithRemainingSpace((Inventory) (Object) this, stack));
        }
    }

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$hotbarOnlyWhenZombified(CallbackInfoReturnable<Integer> result) {
        if (ZombiePack.isLocked(((Inventory) (Object) this).player)) {
            result.setReturnValue(ZombiePack.freeSlot((Inventory) (Object) this));
        }
    }
}
