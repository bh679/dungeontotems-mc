package games.brennan.dungeontotems.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The key a mapping is bound to (Forge's {@code getKey}). */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

    @Accessor("key")
    InputConstants.Key dungeontotems$key();
}
