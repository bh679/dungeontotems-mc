package games.brennan.dungeontotems.fabric.mixin;

import games.brennan.dungeontotems.platform.event.PlayerEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Forge 47's login (end of {@code placeNewPlayer}) and logout (start of {@code remove}). */
@Mixin(PlayerList.class)
public abstract class PlayerListHooksMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void dungeontotems$loggedIn(Connection connection, ServerPlayer player, CallbackInfo ci) {
        TotemEvents.post(new PlayerEvent.PlayerLoggedInEvent(player));
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void dungeontotems$loggedOut(ServerPlayer player, CallbackInfo ci) {
        TotemEvents.post(new PlayerEvent.PlayerLoggedOutEvent(player));
    }
}
