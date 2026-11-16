package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombiePack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A Zombified player's locked pack slots are greyed out, items and all, in every screen that shows the
 * inventory, and say why on hover. See {@link ZombiePack}.
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
        GuiGraphics graphics = event.getGuiGraphics();
        List<Slot> locked = event.getContainerScreen().getMenu().slots.stream().filter(ZombiePack::isLockedSlot).toList();
        if (locked.isEmpty()) return;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, OVER_ITEMS_Z);
        for (Slot slot : locked) {
            graphics.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, LOCKED_TINT);
        }
        graphics.pose().popPose();
    }

    /** An empty locked slot has no tooltip of its own. */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Slot slot = hoveredLockedSlot();
        if (slot == null || slot.hasItem()) return;
        event.getGuiGraphics().renderComponentTooltip(Minecraft.getInstance().font, lines(),
            event.getMouseX(), event.getMouseY());
    }

    /** A locked slot's item keeps its own tooltip, with the reason underneath. */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Slot slot = hoveredLockedSlot();
        if (slot != null && slot.getItem() == event.getItemStack()) event.getToolTip().addAll(lines());
    }

    @Nullable
    private static Slot hoveredLockedSlot() {
        if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)) return null;
        Slot slot = screen.getSlotUnderMouse();
        return slot != null && ZombiePack.isLockedSlot(slot) ? slot : null;
    }

    private static List<Component> lines() {
        return List.of(
            Component.translatable("tooltip.dungeontotems.zombified.pack").withStyle(ChatFormatting.DARK_GREEN),
            Component.translatable("tooltip.dungeontotems.zombified.pack.stash").withStyle(ChatFormatting.GRAY));
    }
}
