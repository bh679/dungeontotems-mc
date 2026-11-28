package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.curse.CurseMarks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla's Totem of Undying clears every effect in 1.20.1; on main (1.21) it clears only those a totem
 * cures, which leaves curses on. Same here: its pop keeps curses ({@link CurseMarks}).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityTotemMixin {

    @Redirect(method = "checkTotemDeathProtection",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"))
    private boolean dungeontotems$keepCurses(LivingEntity self) {
        CurseMarks.removeAllButCurses(self);
        return true;
    }
}
