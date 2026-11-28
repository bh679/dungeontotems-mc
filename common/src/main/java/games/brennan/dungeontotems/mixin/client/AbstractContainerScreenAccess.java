package games.brennan.dungeontotems.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Where a container screen's slots start: 26.x has no public getter for it. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccess {

    @Accessor("leftPos")
    int dungeontotems$leftPos();

    @Accessor("topPos")
    int dungeontotems$topPos();
}
