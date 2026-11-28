package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's {@code ScreenEvent.Render.Foreground} for recipe-book screens (the inventory, crafting, furnaces). */
@Mixin(AbstractRecipeBookScreen.class)
public abstract class RecipeBookScreenForegroundMixin {

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen;extractCarriedItem(Lnet/minecraft/client/gui/GuiGraphicsExtractor;II)V"))
    private void dungeontotems$foreground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
        DtClientEvents.SCREEN_FOREGROUND.post(new ClientEvents.ScreenForeground((Screen) (Object) this, graphics));
    }
}
