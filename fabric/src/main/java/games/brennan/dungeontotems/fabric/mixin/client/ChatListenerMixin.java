package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.platform.event.ClientChatReceivedEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Forge's {@code ClientChatReceivedEvent}: each chat line as it goes into the chat box, a player's (signed or
 * disguised) or the game's own ({@link ClientChatReceivedEvent.System}; action-bar ones too, before they show).
 */
@Mixin(ChatListener.class)
public abstract class ChatListenerMixin {

    @Unique private boolean dungeontotems$system;

    @ModifyVariable(method = "handleSystemMessage", at = @At("HEAD"), argsOnly = true)
    private Component dungeontotems$system(Component message, Component same, boolean overlay) {
        dungeontotems$system = true;
        ClientChatReceivedEvent event = new ClientChatReceivedEvent.System(message, overlay);
        TotemEvents.post(event);
        return event.getMessage();
    }

    @Inject(method = "handleSystemMessage", at = @At("RETURN"))
    private void dungeontotems$systemDone(Component message, boolean overlay, CallbackInfo ci) {
        dungeontotems$system = false;
    }

    /** A player's chat (signed); the game's system lines went through {@link #dungeontotems$system} already. */
    @ModifyArg(method = "*", index = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"))
    private Component dungeontotems$playerChat(Component message) {
        return dungeontotems$player(message);
    }

    /** A disguised player message ({@code /say}, {@code /me}...) goes in with one argument, as the game's own lines do. */
    @ModifyArg(method = "*", index = 0, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"))
    private Component dungeontotems$oneArgChat(Component message) {
        return dungeontotems$system ? message : dungeontotems$player(message);
    }

    @Unique
    private static Component dungeontotems$player(Component message) {
        ClientChatReceivedEvent event = new ClientChatReceivedEvent(message, null);
        TotemEvents.post(event);
        return event.getMessage();
    }
}
