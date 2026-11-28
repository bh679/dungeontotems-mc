package games.brennan.dungeontotems.platform.event;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** An effect added, removed or run out. */
public abstract class MobEffectEvent extends LivingEvent {
    private final MobEffectInstance effectInstance;

    protected MobEffectEvent(LivingEntity entity, MobEffectInstance effectInstance) {
        super(entity);
        this.effectInstance = effectInstance;
    }

    /** May be null for {@link Remove} when the entity didn't have the effect. */
    public MobEffectInstance getEffectInstance() {
        return effectInstance;
    }

    /** After an effect is added or changed ({@code onEffectAdded} / {@code onEffectUpdated}). */
    public static final class Added extends MobEffectEvent {
        private final MobEffectInstance oldEffectInstance;
        private final Entity source;

        public Added(LivingEntity entity, MobEffectInstance oldEffectInstance, MobEffectInstance newEffectInstance, Entity source) {
            super(entity, newEffectInstance);
            this.oldEffectInstance = oldEffectInstance;
            this.source = source;
        }

        public MobEffectInstance getOldEffectInstance() { return oldEffectInstance; }
        public Entity getEffectSource() { return source; }
    }

    /** Before an effect is taken off ({@code removeEffect}, milk). Cancel to keep it. */
    public static final class Remove extends MobEffectEvent {
        private final MobEffect effect;

        public Remove(LivingEntity entity, MobEffect effect, MobEffectInstance instance) {
            super(entity, instance);
            this.effect = effect;
        }

        public MobEffect getEffect() { return effect; }
    }

    /** An effect that ran out ({@code tickEffects}), just before it goes. */
    public static final class Expired extends MobEffectEvent {
        public Expired(LivingEntity entity, MobEffectInstance instance) {
            super(entity, instance);
        }
    }
}
