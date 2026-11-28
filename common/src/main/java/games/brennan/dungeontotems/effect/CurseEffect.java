package games.brennan.dungeontotems.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * A curse effect. {@code curable == false} means milk leaves it; no totem pop ever lifts a curse
 * (both enforced by {@link games.brennan.dungeontotems.curse.CurseGuard}: 26.x has no effect cures).
 */
public class CurseEffect extends MobEffect {

    private final boolean curable;

    public CurseEffect(MobEffectCategory category, int color, boolean curable) {
        super(category, color);
        this.curable = curable;
    }

    /** Milk lifts it. */
    public boolean curable() {
        return curable;
    }
}
