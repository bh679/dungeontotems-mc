package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.client.TimerCurseIcons;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Forge's {@code renderGuiIcon} hook: Rage and Lonely draw their timer on the HUD box, after its background, under its icon. */
@Mixin(Gui.class)
public abstract class GuiEffectsMixin {

    @WrapOperation(method = "renderEffects", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void dungeontotems$timer(GuiGraphics graphics, ResourceLocation texture, int x, int y, int u, int v, int width, int height,
                                     Operation<Void> original, @Local MobEffectInstance effect) {
        original.call(graphics, texture, x, y, u, v, width, height);
        TimerCurseIcons.renderGuiIcon(effect, graphics, x, y);
    }
}
