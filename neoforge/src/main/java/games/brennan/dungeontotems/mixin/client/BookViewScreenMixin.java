package games.brennan.dungeontotems.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import games.brennan.dungeontotems.client.ZombieReading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A book's pages read as zombie gibberish while you are Zombified ({@link ZombieReading}): written books,
 * lecterns, anything that shows a {@link BookViewScreen}. Clickable text goes with it.
 */
@Mixin(BookViewScreen.class)
public abstract class BookViewScreenMixin {

    @WrapOperation(method = "visitText", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/BookViewScreen$BookAccess;getPage(I)Lnet/minecraft/network/chat/Component;"))
    private Component dungeontotems$zombieCantRead(BookViewScreen.BookAccess book, int index, Operation<Component> original) {
        Component page = original.call(book, index);
        if (!ZombieReading.cantRead(Minecraft.getInstance().player)) return page;
        FormattedText mumbled = ZombieReading.page(page, index);
        return mumbled instanceof Component component ? component : Component.literal(mumbled.getString());
    }
}
