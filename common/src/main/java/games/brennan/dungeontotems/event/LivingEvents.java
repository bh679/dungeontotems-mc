package games.brennan.dungeontotems.event;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/** Events on living entities (and entities at large), named after NeoForge's. */
public final class LivingEvents {

    private LivingEvents() {}

    /** Shared base: the entity the event is about. */
    public abstract static class OfEntity<T extends Entity> extends DtEvent {
        private final T entity;

        protected OfEntity(T entity) {
            this.entity = entity;
        }

        public T getEntity() {
            return entity;
        }
    }

    /** About to die ({@code LivingEntity#die}, after vanilla's totem). Cancel and it lives. */
    public static final class Death extends OfEntity<LivingEntity> {
        private final DamageSource source;

        public Death(LivingEntity entity, DamageSource source) {
            super(entity);
            this.source = source;
        }

        public DamageSource getSource() {
            return source;
        }
    }

    /** Damage on its way in, before armour. Cancel to take none; change the amount. */
    public static final class IncomingDamage extends OfEntity<LivingEntity> {
        private final DamageSource source;
        private float amount;

        public IncomingDamage(LivingEntity entity, DamageSource source, float amount) {
            super(entity);
            this.source = source;
            this.amount = amount;
        }

        public DamageSource getSource() {
            return source;
        }

        public float getAmount() {
            return amount;
        }

        public void setAmount(float amount) {
            this.amount = amount;
        }
    }

    /** After damage was dealt (end of {@code hurtServer}, a fatal hit included). */
    public static final class DamagePost extends OfEntity<LivingEntity> {
        private final DamageSource source;
        private final float healthDamage;

        public DamagePost(LivingEntity entity, DamageSource source, float healthDamage) {
            super(entity);
            this.source = source;
            this.healthDamage = healthDamage;
        }

        public DamageSource getSource() {
            return source;
        }

        /** What came off health, after armour and absorption. */
        public float getHealthDamage() {
            return healthDamage;
        }
    }

    /** A vanilla Totem of Undying is about to save this entity. */
    public static final class UseTotem extends OfEntity<LivingEntity> {
        public UseTotem(LivingEntity entity) {
            super(entity);
        }
    }

    public static final class EffectAdded extends OfEntity<LivingEntity> {
        private final MobEffectInstance effectInstance;
        @Nullable private final MobEffectInstance oldEffectInstance;

        public EffectAdded(LivingEntity entity, MobEffectInstance effectInstance, @Nullable MobEffectInstance oldEffectInstance) {
            super(entity);
            this.effectInstance = effectInstance;
            this.oldEffectInstance = oldEffectInstance;
        }

        public MobEffectInstance getEffectInstance() {
            return effectInstance;
        }

        @Nullable
        public MobEffectInstance getOldEffectInstance() {
            return oldEffectInstance;
        }
    }

    /** An effect is being taken off (milk, a pop, a command, a cure). Cancel and it stays. */
    public static final class EffectRemove extends OfEntity<LivingEntity> {
        private final Holder<MobEffect> effect;
        @Nullable private final MobEffectInstance effectInstance;

        public EffectRemove(LivingEntity entity, Holder<MobEffect> effect, @Nullable MobEffectInstance effectInstance) {
            super(entity);
            this.effect = effect;
            this.effectInstance = effectInstance;
        }

        public Holder<MobEffect> getEffect() {
            return effect;
        }

        @Nullable
        public MobEffectInstance getEffectInstance() {
            return effectInstance;
        }
    }

    /** An effect ran out. */
    public static final class EffectExpired extends OfEntity<LivingEntity> {
        @Nullable private final MobEffectInstance effectInstance;

        public EffectExpired(LivingEntity entity, @Nullable MobEffectInstance effectInstance) {
            super(entity);
            this.effectInstance = effectInstance;
        }

        @Nullable
        public MobEffectInstance getEffectInstance() {
            return effectInstance;
        }
    }

    /** Whether the entity can breathe where it is this tick. */
    public static final class Breathe extends OfEntity<LivingEntity> {
        private boolean canBreathe;

        public Breathe(LivingEntity entity, boolean canBreathe) {
            super(entity);
            this.canBreathe = canBreathe;
        }

        public boolean canBreathe() {
            return canBreathe;
        }

        public void setCanBreathe(boolean canBreathe) {
            this.canBreathe = canBreathe;
        }
    }

    /** About to take fall damage. Cancel for none; change the distance. */
    public static final class Fall extends OfEntity<LivingEntity> {
        private double distance;

        public Fall(LivingEntity entity, double distance) {
            super(entity);
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
    }

    public static final class Jump extends OfEntity<LivingEntity> {
        public Jump(LivingEntity entity) {
            super(entity);
        }
    }

    /** A mob is about to take a new target. Cancel and it keeps the old one. */
    public static final class ChangeTarget extends OfEntity<LivingEntity> {
        @Nullable private final LivingEntity newTarget;

        public ChangeTarget(LivingEntity entity, @Nullable LivingEntity newTarget) {
            super(entity);
            this.newTarget = newTarget;
        }

        @Nullable
        public LivingEntity getNewAboutToBeSetTarget() {
            return newTarget;
        }
    }

    /** Starting to use (eat, drink) an item. Cancel and it isn't used. */
    public static final class UseItemStart extends OfEntity<LivingEntity> {
        private final ItemStack item;

        public UseItemStart(LivingEntity entity, ItemStack item) {
            super(entity);
            this.item = item;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    /** Finished using an item: {@code item} is a copy from before it was used up. */
    public static final class UseItemFinish extends OfEntity<LivingEntity> {
        private final ItemStack item;

        public UseItemFinish(LivingEntity entity, ItemStack item) {
            super(entity);
            this.item = item;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    /** Let go of an item before it was used up. */
    public static final class UseItemStop extends OfEntity<LivingEntity> {
        private final ItemStack item;

        public UseItemStop(LivingEntity entity, ItemStack item) {
            super(entity);
            this.item = item;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    /** An entity's size is being worked out for {@code pose}. */
    public static final class Size extends OfEntity<Entity> {
        private final Pose pose;
        private EntityDimensions newSize;

        public Size(Entity entity, Pose pose, EntityDimensions size) {
            super(entity);
            this.pose = pose;
            this.newSize = size;
        }

        public Pose getPose() {
            return pose;
        }

        public EntityDimensions getNewSize() {
            return newSize;
        }

        public void setNewSize(EntityDimensions newSize) {
            this.newSize = newSize;
        }
    }

    public static final class JoinLevel extends OfEntity<Entity> {
        private final Level level;

        public JoinLevel(Entity entity, Level level) {
            super(entity);
            this.level = level;
        }

        public Level getLevel() {
            return level;
        }
    }

    public static final class LeaveLevel extends OfEntity<Entity> {
        private final Level level;

        public LeaveLevel(Entity entity, Level level) {
            super(entity);
            this.level = level;
        }

        public Level getLevel() {
            return level;
        }
    }
}
