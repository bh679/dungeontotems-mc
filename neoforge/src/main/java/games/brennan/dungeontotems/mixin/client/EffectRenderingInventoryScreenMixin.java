package games.brennan.dungeontotems.mixin.client;

import games.brennan.dungeontotems.client.DrawnEffectRows;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records where the inventory's effect list is really drawn ({@link DrawnEffectRows}), so the curse hints in
 * {@code CurseTooltips} hit-test the drawn rows rather than vanilla's default spot. Read-only: the y each row
 * is drawn at passes through unchanged, which also keeps this compatible with mods that move the list.
 */
@Mixin(EffectRenderingInventoryScreen.class)
public class EffectRenderingInventoryScreenMixin {

    @Inject(method = "renderBackgrounds", at = @At("HEAD"))
    private void dungeontotems$beginRows(GuiGraphicsExtractor graphics, int renderX, int yOffset,
                                         Iterable<MobEffectInstance> effects, boolean wide, CallbackInfo ci) {
        DrawnEffectRows.begin(renderX, wide);
    }

    @ModifyArg(method = "renderBackgrounds",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V"),
               index = 2)
    private int dungeontotems$recordRow(int y) {
        DrawnEffectRows.add(y);
        return y;
    }
}
