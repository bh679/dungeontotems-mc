package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.data.DataHolder;
import games.brennan.dungeontotems.data.DataKey;
import games.brennan.dungeontotems.data.TotemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

/** Gives every entity the {@link TotemData} store and saves it with the entity. */
@Mixin(Entity.class)
public abstract class EntityDataMixin implements DataHolder {

    @Unique
    private final Map<DataKey<?>, Object> dungeontotems$values = new HashMap<>();

    @Override
    public Map<DataKey<?>, Object> dungeontotems$data() {
        return dungeontotems$values;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void dungeontotems$save(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        TotemData.save((Entity) (Object) this, tag);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void dungeontotems$load(CompoundTag tag, CallbackInfo ci) {
        TotemData.load((Entity) (Object) this, tag);
    }
}
