package games.brennan.dungeontotems.mixin;

import games.brennan.dungeontotems.data.TotemData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A player coming back from the End is a new entity: keep their {@link TotemData}, as main's
 * attachments are kept. After a death it is not kept (main's attachments aren't copied on death).
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDataMixin {

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void dungeontotems$keepData(ServerPlayer old, boolean keepEverything, CallbackInfo ci) {
        if (keepEverything) TotemData.copy(old, (ServerPlayer) (Object) this);
    }
}
