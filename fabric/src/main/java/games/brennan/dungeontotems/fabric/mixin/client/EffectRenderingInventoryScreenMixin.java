package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.client.TimerCurseIcons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Forge's {@code renderInventoryIcon} hook: Rage and Lonely draw their timer on the inventory's effect panel,
 * under its icon. Forge passes the icon's x and the panel's top: the icon sits 7 below it.
 */
@Mixin(EffectRenderingInventoryScreen.class)
public abstract class EffectRenderingInventoryScreenMixin {

    @WrapOperation(method = "renderIcons", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(IIIIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void dungeontotems$timer(GuiGraphics graphics, int x, int y, int blitOffset, int width, int height, TextureAtlasSprite sprite,
                                     Operation<Void> original, @Local MobEffectInstance effect) {
        TimerCurseIcons.renderInventoryIcon(effect, (EffectRenderingInventoryScreen<?>) (Object) this, graphics, x, y - 7);
        original.call(graphics, x, y, blitOffset, width, height, sprite);
    }
}
