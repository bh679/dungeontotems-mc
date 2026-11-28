package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.mixin.client.AbstractContainerScreenAccess;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombiePack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

/**
 * A Zombified player's locked pack slots are greyed out, items and all, in every screen that shows the
 * inventory. A locked slot holding something a zombie can still take out ({@link ZombiePack#reachable}) stays
 * clear, so they can see it. See {@link ZombiePack}.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class ZombiePackOverlay {

    /** Rotten green-grey, mostly opaque so the items read as out of reach. */
    private static final int LOCKED_TINT = 0xB0283222;
    private static final int SLOT_SIZE = 16;
    private ZombiePackOverlay() {}

    /** After the slots are drawn. 26.x GUIs are flat: a new stratum puts the tint over the items. */
    @SubscribeEvent
    public static void onForeground(ScreenEvent.Render.Foreground event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        List<Slot> locked = screen.getMenu().slots.stream().filter(slot -> slot.isActive() && ZombiePack.keepsItem(slot)).toList();
        if (locked.isEmpty()) return;
        graphics.nextStratum();
        int left = ((AbstractContainerScreenAccess) screen).dungeontotems$leftPos();
        int top = ((AbstractContainerScreenAccess) screen).dungeontotems$topPos();
        for (Slot slot : locked) {
            graphics.fill(left + slot.x, top + slot.y, left + slot.x + SLOT_SIZE, top + slot.y + SLOT_SIZE, LOCKED_TINT);
        }
    }
}
