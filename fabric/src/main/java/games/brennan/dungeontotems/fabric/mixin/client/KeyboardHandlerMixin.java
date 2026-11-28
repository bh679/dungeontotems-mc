package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.platform.event.ScreenEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Forge's {@code ScreenEvent.CharacterTyped.Pre}: a typed character is asked first; cancelled, the screen never gets it. */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/events/GuiEventListener;charTyped(CI)Z"))
    private static boolean dungeontotems$charTyped(GuiEventListener listener, char codePoint, int modifiers, Operation<Boolean> original) {
        if (listener instanceof Screen screen && TotemEvents.post(new ScreenEvent.CharacterTyped.Pre(screen, codePoint, modifiers))) return true;
        return original.call(listener, codePoint, modifiers);
    }
}
