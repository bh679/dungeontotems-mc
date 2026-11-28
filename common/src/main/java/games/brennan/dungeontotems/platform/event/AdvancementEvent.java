package games.brennan.dungeontotems.platform.event;

import net.minecraft.advancements.Advancement;
import net.minecraft.world.entity.player.Player;

/** Advancement events. */
public abstract class AdvancementEvent extends PlayerEvent {
    private final Advancement advancement;

    protected AdvancementEvent(Player player, Advancement advancement) {
        super(player);
        this.advancement = advancement;
    }

    public Advancement getAdvancement() { return advancement; }

    /** An advancement a player just completed. */
    public static final class AdvancementEarnEvent extends AdvancementEvent {
        public AdvancementEarnEvent(Player player, Advancement advancement) {
            super(player, advancement);
        }
    }
}
