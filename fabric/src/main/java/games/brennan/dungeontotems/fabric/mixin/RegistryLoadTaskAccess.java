package games.brennan.dungeontotems.fabric.mixin;

import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The registry a data-registry load task fills ({@code FabricGameTestModInitializerMixin}). */
@Mixin(RegistryLoadTask.class)
public interface RegistryLoadTaskAccess {

    @Accessor("registry")
    WritableRegistry<?> dungeontotems$registry();
}
