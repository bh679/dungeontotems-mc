package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombiePack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

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
    /** Above the items' own depth, so the tint covers them. */
    private static final float OVER_ITEMS_Z = 300.0F;

    private ZombiePackOverlay() {}

    @SubscribeEvent
    public static void onForeground(ContainerScreenEvent.Render.Foreground event) {
        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        List<Slot> locked = event.getContainerScreen().getMenu().slots.stream().filter(ZombiePack::keepsItem).toList();
        if (locked.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, OVER_ITEMS_Z);
        for (Slot slot : locked) {
            graphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, LOCKED_TINT);
        }
        graphics.pose().popPose();
    }
}
