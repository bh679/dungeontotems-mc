package games.brennan.dungeontotems.mixin;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

/**
 * A mob's own voice ({@code Mob#getAmbientSound} is protected), for a form to make its sound; and its goals
 * ({@code Mob#goalSelector} is protected in vanilla, public only through NeoForge's access transformer).
 */
@Mixin(Mob.class)
public interface MobInvoker {

    @Invoker("getAmbientSound")
    @Nullable
    SoundEvent dungeontotems$ambientSound();

    @Accessor("goalSelector")
    GoalSelector dungeontotems$goalSelector();

    @Accessor("targetSelector")
    GoalSelector dungeontotems$targetSelector();
}
