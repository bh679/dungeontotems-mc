package games.brennan.dungeontotems.mixin;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** {@code CriteriaTriggers#register}, private in 1.20.1 (Forge's access transformer opens it; Fabric's doesn't). */
@Mixin(CriteriaTriggers.class)
public interface CriteriaTriggersInvoker {

    @Invoker("register")
    static <T extends CriterionTrigger<?>> T dungeontotems$register(T trigger) {
        throw new AssertionError("mixin");
    }
}
