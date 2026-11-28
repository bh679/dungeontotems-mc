package games.brennan.dungeontotems.platform.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** An item's tooltip lines, after the game and other mods added theirs (client). Edit the list in place. */
public final class ItemTooltipEvent extends Event {
    private final ItemStack stack;
    private final Player player;
    private final List<Component> toolTip;
    private final TooltipFlag flags;

    public ItemTooltipEvent(ItemStack stack, Player player, List<Component> toolTip, TooltipFlag flags) {
        this.stack = stack;
        this.player = player;
        this.toolTip = toolTip;
        this.flags = flags;
    }

    public ItemStack getItemStack() { return stack; }
    /** May be null (a tooltip built with no player, as at startup). */
    public Player getEntity() { return player; }
    public List<Component> getToolTip() { return toolTip; }
    public TooltipFlag getFlags() { return flags; }
}
