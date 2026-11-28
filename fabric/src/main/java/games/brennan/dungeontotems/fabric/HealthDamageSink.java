package games.brennan.dungeontotems.fabric;

/** What a hit took off health, set by {@code Player#actuallyHurt} as well as {@code LivingEntity}'s ({@code mixin/LivingEntityHooksMixin}). */
public interface HealthDamageSink {
    void dungeontotems$setHealthDamage(float taken);
}
