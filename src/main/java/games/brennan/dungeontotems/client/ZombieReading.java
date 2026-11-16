package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombieDiet;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Zombies can't read: while you are Zombified every item's name and tooltip line (Adventure Item Stats
 * and Names, enchantments, everything) reads as {@link ZombieGibberish}. Runs last, after other mods
 * have added their lines. The name over the hotbar goes the same way ({@code mixin/client/GuiMixin}).
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class ZombieReading {

    private ZombieReading() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        if (!cantRead(event.getEntity())) return;
        List<Component> lines = event.getToolTip();
        int seed = ItemStack.hashItemAndComponents(event.getItemStack());
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.getString().isBlank()) continue; // spacing between sections stays
            lines.set(i, ZombieGibberish.line(seed, i).withStyle(line.getStyle()));
        }
    }

    /** The name shown over the hotbar when you switch items. */
    public static Component name(ItemStack stack, Component name) {
        return ZombieGibberish.line(ItemStack.hashItemAndComponents(stack), 0).withStyle(name.getStyle());
    }

    public static boolean cantRead(Player player) {
        return player != null && ZombieDiet.isZombie(player);
    }
}
