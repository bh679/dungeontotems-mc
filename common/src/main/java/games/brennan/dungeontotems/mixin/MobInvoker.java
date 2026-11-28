package games.brennan.dungeontotems.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

/** A mob's own voice ({@code Mob#getAmbientSound} is protected), for a form to make its sound. */
@Mixin(Mob.class)
public interface MobInvoker {

    /** {@code Mob#goalSelector}: Forge's access transformer opens it; this works on every loader. */
    @Accessor("goalSelector")
    GoalSelector dungeontotems$goalSelector();

    @Accessor("targetSelector")
    GoalSelector dungeontotems$targetSelector();

    @Invoker("getAmbientSound")
    @Nullable
    SoundEvent dungeontotems$ambientSound();
}
