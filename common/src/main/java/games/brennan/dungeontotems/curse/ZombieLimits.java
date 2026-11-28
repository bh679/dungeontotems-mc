package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import games.brennan.dungeontotems.platform.event.SubscribeEvent;
import games.brennan.dungeontotems.platform.event.EventBusSubscriber;
import games.brennan.dungeontotems.platform.event.Event;
import games.brennan.dungeontotems.platform.event.AnimalTameEvent;
import games.brennan.dungeontotems.platform.event.PlayerContainerEvent;
import games.brennan.dungeontotems.platform.event.PlayerEvent;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.BlockEvent;

/**
 * Two more things a Zombified player can't do:
 * <ul>
 *   <li><b>Shulker boxes</b>: no opening one, placing one or mining one, and no opening one from your
 *       hand (mods such as Advanced Shulkerboxes open a held box on use). A box carried in the hotbar
 *       stays an item until you are cured.</li>
 *   <li><b>Feeding and taming</b>: an animal offered its food or taming item (a bone, seeds) by a zombie
 *       runs away instead of taking it, and the item isn't used. No animal takes a zombie as its owner,
 *       including a horse, donkey, mule or llama ridden to tame it. Animals tamed before stay yours.</li>
 * </ul>
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class ZombieLimits {

    private static final int FLEE_DISTANCE = 16;
    private static final int FLEE_HEIGHT = 7;
    private static final double FLEE_SPEED = 1.6;

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
            event.setUseItem(Event.Result.DENY);
            tell(player, "message.dungeontotems.zombified.shulker");
        }
    }

    /**
     * Both sides: a held box is never used. Right-clicking a block with one only stops the placing above,
     * and the game then uses the item in hand, which is where a mod like Advanced Shulkerboxes opens it.
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!ZombieDiet.isZombie(event.getEntity()) || !isShulkerBox(event.getItemStack())) return;
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
        tell(event.getEntity(), "message.dungeontotems.zombified.shulker");
    }

    /** Backstop for any other way a shulker box's screen opens (another mod, a command): it shuts again. */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (!ZombieDiet.isZombie(player) || !(event.getContainer() instanceof ShulkerBoxMenu)) return;
        if (player instanceof net.minecraft.server.level.ServerPlayer server) server.closeContainer();
        tell(player, "message.dungeontotems.zombified.shulker");
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

    /** Both sides, so the client never uses the item. The animal bolts, with nothing said. */
    @SubscribeEvent
    public static void onFeed(PlayerInteractEvent.EntityInteract event) {
        if (!ZombieDiet.isZombie(event.getEntity()) || !(event.getTarget() instanceof Animal animal)) return;
        if (!wouldTake(animal, event.getItemStack())) return;
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
        if (!animal.level().isClientSide()) runFrom(animal, event.getEntity());
    }

    /** 1.21's {@code #minecraft:parrot_food}: {@code Parrot.TAME_FOOD} in 1.20.1, which is private. */
    private static final java.util.Set<net.minecraft.world.item.Item> PARROT_FOOD = java.util.Set.of(
        Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS, Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD);

    /** Its breeding food, or what tames it (a wolf's bone, a parrot's seeds). */
    public static boolean wouldTake(Animal animal, ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (animal.isFood(stack)) return true;
        return (animal instanceof Wolf && stack.is(Items.BONE)) || (animal instanceof Parrot && PARROT_FOOD.contains(stack.getItem()));
    }

    /** Off at a run to somewhere away from the zombie; straight away from them when no spot is found. */
    static void runFrom(Animal animal, Player player) {
        Vec3 away = DefaultRandomPos.getPosAway(animal, FLEE_DISTANCE, FLEE_HEIGHT, player.position());
        if (away == null) {
            Vec3 direction = animal.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
            if (direction.lengthSqr() < 1.0E-4) direction = new Vec3(1.0, 0.0, 0.0);
            away = animal.position().add(direction.normalize().scale(FLEE_DISTANCE / 2.0));
        }
        animal.getNavigation().moveTo(away.x, away.y, away.z, FLEE_SPEED);
    }

    /** Ridden taming (horses, donkeys, mules, llamas) and any other path: refused, silently. */
    @SubscribeEvent
    public static void onTame(AnimalTameEvent event) {
        if (ZombieDiet.isZombie(event.getTamer())) event.setCanceled(true);
    }

    private static void tell(Player player, String key) {
        if (player instanceof ServerPlayer server) server.displayClientMessage(Component.translatable(key), true);
    }
}
