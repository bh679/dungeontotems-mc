package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

/**
 * Stackable totems only stack when you stack them ({@link games.brennan.dungeontotems.TotemStacking}):
 * pickups and shift-clicks take their own slot, the cursor stacks them.
 */
public final class StackingGameTests {

    private static final String EMPTY = "empty";
    /** {@code InventoryMenu}: inventory slots 9..35 sit at the same menu index, the hotbar at 36..44. */
    private static final int MENU_HOTBAR = 36;
    private static final int CHEST_SLOTS = 27;

    public StackingGameTests() {} // public: Fabric instantiates its gametest entrypoints

    @GameTest(template = EMPTY)
    public static void aPickedUpTotemTakesItsOwnSlot(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(0, rage(1));
        Vec3 at = player.position();
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, rage(1));
        item.setNoPickUpDelay();
        helper.getLevel().addFreshEntity(item);
        item.playerTouch(player);
        check(helper, item.isRemoved(), "the totem should have been picked up");
        check(helper, player.getInventory().getItem(0).getCount() == 1, "the held totem should stay one, was "
                + player.getInventory().getItem(0).getCount());
        check(helper, totemsIn(player.getInventory()) == 2 && slotsWithTotems(player.getInventory()) == 2,
                "the picked-up totem should sit in a slot of its own");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aFullInventoryStacksThePickup(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        fillWithDirt(inventory);
        inventory.setItem(5, rage(2));
        ItemStack picked = rage(2);
        inventory.add(picked); // false: vanilla reports a partial add as not added
        check(helper, inventory.getItem(5).getCount() == 3, "the stack should fill to 3, was " + inventory.getItem(5).getCount());
        check(helper, picked.getCount() == 1, "the one that doesn't fit should be left over, left " + picked.getCount());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void otherItemsStillStackOnPickup(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.DIRT));
        inventory.add(new ItemStack(Items.DIRT));
        check(helper, inventory.getItem(0).getCount() == 2, "dirt should stack as vanilla");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void shiftClickKeepsTotemsApart(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, rage(1));
        inventory.setItem(9, rage(1));
        player.inventoryMenu.clicked(MENU_HOTBAR, 0, ClickType.QUICK_MOVE, player);
        check(helper, inventory.getItem(0).isEmpty(), "the shift-clicked totem should leave the hotbar");
        check(helper, inventory.getItem(9).getCount() == 1, "the shift-clicked totem should not join the stack");
        check(helper, totemsIn(inventory) == 2 && slotsWithTotems(inventory) == 2, "it should take a slot of its own");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void shiftClickFromAChestKeepsTotemsApart(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, rage(1));
        SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
        chest.setItem(0, rage(1));
        ChestMenu menu = ChestMenu.threeRows(1, inventory, chest);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        check(helper, chest.getItem(0).isEmpty(), "the totem should leave the chest");
        check(helper, inventory.getItem(0).getCount() == 1, "it should not join the hotbar stack");
        check(helper, totemsIn(inventory) == 2 && slotsWithTotems(inventory) == 2, "it should take a slot of its own");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void theCursorStacksTotems(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        AbstractContainerMenu menu = player.inventoryMenu;
        inventory.setItem(9, rage(1));
        menu.setCarried(rage(1));
        menu.clicked(9, 0, ClickType.PICKUP, player);
        check(helper, inventory.getItem(9).getCount() == 2 && menu.getCarried().isEmpty(), "placing one on another should stack them");
        menu.setCarried(rage(2));
        menu.clicked(9, 0, ClickType.PICKUP, player);
        check(helper, inventory.getItem(9).getCount() == 3, "the stack should fill to 3, was " + inventory.getItem(9).getCount());
        check(helper, menu.getCarried().getCount() == 1, "the one that doesn't fit stays on the cursor");
        helper.succeed();
    }

    private static ItemStack rage(int count) {
        return new ItemStack(TotemRegistries.TOTEMS.get(Curse.RAGE).get(), count);
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.teleportTo(at.x, at.y, at.z);
        player.getInventory().clearContent();
        return player;
    }

    private static void fillWithDirt(Inventory inventory) {
        for (int i = 0; i < inventory.items.size(); i++) inventory.setItem(i, new ItemStack(Items.DIRT));
    }

    private static int totemsIn(Inventory inventory) {
        return inventory.countItem(TotemRegistries.TOTEMS.get(Curse.RAGE).get());
    }

    private static int slotsWithTotems(Inventory inventory) {
        int slots = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(TotemRegistries.TOTEMS.get(Curse.RAGE).get())) slots++;
        }
        return slots;
    }

    private static void check(GameTestHelper helper, boolean ok, String message) {
        if (!ok) helper.fail(message);
    }
}
