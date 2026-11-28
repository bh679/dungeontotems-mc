package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import games.brennan.dungeontotems.mixin.client.BookEditScreenAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.resources.language.I18n;
import games.brennan.dungeontotems.platform.event.Dist;
import games.brennan.dungeontotems.platform.event.SubscribeEvent;
import games.brennan.dungeontotems.platform.event.EventBusSubscriber;
import games.brennan.dungeontotems.platform.event.ClientChatEvent;
import games.brennan.dungeontotems.platform.event.ScreenEvent;

import java.util.Random;

/**
 * Zombies can't write either: in chat or a book, each word turns into a mumble the moment the space after
 * it is typed, and the last word of a chat message when it is sent. One word in
 * {@link ZombieGibberish#WORD_THROUGH_ODDS} comes through as typed. Commands ({@code /...}) are left alone.
 * Everyone else reads what was sent, gibberish and all.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class ZombieWriting {

    private static final Random RANDOM = new Random();

    private ZombieWriting() {}

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event) {
        if (event.getCodePoint() != ' ' || !ZombieReading.cantRead(Minecraft.getInstance().player)) return;
        Screen screen = event.getScreen();
        if (screen instanceof ChatScreen && screen.getFocused() instanceof EditBox input) {
            if (input.getValue().startsWith("/")) return;
            ZombieGibberish.Edit edit = write(input.getValue(), input.getCursorPosition());
            input.setValue(edit.text());
            input.setCursorPosition(edit.cursor());
            input.setHighlightPos(edit.cursor());
        } else if (screen instanceof BookEditScreen && screen instanceof BookEditScreenAccess book) {
            TextFieldHelper cursor = book.dungeontotems$pageEdit();
            ZombieGibberish.Edit edit = write(book.dungeontotems$pageText(), cursor.getCursorPos());
            book.dungeontotems$setPageText(edit.text());
            cursor.setCursorPos(edit.cursor(), false);
        }
    }

    /** The last word, which no space followed. */
    @SubscribeEvent
    public static void onChatSent(ClientChatEvent event) {
        String message = event.getMessage();
        if (message.startsWith("/") || !ZombieReading.cantRead(Minecraft.getInstance().player)) return;
        event.setMessage(write(message, message.length()).text());
    }

    static ZombieGibberish.Edit write(String text, int cursor) {
        return ZombieGibberish.writeWord(text, cursor, RANDOM, I18n::get);
    }
}
