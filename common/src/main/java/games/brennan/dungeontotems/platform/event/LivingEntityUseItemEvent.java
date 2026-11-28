package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Using an item over time (eating, drinking, drawing a bow). */
public abstract class LivingEntityUseItemEvent extends LivingEvent {
    private final ItemStack item;
    private int duration;

    protected LivingEntityUseItemEvent(LivingEntity entity, ItemStack item, int duration) {
        super(entity);
        this.item = item;
        this.duration = duration;
    }

    public ItemStack getItem() { return item; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    /** {@code startUsingItem}. Cancel to not start. */
    public static final class Start extends LivingEntityUseItemEvent {
        public Start(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }
    }

    /** {@code releaseUsingItem}: let go before finishing. */
    public static final class Stop extends LivingEntityUseItemEvent {
        public Stop(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }
    }

    /** {@code completeUsingItem}, after the item did its thing. {@link #getItem} is a copy of what was used. */
    public static final class Finish extends LivingEntityUseItemEvent {
        private ItemStack result;

        public Finish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
            super(entity, item, duration);
            this.result = result;
        }

        public ItemStack getResultStack() { return result; }
        public void setResultStack(ItemStack result) { this.result = result; }
    }
}
