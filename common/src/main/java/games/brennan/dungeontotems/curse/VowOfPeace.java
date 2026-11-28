package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.server.level.ServerPlayer;

/**
 * The Vow of Peace: hurt anything and it costs you. Any damage the game credits to you counts: hits,
 * projectiles, thrown potions, TNT you lit. Holding the Totem of Pacifism, that totem pops; after it has
 * saved you, you drop to half a heart on your next tick ({@link SavedCurseShield}).
 */
public final class VowOfPeace {

    private VowOfPeace() {}

    public static void onDamage(LivingEvents.DamagePost event) {
        if (event.getHealthDamage() <= 0.0F) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker) || attacker == event.getEntity()) return;
        RuleCurses.broke(attacker, Curse.PACIFISM, TotemRegistries.BROKEN_VOW);
    }
}
