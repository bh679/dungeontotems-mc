package games.brennan.dungeontotems.mixin.client;

import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The page being written and its cursor, for a zombie's writing ({@code client/ZombieWriting}). */
@Mixin(BookEditScreen.class)
public interface BookEditScreenAccess {

    @Invoker("getCurrentPageText")
    String dungeontotems$pageText();

    @Invoker("setCurrentPageText")
    void dungeontotems$setPageText(String text);

    @Accessor("pageEdit")
    TextFieldHelper dungeontotems$pageEdit();
}
