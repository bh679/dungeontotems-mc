package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.fabric.client.FabricClientHooks;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;

/** NeoForge's {@code ScreenEvent.CharacterTyped.Pre}: before a typed character reaches the open screen. */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerHooksMixin {

    @Inject(method = "charTyped", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;charTyped(Lnet/minecraft/client/input/CharacterEvent;)Z"))
    private void dungeontotems$charTyped(long handle, CharacterEvent event, CallbackInfo ci, @Local(name = "screen") Screen screen) {
        FabricClientHooks.charTyped(screen, event);
    }
}
