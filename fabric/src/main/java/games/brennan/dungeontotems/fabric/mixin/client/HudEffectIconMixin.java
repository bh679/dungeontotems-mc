package games.brennan.dungeontotems.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import games.brennan.dungeontotems.client.TimerCurseIcons;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Rage's and Lonely's timer on their HUD effect box ({@link TimerCurseIcons}), where NeoForge's icon hook is. */
@Mixin(Hud.class)
public abstract class HudEffectIconMixin {

    @Inject(method = "extractEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;getMobEffectSprite(Lnet/minecraft/core/Holder;)Lnet/minecraft/resources/Identifier;"))
    private void dungeontotems$timer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci,
                                     @Local(name = "instance") MobEffectInstance instance, @Local(name = "x") int x, @Local(name = "y") int y) {
        TimerCurseIcons.extractHudIcon(instance, graphics, x + 3, y + 3);
    }
}
