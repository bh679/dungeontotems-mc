package games.brennan.dungeontotems.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A container screen's corner, width and hovered slot (Forge adds getters for these; Fabric doesn't). */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {

    @Accessor("leftPos")
    int dungeontotems$leftPos();

    @Accessor("topPos")
    int dungeontotems$topPos();

    @Accessor("imageWidth")
    int dungeontotems$imageWidth();

    @Accessor("hoveredSlot")
    Slot dungeontotems$hoveredSlot();
}
