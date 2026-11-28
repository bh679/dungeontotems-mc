package games.brennan.dungeontotems.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The key a mapping is bound to ({@code KeyMapping#key}; NeoForge adds {@code getKey()}, vanilla keeps it protected), for the self-tests. */
@Mixin(KeyMapping.class)
public interface KeyMappingAccess {

    @Accessor("key")
    InputConstants.Key dungeontotems$key();
}
