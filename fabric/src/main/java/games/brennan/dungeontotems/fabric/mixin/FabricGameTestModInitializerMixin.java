package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.fabric.FabricGameTests;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.RegistryLoadTask;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Fabric API adds its {@code @GameTest} instances to the test-instance registry as the data registries
 * load; ours go in at the same moment ({@link FabricGameTests}). {@link Pseudo}: players' Fabric API jars
 * leave the GameTest module out, and then there is nothing to hook (and nothing to test).
 */
@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.gametest.FabricGameTestModInitializer", remap = false)
public abstract class FabricGameTestModInitializerMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "registerDynamicEntries", at = @At("TAIL"), require = 0)
    private static void dungeontotems$registerOurs(List<RegistryLoadTask<?>> loadTasks, CallbackInfo ci) {
        Registry<GameTestInstance> instances = null;
        Registry<TestEnvironmentDefinition<?>> environments = null;
        for (RegistryLoadTask<?> task : loadTasks) {
            Registry<?> registry = ((RegistryLoadTaskAccess) task).dungeontotems$registry();
            if (registry.key() == Registries.TEST_INSTANCE) instances = (Registry<GameTestInstance>) registry;
            if (registry.key() == Registries.TEST_ENVIRONMENT) environments = (Registry<TestEnvironmentDefinition<?>>) registry;
        }
        if (instances != null && environments != null) FabricGameTests.registerInstances(instances, environments);
    }
}
