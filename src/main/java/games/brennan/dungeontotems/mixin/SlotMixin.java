package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.curse.ZombiePack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A Zombified player's locked pack slots, in every menu: nothing comes out, and only what they carried
 * when they turned goes in. Clicks, shift-clicks, number keys, drags and throws all ask these two.
 * See {@link ZombiePack}.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$lockedPackKeepsItems(Player player, CallbackInfoReturnable<Boolean> result) {
        if (ZombiePack.isLockedSlot((Slot) (Object) this)) result.setReturnValue(false);
    }

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void dungeontotems$lockedPackTakesOnlyWhatYouHad(ItemStack stack, CallbackInfoReturnable<Boolean> result) {
        if (ZombiePack.refuses((Slot) (Object) this, stack)) result.setReturnValue(false);
    }
}
