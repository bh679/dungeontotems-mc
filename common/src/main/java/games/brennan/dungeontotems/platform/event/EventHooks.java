package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Shortcuts that fire an event as the game would, for the GameTests and self-tests. */
public final class EventHooks {

    private EventHooks() {}

    /** As {@code completeUsingItem} does after the item did its thing. Returns the result stack. */
    public static ItemStack onItemUseFinish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
        LivingEntityUseItemEvent.Finish event = new LivingEntityUseItemEvent.Finish(entity, item, duration, result);
        TotemEvents.post(event);
        return event.getResultStack();
    }

    /** Whether the tame was refused. */
    public static boolean onAnimalTame(Mob animal, Player tamer) {
        return TotemEvents.post(new AnimalTameEvent(animal, tamer));
    }
}
