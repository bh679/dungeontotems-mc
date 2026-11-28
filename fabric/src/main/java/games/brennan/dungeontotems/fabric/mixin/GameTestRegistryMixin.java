package games.brennan.dungeontotems.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraft.gametest.framework.TestFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Method;

/**
 * The shared GameTests name their template {@code "empty"} (Forge gives it our namespace). Fabric takes a
 * template verbatim, which would be {@code minecraft:empty}: ours become {@code dungeontotems:empty}.
 */
@Mixin(GameTestRegistry.class)
public abstract class GameTestRegistryMixin {

    private static final String TESTS = "games.brennan.dungeontotems.gametest.";

    @WrapOperation(method = "register(Ljava/lang/reflect/Method;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/gametest/framework/GameTestRegistry;turnMethodIntoTestFunction(Ljava/lang/reflect/Method;)Lnet/minecraft/gametest/framework/TestFunction;"))
    private static TestFunction dungeontotems$ourTemplate(Method method, Operation<TestFunction> original) {
        TestFunction test = original.call(method);
        if (!method.getDeclaringClass().getName().startsWith(TESTS) || test.getStructureName().contains(":")) return test;
        return new TestFunction(test.getBatchName(), test.getTestName(), DungeonTotems.MOD_ID + ":" + test.getStructureName(),
            test.getRotation(), test.getMaxTicks(), test.getSetupTicks(), test.isRequired(), test.getRequiredSuccesses(),
            test.getMaxAttempts(), test::run);
    }
}
