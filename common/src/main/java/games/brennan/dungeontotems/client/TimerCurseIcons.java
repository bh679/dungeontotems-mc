package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Rage and Lonely show their timer on the effect box's own border: it starts lit in the curse's colour
 * and drains back to grey clockwise from the top. Once the time is up, the border flashes with a glow
 * just outside it until you hit something or give a gift. Same on the HUD and in the inventory.
 * Each loader calls {@link #extractHudIcon} and {@link #extractInventoryIcon} from its effect icon hook.
 */
public final class TimerCurseIcons {

    private static final int RAGE_COLOUR = 0xFFDC2828;
    private static final int LONELY_COLOUR = 0xFFF082AA;
    /** The glow outside the box is the same colour at half strength. */
    private static final int GLOW_ALPHA = 0x80000000;
    private static final long FLASH_MILLIS = 250;
    /** HUD effect box ({@code hud/effect_background}). */
    private static final int HUD_BOX = 24;
    /** Inventory effect panels ({@code effect_background_large} / {@code _small}), and the icon's inset in each. */
    private static final int PANEL_HEIGHT = 32;
    private static final int LARGE_PANEL = 120;
    private static final int SMALL_PANEL = 32;
    /** Where 26.x hands the icon, from the box's corner. */
    private static final int HUD_ICON_INSET = 3;
    private static final int INVENTORY_ICON_INSET = 7;
    /** {@code EffectsInInventory}'s gap after a wide panel's text. */
    private static final int TEXT_MARGIN = 7;

    private TimerCurseIcons() {}

    /** The timed curse this effect is (Rage or Lonely), with its colour; null for any other effect. */
    @Nullable
    private static Timed timed(MobEffectInstance instance) {
        if (instance.is(TotemRegistries.RAGE)) return new Timed(Curse.RAGE, RAGE_COLOUR);
        if (instance.is(TotemRegistries.LONELY)) return new Timed(Curse.LONELINESS, LONELY_COLOUR);
        return null;
    }

    private record Timed(Curse curse, int colour) {}

    /**
     * Draws Rage's or Lonely's timer on the HUD effect box whose icon sits at {@code (x, y)}; vanilla draws
     * the icon on top. Nothing for other effects.
     */
    public static void extractHudIcon(MobEffectInstance instance, GuiGraphicsExtractor graphics, int x, int y) {
        Timed timed = timed(instance);
        if (timed != null) drawCountdown(graphics, x - HUD_ICON_INSET, y - HUD_ICON_INSET, HUD_BOX, HUD_BOX, timed.curse(), timed.colour());
    }

    /** The same on the inventory's effect panel whose icon sits at {@code (x, y)}: 7 in from the panel. */
    public static void extractInventoryIcon(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics, int x, int y) {
        Timed timed = timed(instance);
        if (timed == null) return;
        int panelX = x - INVENTORY_ICON_INSET;
        drawCountdown(graphics, panelX, y - INVENTORY_ICON_INSET, panelWidth(instance, screen, panelX), PANEL_HEIGHT, timed.curse(), timed.colour());
    }

    /** {@code EffectsInInventory#extractBackground}'s width: the text's, at most what fits right of the screen. */
    private static int panelWidth(MobEffectInstance instance, AbstractContainerScreen<?> screen, int panelX) {
        int available = screen.width - panelX;
        if (available < LARGE_PANEL) return SMALL_PANEL;
        Minecraft minecraft = Minecraft.getInstance();
        Font font = screen.getFont();
        MutableComponent name = instance.getEffect().value().getDisplayName().copy();
        if (instance.getAmplifier() >= 1 && instance.getAmplifier() <= 9) {
            name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (instance.getAmplifier() + 1)));
        }
        float tickRate = minecraft.level == null ? 20.0F : minecraft.level.tickRateManager().tickrate();
        Component duration = MobEffectUtil.formatDuration(instance, 1.0F, tickRate);
        int textWidth = Math.max(font.width(name), font.width(duration));
        return Math.min(available - TEXT_MARGIN, SMALL_PANEL + textWidth + TEXT_MARGIN);
    }

    /** Lights the part of the box's border still left on the timer, or flashes it all with a glow when the time is up. */
    static void drawCountdown(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Curse curse, int colour) {
        float left = ClientPayloads.fractionLeft(curse);
        List<int[]> border = border(width, height);
        if (left <= 0.0F) {
            if ((System.currentTimeMillis() / FLASH_MILLIS) % 2 == 1) return;
            int glow = (colour & 0x00FFFFFF) | GLOW_ALPHA;
            graphics.fill(x - 1, y, x, y + height, glow);
            graphics.fill(x + width, y, x + width + 1, y + height, glow);
            graphics.fill(x, y - 1, x + width, y, glow);
            graphics.fill(x, y + height, x + width, y + height + 1, glow);
            for (int[] p : border) graphics.fill(x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, colour);
            return;
        }
        int lit = Math.round(border.size() * left);
        // Drains clockwise from the top: the pixels still lit are the last ones round the ring.
        for (int i = border.size() - lit; i < border.size(); i++) {
            int[] p = border.get(i);
            graphics.fill(x + p[0], y + p[1], x + p[0] + 1, y + p[1] + 1, colour);
        }
    }

    /**
     * The box's grey border ring, one pixel in from its edge with rounded corners, in clockwise order
     * starting at the top centre.
     */
    static List<int[]> border(int width, int height) {
        List<int[]> ring = new ArrayList<>();
        for (int x = 2; x <= width - 3; x++) {
            ring.add(new int[] {x, 1});
            ring.add(new int[] {x, height - 2});
        }
        for (int y = 2; y <= height - 3; y++) {
            ring.add(new int[] {1, y});
            ring.add(new int[] {width - 2, y});
        }
        ring.add(new int[] {2, 2});
        ring.add(new int[] {width - 3, 2});
        ring.add(new int[] {2, height - 3});
        ring.add(new int[] {width - 3, height - 3});
        double cx = (width - 1) / 2.0;
        double cy = (height - 1) / 2.0;
        ring.sort(Comparator.comparingDouble(p -> (Math.atan2(p[0] - cx, cy - p[1]) + 2 * Math.PI) % (2 * Math.PI)));
        return ring;
    }
}
