package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Two more things a Zombified player can't do:
 * <ul>
 *   <li><b>Shulker boxes</b>: no opening one, placing one or mining one. A box carried in the hotbar
 *       stays an item until you are cured.</li>
 *   <li><b>Taming</b>: no animal takes a zombie as its owner: wolves, cats, ocelots, parrots, horses,
 *       donkeys, mules and llamas all refuse, as they do on a failed try. Animals tamed before stay yours.</li>
 * </ul>
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class ZombieLimits {

    private ZombieLimits() {}

    public static boolean isShulkerBox(BlockState state) {
        return state.getBlock() instanceof ShulkerBoxBlock;
    }

    public static boolean isShulkerBox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ShulkerBoxBlock;
    }

    /** Both sides, so the client never opens or places one. Clicking another block with a box in hand still uses that block. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!ZombieDiet.isZombie(player)) return;
        if (isShulkerBox(event.getLevel().getBlockState(event.getPos()))) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            tell(player, "message.dungeontotems.zombified.shulker");
        } else if (isShulkerBox(event.getItemStack())) {
            event.setUseItem(TriState.FALSE);
            tell(player, "message.dungeontotems.zombified.shulker");
        }
    }

    /** No cracks: the box never starts to break. */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (ZombieDiet.isZombie(event.getEntity()) && isShulkerBox(event.getState())) event.setCanceled(true);
    }

    /** Backstop for an instant break (creative, or another mod's tool). */
    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!ZombieDiet.isZombie(event.getPlayer()) || !isShulkerBox(event.getState())) return;
        event.setCanceled(true);
        tell(event.getPlayer(), "message.dungeontotems.zombified.shulker");
    }

    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (!ZombieDiet.isZombie(event.getTamer())) return;
        event.setCanceled(true);
        tell(event.getTamer(), "message.dungeontotems.zombified.tame");
    }

    private static void tell(Player player, String key) {
        if (player instanceof ServerPlayer server) server.displayClientMessage(Component.translatable(key), true);
    }
}
