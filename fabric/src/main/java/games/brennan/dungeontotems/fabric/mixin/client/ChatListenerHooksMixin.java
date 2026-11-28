package games.brennan.dungeontotems.fabric.mixin.client;

import games.brennan.dungeontotems.fabric.client.FabricClientHooks;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * NeoForge's {@code ClientChatReceivedEvent} for players' chat (signed and disguised), as it goes into the chat
 * box. System and action-bar lines come through Fabric's own message event.
 */
@Mixin(ChatListener.class)
public abstract class ChatListenerHooksMixin {

    @ModifyArg(method = {"showMessageToPlayer", "lambda$handleDisguisedChatMessage$0"},
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ChatComponent;addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V"),
        index = 0)
    private Component dungeontotems$playerChat(Component message) {
        return FabricClientHooks.playerChat(message);
    }
}
