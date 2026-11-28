package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.platform.event.ContainerScreenEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge's {@code ContainerScreenEvent.Render.Foreground}: right after the labels, still translated to the screen's corner. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void dungeontotems$foreground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        TotemEvents.post(new ContainerScreenEvent.Render.Foreground((AbstractContainerScreen<?>) (Object) this, graphics, mouseX, mouseY));
    }
}
