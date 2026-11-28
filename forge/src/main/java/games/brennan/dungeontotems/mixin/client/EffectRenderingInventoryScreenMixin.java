package games.brennan.dungeontotems.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.client.CurseTooltips;
import games.brennan.dungeontotems.client.DrawnEffectRows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Records where the inventory's effect list is really drawn ({@link DrawnEffectRows}), so the curse hints in
 * {@code CurseTooltips} hit-test the drawn rows rather than vanilla's default spot. Read-only: the y each row
 * is drawn at passes through unchanged, which also keeps this compatible with mods that move the list.
 *
 * <p>1.20.1 only: also adds the curse hints to vanilla's narrow-list effect tooltip (main does that with
 * NeoForge's {@code GatherEffectScreenTooltipsEvent}, which Forge 47 lacks).</p>
 */
@Mixin(EffectRenderingInventoryScreen.class)
public class EffectRenderingInventoryScreenMixin {

    @Inject(method = "renderBackgrounds", at = @At("HEAD"))
    private void dungeontotems$beginRows(GuiGraphics graphics, int renderX, int yOffset,
                                         Iterable<MobEffectInstance> effects, boolean wide, CallbackInfo ci) {
        DrawnEffectRows.begin(renderX, wide);
    }

    @ModifyArg(method = "renderBackgrounds",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
               index = 2)
    private int dungeontotems$recordRow(int y) {
        DrawnEffectRows.add(y);
        return y;
    }

    @ModifyArg(method = "renderEffects",
               at = @At(value = "INVOKE",
                        target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V"),
               index = 1)
    private List<Component> dungeontotems$curseHints(List<Component> lines, @Local(ordinal = 0) MobEffectInstance effect) {
        if (effect == null || Minecraft.getInstance().player == null) return lines;
        List<Component> hints = CurseTooltips.lines(effect, Minecraft.getInstance().player);
        if (hints.isEmpty()) return lines;
        List<Component> all = new ArrayList<>(lines);
        all.addAll(hints);
        return all;
    }
}
