package games.brennan.dungeontotems.fabric.client;

import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;

/** Where Fabric's client mixins fire the common client events (NeoForge's spots). */
public final class FabricClientHooks {

    private FabricClientHooks() {}

    /** Before a typed character reaches the screen. */
    public static void charTyped(Screen screen, CharacterEvent event) {
        DtClientEvents.CHAR_TYPED.post(new ClientEvents.CharTyped(screen, event.codepoint()));
    }

    /** A player's chat line, as it goes into the chat box. */
    public static Component playerChat(Component message) {
        return DtClientEvents.CHAT_RECEIVED.post(new ClientEvents.ChatReceived(message, false, false)).getMessage();
    }
}
