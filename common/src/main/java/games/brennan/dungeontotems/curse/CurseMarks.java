package games.brennan.dungeontotems.curse;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

/**
 * Which effect instances are curses. Main does this with NeoForge's per-instance cures (a curse has no
 * {@code PROTECTED_BY_TOTEM} cure); 1.20.1 has none, so {@code mixin/MobEffectInstanceMixin} adds a flag that
 * is saved and copied with the instance. A totem pop clears every effect but the curses
 * ({@link #removeAllButCurses}), as on main.
 */
public final class CurseMarks {

    /** Added to {@link MobEffectInstance} by {@code mixin/MobEffectInstanceMixin}. */
    public interface Marked {
        boolean dungeontotems$isCurse();

        void dungeontotems$setCurse(boolean curse);
    }

    private CurseMarks() {}

    public static MobEffectInstance mark(MobEffectInstance instance) {
        ((Marked) instance).dungeontotems$setCurse(true);
        return instance;
    }

    public static boolean isCurse(MobEffectInstance instance) {
        return ((Marked) instance).dungeontotems$isCurse();
    }

    /** What a totem pop clears: every effect on {@code entity} except its curses. */
    public static void removeAllButCurses(LivingEntity entity) {
        for (MobEffectInstance instance : new ArrayList<>(entity.getActiveEffects())) {
            if (!isCurse(instance)) entity.removeEffect(instance.getEffect());
        }
    }
}
