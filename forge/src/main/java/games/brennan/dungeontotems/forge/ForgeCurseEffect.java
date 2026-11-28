package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.client.TimerCurseIcons;
import games.brennan.dungeontotems.effect.CurseEffect;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.util.function.Consumer;

/** A {@link CurseEffect} that hands Forge the Rage/Lonely timer drawing ({@link TimerCurseIcons}). */
public final class ForgeCurseEffect extends CurseEffect {

    public ForgeCurseEffect(MobEffectCategory category, int color, boolean curable) {
        super(category, color, curable);
    }

    /** Client only (Forge calls it on the client). */
    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        consumer.accept(new Timed());
    }

    private static final class Timed implements IClientMobEffectExtensions {
        @Override
        public boolean renderGuiIcon(MobEffectInstance instance, Gui gui, GuiGraphics graphics, int x, int y, float z, float alpha) {
            return TimerCurseIcons.renderGuiIcon(instance, graphics, x, y);
        }

        @Override
        public boolean renderInventoryIcon(MobEffectInstance instance, EffectRenderingInventoryScreen<?> screen, GuiGraphics graphics,
                                           int x, int y, int blitOffset) {
            return TimerCurseIcons.renderInventoryIcon(instance, screen, graphics, x, y);
        }
    }
}
