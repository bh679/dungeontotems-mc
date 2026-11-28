package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.effect.HeartbreakEffect;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hover text for curses in the inventory's effect list: hearts Heartbreak takes, how to cure Zombified,
 * each rule, and how long Rage or Lonely give you. 26.x asks for a tooltip on every row, wide or narrow, so
 * NeoForge's tooltip event is all it takes (main's hit-testing of the wide list is gone on this line).
 */
public final class CurseTooltips {

    private CurseTooltips() {}

    public static void onEffectTooltip(ClientEvents.EffectTooltip event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) event.getTooltip().addAll(lines(event.getEffectInstance(), player));
    }

    /** The extra lines for a curse, or none for any other effect. */
    static List<Component> lines(MobEffectInstance effect, Player player) {
        List<Component> lines = new ArrayList<>();
        if (effect.is(TotemRegistries.HEARTBREAK)) {
            lines.add(Component.translatable("effect.dungeontotems.heartbreak.hearts", heartsTaken(effect, player)).withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.ZOMBIFIED)) {
            lines.add(Component.translatable("effect.dungeontotems.zombified.cure").withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.RAGE)) {
            lines.add(timer("rage", ClientPayloads.secondsLeft(Curse.RAGE)));
        } else if (effect.is(TotemRegistries.BREATHLESS) || effect.is(TotemRegistries.INSOMNIA)
            || effect.is(TotemRegistries.VOW_OF_PEACE) || effect.is(TotemRegistries.MELON_DIET) || effect.is(TotemRegistries.RESTRAINED)) {
            String id = effect.getEffect().unwrapKey().map(key -> key.identifier().getPath()).orElse("");
            lines.add(Component.translatable("effect.dungeontotems." + id + ".rule").withStyle(ChatFormatting.GRAY));
        } else if (effect.is(TotemRegistries.LONELY)) {
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
