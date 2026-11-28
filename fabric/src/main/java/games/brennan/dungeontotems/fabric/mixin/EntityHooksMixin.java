package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's {@code EntityEvent.Size}: the size {@code refreshDimensions} settles on (a morphed player's form). */
@Mixin(Entity.class)
public abstract class EntityHooksMixin {

    @WrapOperation(method = "refreshDimensions", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    private EntityDimensions dungeontotems$size(Entity entity, Pose pose, Operation<EntityDimensions> original) {
        EntityDimensions size = original.call(entity, pose);
        return DtEvents.SIZE.post(new LivingEvents.Size(entity, pose, size)).getNewSize();
    }
}
