package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.client.TimerCurseIcons;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The inventory's effect list, as NeoForge has it: every row, wide or narrow, asks for a tooltip
 * ({@code GatherEffectScreenTooltipsEvent}: a curse's extra lines), and Rage's and Lonely's panels show
 * their timer ({@link TimerCurseIcons}).
 */
@Mixin(EffectsInInventory.class)
public abstract class EffectsInInventoryHooksMixin {

    @Shadow @Final private AbstractContainerScreen<?> screen;

    @WrapOperation(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/EffectsInInventory;extractText(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/Font;IIIIII)V"))
    private void dungeontotems$tooltip(EffectsInInventory self, GuiGraphicsExtractor graphics, Component name, Component duration, Font font,
                                       int x0, int y0, int textureWidth, int yStep, int mouseX, int mouseY, Operation<Void> original,
                                       @Local(name = "effect") MobEffectInstance effect) {
        original.call(self, graphics, name, duration, font, x0, y0, textureWidth, yStep, mouseX, mouseY);
        if (mouseX < x0 || mouseX > x0 + textureWidth || mouseY < y0 || mouseY > y0 + yStep) return;
        int maxTextWidth = textureWidth - 32 - 7;
        boolean compact = maxTextWidth <= 0 || font.width(name) > maxTextWidth;
        List<Component> lines = new ArrayList<>(compact ? List.of(name, duration) : List.of());
        int before = lines.size();
        DtClientEvents.EFFECT_TOOLTIP.post(new ClientEvents.EffectTooltip(effect, lines));
        if (lines.size() == before || lines.isEmpty()) return; // vanilla's own tooltip (or none) stands
        graphics.setTooltipForNextFrame(font, lines, Optional.empty(), mouseX, mouseY);
    }

    @Inject(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;getMobEffectSprite(Lnet/minecraft/core/Holder;)Lnet/minecraft/resources/Identifier;"))
    private void dungeontotems$timer(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics, @Local(name = "effect") MobEffectInstance effect,
                                     @Local(name = "x0", argsOnly = true) int x0, @Local(name = "y0") int y0) {
        TimerCurseIcons.extractInventoryIcon(effect, this.screen, graphics, x0 + 7, y0 + 7);
    }
}
