package games.brennan.dungeontotems.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.client.ZombieReading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The item name that pops up over the hotbar reads as mumbles for a Zombified player, like the tooltip
 * (main's {@code GuiMixin}: 26.x draws it in {@link Hud}).
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    // Every overload ("*"): NeoForge adds a (graphics, yShift) overload that does the drawing; vanilla (Fabric) has one.
    @WrapOperation(method = "extractSelectedItemName*", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"))
    private Component dungeontotems$zombieCantRead(ItemStack stack, Operation<Component> original) {
        Component name = original.call(stack);
        return ZombieReading.cantRead(Minecraft.getInstance().player) ? ZombieReading.name(stack, name) : name;
    }
}
