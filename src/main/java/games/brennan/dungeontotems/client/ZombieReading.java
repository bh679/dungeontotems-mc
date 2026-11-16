package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.ZombieDiet;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * Zombies can't read: while you are Zombified every item's name and tooltip line (Adventure Item Stats
 * and Names, enchantments, everything) reads as {@link ZombieGibberish}. Runs last, after other mods
 * have added their lines. The name over the hotbar goes the same way ({@code mixin/client/GuiMixin}), and so
 * does every book page, a lectern's included ({@code mixin/client/BookViewScreenMixin}), and chat.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class ZombieReading {

    private ZombieReading() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        if (!cantRead(event.getEntity())) return;
        List<Component> lines = event.getToolTip();
        ItemStack stack = event.getItemStack();
        int seed = ItemStack.hashItemAndComponents(stack);
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.getString().isBlank()) continue; // spacing between sections stays
            lines.set(i, (i == 0 ? nameLine(stack, seed) : ZombieGibberish.line(seed, i)).withStyle(line.getStyle()));
        }
    }

    /** The name shown over the hotbar when you switch items. */
    public static Component name(ItemStack stack, Component name) {
        return nameLine(stack, ItemStack.hashItemAndComponents(stack)).withStyle(name.getStyle());
    }

    /** A potion of Weakness gives itself away, so a zombie can still find half the cure. */
    private static MutableComponent nameLine(ItemStack stack, int seed) {
        return ZombieGibberish.weakens(stack) ? ZombieGibberish.weaknessLabel(seed) : ZombieGibberish.line(seed, 0);
    }

    /** Chat reads as mumbles too, a player's words mostly readable. Action-bar lines stay, so a zombie's own "Yuck!" still reads. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onChat(ClientChatReceivedEvent event) {
        if (!cantRead(Minecraft.getInstance().player)) return;
        if (event instanceof ClientChatReceivedEvent.System system && system.isOverlay()) return;
        Component message = event.getMessage();
        String text = message.getString();
        // A player's words: 1 in 3 turns to a mumble. The game's own messages are all mumbles.
        MutableComponent read = event.isSystem()
            ? ZombieGibberish.page(text.hashCode(), words(text))
            : ZombieGibberish.read(text, text.hashCode());
        event.setMessage(read.withStyle(message.getStyle()));
    }

    /** A book page: 1 word in 3 turns to a mumble, the rest read. An empty page still mumbles. */
    public static FormattedText page(FormattedText page, int pageIndex) {
        String text = page.getString();
        int seed = text.hashCode() * 31 + pageIndex;
        return text.isBlank() ? ZombieGibberish.page(seed, 0) : ZombieGibberish.read(text, seed);
    }

    private static int words(String text) {
        return text.isBlank() ? 0 : text.trim().split("\\s+").length;
    }

    public static boolean cantRead(Player player) {
        return player != null && ZombieDiet.isZombie(player);
    }
}
