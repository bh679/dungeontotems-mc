package games.brennan.dungeontotems.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.client.ZombieReading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** The item name over the hotbar reads as zombie gibberish while you are Zombified ({@link ZombieReading}). */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @WrapOperation(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;I)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getHoverName()Lnet/minecraft/network/chat/Component;"))
    private Component dungeontotems$zombieCantRead(ItemStack stack, Operation<Component> original) {
        Component name = original.call(stack);
        return ZombieReading.cantRead(Minecraft.getInstance().player) ? ZombieReading.name(stack, name) : name;
    }
}
