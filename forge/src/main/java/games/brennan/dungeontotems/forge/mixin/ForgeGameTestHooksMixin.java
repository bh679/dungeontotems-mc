package games.brennan.dungeontotems.forge.mixin;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraftforge.gametest.ForgeGameTestHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

/**
 * The shared GameTests ({@code common/.../gametest}) can't carry Forge's {@code @GameTestHolder} and
 * {@code @PrefixGameTestTemplate(false)}: this gives their methods what those did, our namespace and
 * unprefixed templates ({@code dungeontotems:empty}). {@link games.brennan.dungeontotems.forge.ForgeGameTests}
 * registers the classes.
 */
@Mixin(value = ForgeGameTestHooks.class, remap = false)
public abstract class ForgeGameTestHooksMixin {

    private static final String TESTS = "games.brennan.dungeontotems.gametest.";

    @Inject(method = "getTemplateNamespace", at = @At("HEAD"), cancellable = true)
    private static void dungeontotems$namespace(Method method, CallbackInfoReturnable<String> cir) {
        if (method.getDeclaringClass().getName().startsWith(TESTS)) cir.setReturnValue(DungeonTotems.MOD_ID);
    }

    @Inject(method = "prefixGameTestTemplate", at = @At("HEAD"), cancellable = true)
    private static void dungeontotems$unprefixed(Method method, CallbackInfoReturnable<Boolean> cir) {
        if (method.getDeclaringClass().getName().startsWith(TESTS)) cir.setReturnValue(false);
    }
}
