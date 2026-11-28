package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.effect.HeartbreakEffect;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hover text for curses in the inventory's effect list: hearts Heartbreak takes, how to cure Zombified,
 * each rule, and how long Rage or Lonely give you. Vanilla only shows effect tooltips on the narrow (icon-only) list, so on the wide list,
 * which has no tooltip of its own, this draws one for the curse under the mouse, hit-testing the rows where
 * they were actually drawn ({@link DrawnEffectRows}).
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class CurseTooltips {

    /** Width of a wide (icon + name) effect row. */
    private static final int WIDE_PANEL = 120;

    private CurseTooltips() {}

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        DrawnEffectRows.clear();
    }

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof EffectRenderingInventoryScreen<?> screen)) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (screen.getSlotUnderMouse() != null) return; // an item tooltip is showing; never stack a hint on it
        // Hit-test the rows as they were really drawn this frame: a mod may have moved the list
        // (Edible Backpacks lifts it above or beside its right panel), so vanilla's spot can hold slots.
        List<DrawnEffectRows.Row> rows = DrawnEffectRows.rows();
        if (rows.isEmpty() || rows.get(0).width() < WIDE_PANEL) return; // compact: vanilla's tooltip plus onEffectTooltip
        List<MobEffectInstance> effects = player.getActiveEffects().stream()
            .filter(ClientHooks::shouldRenderEffect).sorted().toList();
        int index = DrawnEffectRows.hovered(rows, event.getMouseX(), event.getMouseY());
        if (index < 0 || index >= effects.size()) return;
        List<Component> lines = lines(effects.get(index), player);
        if (!lines.isEmpty()) {
            event.getGuiGraphics().renderTooltip(Minecraft.getInstance().font, lines, Optional.empty(),
                event.getMouseX(), event.getMouseY());
        }
    }

    /** The extra lines for a curse, or none for any other effect. */
    /** Also added to vanilla's narrow-list tooltip, by {@code mixin/client/EffectRenderingInventoryScreenMixin}. */
    public static List<Component> lines(MobEffectInstance effect, Player player) {
        List<Component> lines = new ArrayList<>();
        if (effect.is(TotemRegistries.HEARTBREAK.get())) {
            lines.add(Component.translatable("effect.dungeontotems.heartbreak.hearts", heartsTaken(effect, player)).withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.ZOMBIFIED.get())) {
            lines.add(Component.translatable("effect.dungeontotems.zombified.cure").withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.RAGE.get())) {
            lines.add(timer("rage", ClientPayloads.secondsLeft(Curse.RAGE)));
        } else if (effect.is(TotemRegistries.BREATHLESS.get()) || effect.is(TotemRegistries.INSOMNIA.get())
            || effect.is(TotemRegistries.VOW_OF_PEACE.get()) || effect.is(TotemRegistries.MELON_DIET.get()) || effect.is(TotemRegistries.RESTRAINED.get())) {
            String id = effect.getEffect().unwrapKey().map(key -> key.location().getPath()).orElse("");
            lines.add(Component.translatable("effect.dungeontotems." + id + ".rule").withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.LONELY.get())) {
            lines.add(timer("lonely", ClientPayloads.secondsLeft(Curse.LONELINESS)));
        }
        return lines;
    }

    /** "Hit something within 23s", or a warning once the timer has run out. */
    private static Component timer(String name, int seconds) {
        return seconds > 0
            ? Component.translatable("effect.dungeontotems." + name + ".deadline", seconds).withStyle(ChatFormatting.GRAY)
            : Component.translatable("effect.dungeontotems." + name + ".overdue").withStyle(ChatFormatting.RED);
    }

    /** Hearts Heartbreak takes right now (its last level may take less), as "2" or "9.5". */
    private static String heartsTaken(MobEffectInstance effect, Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeModifier modifier = maxHealth == null ? null : maxHealth.getModifier(HeartbreakEffect.MODIFIER_ID);
        double health = modifier != null ? -modifier.amount() : HeartbreakEffect.HEALTH_PER_LEVEL * (effect.getAmplifier() + 1);
        double hearts = health / 2.0;
        return hearts == Math.floor(hearts) ? Integer.toString((int) hearts) : Double.toString(hearts);
    }
}
