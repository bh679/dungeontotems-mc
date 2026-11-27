package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.net.ZombieCarriedPayload;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * A Zombified player can't reach their pack: the 27 main inventory slots are locked, and only the hotbar,
 * armour and offhand work. Nothing comes out of a locked slot, and nothing new goes in; what you carried
 * in your hotbar and offhand when you turned (the {@code zombie_carried} snapshot) can still be put away in your
 * own inventory screen, never while a container is open.
 * Pickups only fill the hotbar and offhand. Creative and spectator players are not locked.
 * The one way out: a potion of Weakness or a Totem of Weakness ({@link #reachable}) can still be taken out
 * of a locked slot, so a zombie is never locked away from half of their cure. Nothing new goes back in.
 *
 * <p>The checks run on both sides through {@code mixin/SlotMixin}, {@code mixin/InventoryMixin} and
 * {@code mixin/AbstractContainerMenuMixin}, so the client's snapshot is synced with
 * {@link ZombieCarriedPayload}.</p>
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class ZombiePack {

    private static final int OFFHAND_SLOT = Inventory.SLOT_OFFHAND;

    /** Host-declared extra pack slots (e.g. a backpack mod's): each maps a slot to its owner, or null. */
    private static final List<Function<Slot, Player>> EXTRA_PACK_SLOTS = new CopyOnWriteArrayList<>();

    private ZombiePack() {}

    /** Whether {@code player}'s pack is locked right now. */
    public static boolean isLocked(Player player) {
        // abilities, not isCreative(): a GameTest mock player always reports creative.
        return ZombieDiet.isZombie(player) && !player.getAbilities().instabuild && !player.isSpectator();
    }

    /** One of the 27 main inventory slots (not the hotbar, armour or offhand). */
    public static boolean isPackSlot(int index) {
        return index >= Inventory.getSelectionSize() && index < Inventory.INVENTORY_SIZE;
    }

    /**
     * Declare more slots as part of a player's pack, locked with it while Zombified (greyed out, nothing in or
     * out). {@code ownerOf} returns the player whose pack {@code slot} belongs to, or {@code null} for any other
     * slot. Called once at boot by a host mod; Dungeon Train adds Edible Backpacks' slots this way. The host
     * must also block its own item routes (its slots may override {@code mayPlace}, and pickups).
     */
    public static void addPackSlots(Function<Slot, Player> ownerOf) {
        EXTRA_PACK_SLOTS.add(ownerOf);
    }

    /** A menu slot showing one of {@code player}'s locked pack slots. */
    public static boolean isLockedSlot(Slot slot) {
        Player owner = packOwner(slot);
        return owner != null && isLocked(owner);
    }

    /**
     * {@code slot} is locked and what it holds stays there: the lock on taking things out. A locked slot holding
     * something {@link #reachable} gives it up.
     */
    public static boolean keepsItem(Slot slot) {
        return isLockedSlot(slot) && !reachable(slot.getItem());
    }

    /**
     * What a zombie can still take out of a locked pack: a potion of Weakness (drunk, splash or lingering;
     * not a tipped arrow) or a Totem of Weakness. The same answer on both sides, from the stack alone.
     */
    public static boolean reachable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(TotemRegistries.TOTEMS.get(Curse.WEAKNESS).get())) return true;
        boolean potion = stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
        return potion && ZombieGibberish.weakens(stack);
    }

    /** The player whose pack {@code slot} is part of (main inventory or a host-declared slot), or null. */
    private static Player packOwner(Slot slot) {
        if (slot.container instanceof Inventory inventory) {
            return isPackSlot(slot.getContainerSlot()) ? inventory.player : null;
        }
        for (Function<Slot, Player> ownerOf : EXTRA_PACK_SLOTS) {
            Player owner = ownerOf.apply(slot);
            if (owner != null) return owner;
        }
        return null;
    }

    /** Something you carried when you turned, so it may go into a locked slot. */
    public static boolean carriedBefore(Player player, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (ItemStack carried : player.getData(TotemRegistries.ZOMBIE_CARRIED)) {
            if (ItemStack.isSameItemSameComponents(carried, stack)) return true;
        }
        return false;
    }

    /**
     * {@code slot} would refuse {@code stack}: a locked slot and something new. A host-declared slot takes
     * nothing at all while locked; only the main inventory takes back what you carried when you turned, and only
     * in your own inventory screen. With a chest or any other menu open the pack takes nothing, so what you
     * take out of a container goes to the hotbar or stays in the container.
     */
    public static boolean refuses(Slot slot, ItemStack stack) {
        if (!isLockedSlot(slot)) return false;
        if (!(slot.container instanceof Inventory inventory)) return true;
        return !inOwnInventory(inventory.player) || !carriedBefore(inventory.player, stack);
    }

    /** {@code player}'s own inventory screen is the open menu (no chest, barrel or other container). */
    private static boolean inOwnInventory(Player player) {
        return player.containerMenu == player.inventoryMenu;
    }

    /**
     * Where a pickup may stack while locked: the selected slot, the offhand, then the hotbar. -1 for none.
     * Mirrors {@code Inventory#getSlotWithRemainingSpace} over the unlocked slots only.
     */
    public static int slotWithRemainingSpace(Inventory inventory, ItemStack stack) {
        if (hasRemainingSpaceFor(inventory.getItem(inventory.selected), stack)) return inventory.selected;
        if (hasRemainingSpaceFor(inventory.getItem(OFFHAND_SLOT), stack)) return OFFHAND_SLOT;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (hasRemainingSpaceFor(inventory.getItem(i), stack)) return i;
        }
        return -1;
    }

    /** The first empty hotbar slot, or -1. Mirrors {@code Inventory#getFreeSlot} over the hotbar only. */
    public static int freeSlot(Inventory inventory) {
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (inventory.getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    private static boolean hasRemainingSpaceFor(ItemStack inSlot, ItemStack stack) {
        return !inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, stack)
            && inSlot.isStackable() && inSlot.getCount() < inSlot.getMaxStackSize();
    }

    // ---- the snapshot ----

    /** Zombified some other way ({@code /effect}); a totem's curse goes through {@link Curses#apply}, which calls {@link #remember}. */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.getEffectInstance().is(TotemRegistries.ZOMBIFIED) && event.getOldEffectInstance() == null) remember(player);
    }

    /** Remembers what {@code player} carries as they turn: one of each hotbar and offhand item. */
    static void remember(ServerPlayer player) {
        List<ItemStack> carried = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) addCopy(carried, inventory.getItem(i));
        addCopy(carried, inventory.getItem(OFFHAND_SLOT));
        setCarried(player, List.copyOf(carried));
    }

    private static void addCopy(List<ItemStack> carried, ItemStack stack) {
        if (!stack.isEmpty()) carried.add(stack.copyWithCount(1));
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        clearIfZombified(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        clearIfZombified(event.getEntity(), event.getEffectInstance());
    }

    private static void clearIfZombified(Object entity, MobEffectInstance instance) {
        if (entity instanceof ServerPlayer player && instance != null && instance.is(TotemRegistries.ZOMBIFIED)) {
            setCarried(player, List.of());
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    private static void setCarried(ServerPlayer player, List<ItemStack> carried) {
        player.setData(TotemRegistries.ZOMBIE_CARRIED, carried);
        sync(player);
    }

    private static void sync(ServerPlayer player) {
        TotemNetwork.sendTo(player, new ZombieCarriedPayload(player.getData(TotemRegistries.ZOMBIE_CARRIED)));
    }
}
