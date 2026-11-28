package games.brennan.dungeontotems.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.client.ZombieReading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A book's pages read as zombie gibberish while you are Zombified ({@link ZombieReading}): written books,
 * lecterns, anything that shows a {@link BookViewScreen}. Clickable text goes with it.
 */
@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;getPage(I)Lnet/minecraft/network/chat/FormattedText;"))
    private FormattedText dungeontotems$zombieCantRead(BookViewScreen.BookAccess book, int index, Operation<FormattedText> original) {
        FormattedText page = original.call(book, index);
        return ZombieReading.cantRead(Minecraft.getInstance().player) ? ZombieReading.page(page, index) : page;
    }
}
