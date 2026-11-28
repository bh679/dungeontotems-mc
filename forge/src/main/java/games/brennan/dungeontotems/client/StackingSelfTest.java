package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Development only ({@code ./gradlew runSelfTest -Dstacking=true}): a real client picks up, shift-clicks and
 * cursor-clicks Totems of Rage (the clicks go to the server as a player's would) and checks that totems only
 * stack when you stack them ({@link games.brennan.dungeontotems.TotemStacking}). Each step is screenshotted
 * ({@code screenshots/selftest_stacking_*}) and reported to {@code selftest/stacking.txt}, then the client quits.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class StackingSelfTest {

    static final boolean ENABLED = !System.getProperty("dungeontotems.selftest.stacking", "").isEmpty();
    private static final int WARM_UP_TICKS = 60;
    /** Ticks per step: set up, act, check and screenshot. */
    private static final int STEP_TICKS = 40;
    private static final int ACT = 10;
    private static final int CHECK = 30;
    /** {@code InventoryMenu}: inventory slots 9..35 sit at the same menu index, the hotbar at 36..44. */
    private static final int MENU_HOTBAR = 36;
    private static final int CHEST_SLOTS = 27;

    private record Step(String name, Setup setup, Act act, Predicate<Inventory> expected) {}

    private interface Setup { void run(ServerPlayer player); }

    private interface Act { void run(Minecraft minecraft, ServerPlayer player); }

    private static final List<Step> STEPS = List.of(
        new Step("pickup takes its own slot", player -> put(player, 0, rage(1)),
            (minecraft, player) -> drop(player, rage(1)),
            inventory -> count(inventory, 0) == 1 && rages(inventory) == 2 && rageSlots(inventory) == 2),
        new Step("shift-click doesn't merge", player -> { put(player, 0, rage(1)); put(player, 9, rage(1)); },
            (minecraft, player) -> click(minecraft, MENU_HOTBAR, ClickType.QUICK_MOVE),
            inventory -> inventory.getItem(0).isEmpty() && count(inventory, 9) == 1 && rageSlots(inventory) == 2),
        new Step("cursor stacks them", player -> { put(player, 9, rage(1)); put(player, 10, rage(1)); },
            (minecraft, player) -> { click(minecraft, 10, ClickType.PICKUP); click(minecraft, 9, ClickType.PICKUP); },
            inventory -> count(inventory, 9) == 2 && inventory.getItem(10).isEmpty()),
        new Step("cursor fills to 3", player -> { put(player, 9, rage(2)); put(player, 10, rage(2)); },
            (minecraft, player) -> { click(minecraft, 10, ClickType.PICKUP); click(minecraft, 9, ClickType.PICKUP); click(minecraft, 10, ClickType.PICKUP); },
            inventory -> count(inventory, 9) == 3 && count(inventory, 10) == 1),
        new Step("full inventory pickup stacks", player -> { put(player, 9, rage(2)); fillWithDirt(player); },
            (minecraft, player) -> drop(player, rage(1)),
            inventory -> count(inventory, 9) == 3 && rageSlots(inventory) == 1),
        new Step("chest shift-click doesn't merge", player -> {
                put(player, 0, rage(1));
                SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
                chest.setItem(0, rage(1));
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.threeRows(id, inventory, chest),
                    Component.literal("Chest")));
            },
            (minecraft, player) -> click(minecraft, 0, ClickType.QUICK_MOVE),
            inventory -> count(inventory, 0) == 1 && rages(inventory) == 2 && rageSlots(inventory) == 2),
        new Step("dirt still stacks on pickup", player -> put(player, 0, new ItemStack(Items.DIRT)),
            (minecraft, player) -> drop(player, new ItemStack(Items.DIRT)),
            inventory -> inventory.getItem(0).is(Items.DIRT) && count(inventory, 0) == 2));

    private static final List<String> REPORT = new ArrayList<>();
    private static int tick;
    private static int failures;

    private StackingSelfTest() {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ENABLED) return;
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (minecraft.player == null || server == null) return;
        tick++;
        if (tick < WARM_UP_TICKS && minecraft.player.isDeadOrDying()) minecraft.player.respawn();
        ServerPlayer player = server.getPlayerList().getPlayer(minecraft.player.getUUID());
        if (player == null || tick < WARM_UP_TICKS) return;
        int t = tick - WARM_UP_TICKS;
        int index = t / STEP_TICKS;
        if (index >= STEPS.size()) {
            finish(minecraft);
            return;
        }
        Step step = STEPS.get(index);
        int at = t % STEP_TICKS;
        if (at == 0) {
            server.execute(() -> {
                player.closeContainer();
                player.setGameMode(GameType.SURVIVAL);
                player.getInventory().clearContent();
                step.setup().run(player);
                player.containerMenu.broadcastChanges();
            });
        }
        if (at == ACT / 2 && !(minecraft.screen != null && minecraft.player.containerMenu != minecraft.player.inventoryMenu)) {
            minecraft.setScreen(new InventoryScreen(minecraft.player));
        }
        if (at == ACT) step.act().run(minecraft, player);
        if (at == CHECK) {
            server.execute(() -> {
                boolean ok = step.expected().test(player.getInventory());
                if (!ok) failures++;
                REPORT.add(String.format("%s  %-34s %s", ok ? "PASS" : "FAIL", step.name(), describe(player.getInventory())));
            });
            Screenshot.grab(minecraft.gameDirectory, String.format("selftest_stacking_%02d.png", index + 1),
                minecraft.getMainRenderTarget(), message -> {});
        }
    }

    private static ItemStack rage(int count) {
        return new ItemStack(totem(), count);
    }

    private static Item totem() {
        return TotemRegistries.TOTEMS.get(Curse.RAGE).get();
    }

    private static void put(ServerPlayer player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    private static void fillWithDirt(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (inventory.getItem(i).isEmpty()) inventory.setItem(i, new ItemStack(Items.DIRT, 64));
        }
    }

    /** Drops a stack at the player's feet, ready to pick up, as a mob drop or a thrown item lands. */
    private static void drop(ServerPlayer player, ItemStack stack) {
        player.server.execute(() -> {
            ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            item.setNoPickUpDelay();
            player.level().addFreshEntity(item);
        });
    }

    /** A left click on a slot of the open menu, sent to the server as the player's own click. */
    private static void click(Minecraft minecraft, int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private static int count(Inventory inventory, int slot) {
        return inventory.getItem(slot).getCount();
    }

    private static int rages(Inventory inventory) {
        return inventory.countItem(totem());
    }

    private static int rageSlots(Inventory inventory) {
        int slots = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(totem())) slots++;
        }
        return slots;
    }

    private static String describe(Inventory inventory) {
        List<String> rows = new ArrayList<>();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(totem()) || (stack.is(Items.DIRT) && stack.getCount() < 64)) {
                rows.add("slot " + i + ": " + stack.getCount() + "x " + (stack.is(Items.DIRT) ? "dirt" : "rage"));
            }
        }
        return String.join(", ", rows);
    }

    private static void finish(Minecraft minecraft) {
        REPORT.add(failures == 0 ? "ALL PASSED" : failures + " FAILED");
        try {
            Path out = minecraft.gameDirectory.toPath().resolve("selftest");
            Files.createDirectories(out);
            Files.write(out.resolve("stacking.txt"), REPORT);
        } catch (IOException e) {
            DungeonTotems.LOGGER.error("Stacking self-test report couldn't be written", e);
        }
        DungeonTotems.LOGGER.info("Stacking self-test done:\n{}", String.join("\n", REPORT));
        minecraft.stop();
    }
}
