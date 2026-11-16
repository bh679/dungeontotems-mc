package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.Curses;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Development only ({@code ./gradlew runSelfTest -Dzombie=true}): a real client lives as a Zombified player
 * ({@link games.brennan.dungeontotems.curse.ZombiePack}, {@code ZombieDiet}, {@code ZombieTrade}): clicks at the
 * locked pack, puts away what it carried, picks up, eats bread, tries to trade and meets a pillager. The clicks
 * and uses go to the server as a player's would. Each step is screenshotted
 * ({@code screenshots/selftest_zombie_*}) and reported to {@code selftest/zombie.txt}, then the client quits.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class ZombieSelfTest {

    static final boolean ENABLED = !System.getProperty("dungeontotems.selftest.zombie", "").isEmpty();
    private static final int WARM_UP_TICKS = 60;
    private static final int STEP_TICKS = 60;
    private static final int ACT = 10;
    private static final int CHECK = 50;
    private static final long MIDNIGHT = 18000L;
    /** {@code InventoryMenu}: inventory slots 9..35 sit at the same menu index, the hotbar at 36..44. */
    private static final int MENU_HOTBAR = 36;
    private static final int CHEST_SLOTS = 27;
    /** The hotbar name shows for 2 s and fades in its last half second. */
    private static final int HOTBAR_NAME_LEAD = 15;

    private record Step(String name, boolean inventoryOpen, int hover, int actAt, Setup setup, Act act, Predicate<ServerPlayer> expected) {
        Step(String name, boolean inventoryOpen, Setup setup, Act act, Predicate<ServerPlayer> expected) {
            this(name, inventoryOpen, NO_HOVER, setup, act, expected);
        }

        Step(String name, boolean inventoryOpen, int hover, Setup setup, Act act, Predicate<ServerPlayer> expected) {
            this(name, inventoryOpen, hover, ACT, setup, act, expected);
        }
    }

    private static final int NO_HOVER = -1;

    private interface Setup { void run(ServerPlayer player); }

    private interface Act { void run(Minecraft minecraft, ServerPlayer player); }

    private static final List<Step> STEPS = List.of(
        new Step("pack is greyed out, the sword reads as gibberish", true, 14, ZombieSelfTest::packed, (minecraft, player) -> {},
            player -> player.getInventory().getItem(10).is(Items.DIRT)),
        new Step("the hotbar arrows read as gibberish", true, MENU_HOTBAR, ZombieSelfTest::packed, (minecraft, player) -> {},
            player -> player.getInventory().getItem(0).is(Items.ARROW)),
        new Step("can't take from the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> click(minecraft, 10, ClickType.PICKUP),
            player -> player.getInventory().getItem(10).getCount() == 8 && player.containerMenu.getCarried().isEmpty()),
        new Step("can't shift-click out of the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> click(minecraft, 10, ClickType.QUICK_MOVE),
            player -> player.getInventory().getItem(10).getCount() == 8),
        new Step("carried arrows go into the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> { click(minecraft, MENU_HOTBAR, ClickType.PICKUP); click(minecraft, 11, ClickType.PICKUP); },
            player -> player.getInventory().getItem(11).is(Items.ARROW) && player.getInventory().getItem(0).isEmpty()),
        new Step("new bread stays out of the pack", true, player -> { packed(player); put(player, 1, new ItemStack(Items.BREAD, 4)); },
            (minecraft, player) -> {
                click(minecraft, MENU_HOTBAR + 1, ClickType.PICKUP);
                click(minecraft, 12, ClickType.PICKUP);
                click(minecraft, MENU_HOTBAR + 1, ClickType.PICKUP);
            },
            player -> player.getInventory().getItem(12).isEmpty() && player.getInventory().getItem(1).is(Items.BREAD)),
        new Step("pickup with a full hotbar stays on the ground", false, player -> {
                packed(player);
                for (int i = 0; i < Inventory.getSelectionSize(); i++) put(player, i, new ItemStack(Items.STONE));
            },
            (minecraft, player) -> drop(player, new ItemStack(Items.EMERALD, 3)),
            player -> player.getInventory().countItem(Items.EMERALD) == 0),
        new Step("chest shift-click lands in the hotbar", false, player -> {
                packed(player);
                SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
                chest.setItem(0, new ItemStack(Items.DIRT, 8));
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.threeRows(id, inventory, chest),
                    Component.literal("Chest")));
            },
            (minecraft, player) -> click(minecraft, 0, ClickType.QUICK_MOVE),
            player -> player.getInventory().getItem(10).getCount() == 8 && hotbarHas(player, Items.DIRT)),
        new Step("bread is refused", false, player -> { packed(player); hungry(player); put(player, 0, new ItemStack(Items.BREAD)); },
            (minecraft, player) -> eat(minecraft),
            player -> !player.isUsingItem() && player.getInventory().getItem(0).getCount() == 1),
        new Step("rotten flesh is eaten", false, player -> { packed(player); hungry(player); put(player, 0, new ItemStack(Items.ROTTEN_FLESH)); },
            (minecraft, player) -> eat(minecraft),
            player -> player.getInventory().getItem(0).isEmpty() && !player.hasEffect(MobEffects.HUNGER)),
        new Step("a villager turns", false, player -> {
                packed(player);
                Villager villager = EntityType.VILLAGER.create(player.serverLevel());
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.FARMER));
                villager.moveTo(player.getX(), player.getY(), player.getZ() + 2, 180.0F, 0.0F);
                villager.setNoAi(true);
                player.serverLevel().addFreshEntity(villager);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, villager.getEyePosition());
            },
            (minecraft, player) -> {
                List<Villager> near = player.level().getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(4));
                if (!near.isEmpty()) minecraft.gameMode.interact(minecraft.player, minecraft.player.level().getEntity(near.get(0).getId()), InteractionHand.MAIN_HAND);
            },
            player -> !player.level().getEntitiesOfClass(ZombieVillager.class, player.getBoundingBox().inflate(4)).isEmpty()),
        new Step("a pillager comes for you", false, player -> {
                packed(player);
                Mob pillager = EntityType.PILLAGER.create(player.serverLevel());
                pillager.moveTo(player.getX() + 3, player.getY(), player.getZ() + 3, 0.0F, 0.0F);
                player.serverLevel().addFreshEntity(pillager);
            },
            (minecraft, player) -> {},
            player -> player.level().getEntitiesOfClass(Mob.class, near(player), m -> m.getType() == EntityType.PILLAGER)
                .stream().anyMatch(m -> m.getTarget() == player)),
        new Step("a shulker box won't place", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.SHULKER_BOX));
                lookAtGroundAhead(player);
            },
            (minecraft, player) -> eat(minecraft), // holds use
            player -> player.getInventory().getItem(0).is(Items.SHULKER_BOX) && !shulkerNear(player)),
        new Step("a shulker box won't break", false, player -> {
                packed(player);
                player.serverLevel().setBlockAndUpdate(ahead(player), Blocks.SHULKER_BOX.defaultBlockState());
                player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(ahead(player)));
            },
            (minecraft, player) -> minecraft.options.keyAttack.setDown(true),
            ZombieSelfTest::shulkerNear),
        new Step("a wolf runs from a zombie's bone", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.BONE, 16));
                Wolf wolf = EntityType.WOLF.create(player.serverLevel());
                wolf.moveTo(player.getX(), player.getY(), player.getZ() + 2, 180.0F, 0.0F);
                player.serverLevel().addFreshEntity(wolf);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, wolf.getEyePosition());
            },
            (minecraft, player) -> eat(minecraft), // holds use: a bone every few ticks
            player -> player.getInventory().getItem(0).getCount() == 16
                && player.level().getEntitiesOfClass(Wolf.class, near(player)).stream().noneMatch(Wolf::isTame)),
        new Step("an enchanted sword mumbles", true, MENU_HOTBAR + 1, player -> { packed(player); put(player, 1, enchantedSword(player)); },
            (minecraft, player) -> {}, player -> player.getInventory().getItem(1).isEnchanted()),
        new Step("a lucid item thinks", true, MENU_HOTBAR + 2, player -> { packed(player); put(player, 2, thoughtfulBlade()); },
            (minecraft, player) -> {}, player -> ZombieGibberish.thought(ItemStack.hashItemAndComponents(player.getInventory().getItem(2))) != null),
        new Step("the hotbar name mumbles", false, NO_HOVER, CHECK - HOTBAR_NAME_LEAD,
            player -> { packed(player); put(player, 1, enchantedSword(player)); },
            (minecraft, player) -> minecraft.player.getInventory().selected = 1, // client side: the name pops up over the hotbar
            player -> player.getInventory().getItem(1).isEnchanted()));

    private static final List<String> REPORT = new ArrayList<>();
    private static int tick;
    private static int failures;
    private static boolean worldRequested;

    private ZombieSelfTest() {}

    /** A fresh flat world, so the test needs no saved one ({@code --quickPlaySingleplayer} only opens an existing world). */
    private static void createWorld(Minecraft minecraft) {
        if (!(minecraft.screen instanceof TitleScreen || minecraft.screen instanceof CreateWorldScreen)) return;
        worldRequested = true;
        LevelSettings settings = new LevelSettings("Zombie Self Test " + System.currentTimeMillis(), GameType.SURVIVAL, false,
            Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(settings.levelName(), settings, WorldOptions.defaultWithRandomSeed(),
            registries -> registries.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
            new TitleScreen());
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!ENABLED) return;
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft.getSingleplayerServer();
        if (minecraft.player == null && server == null && !worldRequested) createWorld(minecraft);
        if (minecraft.player == null || server == null) return;
        minecraft.options.pauseOnLostFocus = false; // an unfocused window would pause the server for good
        if (minecraft.screen instanceof PauseScreen) minecraft.setScreen(null);
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
            minecraft.options.keyUse.setDown(false);
            minecraft.options.keyAttack.setDown(false);
            if (minecraft.screen != null) minecraft.setScreen(null);
            server.execute(() -> {
                player.closeContainer();
                clearMobs(player);
                player.serverLevel().setBlockAndUpdate(ahead(player), Blocks.AIR.defaultBlockState());
                server.setDifficulty(Difficulty.NORMAL, true);
                player.serverLevel().setDayTime(MIDNIGHT); // no sunburn
                player.setGameMode(GameType.SURVIVAL);
                player.removeEffect(TotemRegistries.ZOMBIFIED);
                player.getInventory().clearContent();
                player.getInventory().selected = 0;
                step.setup().run(player);
                player.containerMenu.broadcastChanges();
            });
        }
        if (at == ACT / 2 && step.inventoryOpen()) minecraft.setScreen(new InventoryScreen(minecraft.player));
        if (at == step.actAt()) step.act().run(minecraft, player);
        if (at > ACT && step.hover() != NO_HOVER) hover(minecraft, step.hover());
        if (at == CHECK) {
            server.execute(() -> {
                boolean ok = step.expected().test(player);
                if (!ok) failures++;
                REPORT.add(String.format("%s  %-48s held %s, using %s, hunger %s, food %d", ok ? "PASS" : "FAIL", step.name(),
                    player.getInventory().getItem(0), player.isUsingItem(), player.hasEffect(MobEffects.HUNGER),
                    player.getFoodData().getFoodLevel()));
            });
            Screenshot.grab(minecraft.gameDirectory, String.format("selftest_zombie_%02d.png", index + 1),
                minecraft.getMainRenderTarget(), message -> {});
        }
    }

    /** Zombified while carrying 16 arrows in the hotbar, with dirt and a sword already in the pack. */
    private static void packed(ServerPlayer player) {
        put(player, 0, new ItemStack(Items.ARROW, 16));
        put(player, 10, new ItemStack(Items.DIRT, 8));
        put(player, 14, new ItemStack(Items.IRON_SWORD));
        put(player, 20, new ItemStack(Items.COOKED_BEEF, 12));
        Curses.apply(player, Curse.ZOMBIFIED, 0, MobEffectInstance.INFINITE_DURATION);
    }

    /** A diamond sword with Sharpness V, Unbreaking III and Looting III: a long tooltip in rare colour. */
    private static ItemStack enchantedSword(ServerPlayer player) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        var enchantments = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        sword.enchant(enchantments.getOrThrow(Enchantments.SHARPNESS), 5);
        sword.enchant(enchantments.getOrThrow(Enchantments.UNBREAKING), 3);
        sword.enchant(enchantments.getOrThrow(Enchantments.LOOTING), 3);
        return sword;
    }

    /** A named blade that happens to be one of the 1 in 500 items that stir a thought. */
    private static ItemStack thoughtfulBlade() {
        for (int n = 0; ; n++) {
            ItemStack blade = new ItemStack(Items.IRON_SWORD);
            blade.set(DataComponents.CUSTOM_NAME, Component.literal("Old Blade " + n));
            if (ZombieGibberish.thought(ItemStack.hashItemAndComponents(blade)) != null) return blade;
        }
    }

    /** Holds the use key down, as a player eating does, until the next step starts. */
    private static void eat(Minecraft minecraft) {
        minecraft.options.keyUse.setDown(true);
    }

    /** The ground block two ahead of the player, on the +z side (the way the test faces things). */
    private static BlockPos ahead(ServerPlayer player) {
        return player.blockPosition().offset(0, 0, 2);
    }

    private static void lookAtGroundAhead(ServerPlayer player) {
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(ahead(player).below()).add(0.0, 0.5, 0.0));
    }

    private static boolean shulkerNear(ServerPlayer player) {
        return BlockPos.betweenClosedStream(player.blockPosition().offset(-3, -2, -3), player.blockPosition().offset(3, 2, 3))
            .anyMatch(pos -> player.level().getBlockState(pos).is(Blocks.SHULKER_BOX));
    }

    private static void hungry(ServerPlayer player) {
        player.getFoodData().setFoodLevel(10);
    }

    private static void put(ServerPlayer player, int slot, ItemStack stack) {
        player.getInventory().setItem(slot, stack);
    }

    private static boolean hotbarHas(ServerPlayer player, Item item) {
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) return true;
        }
        return false;
    }

    private static AABB near(ServerPlayer player) {
        return player.getBoundingBox().inflate(16);
    }

    private static void clearMobs(ServerPlayer player) {
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, near(player))) mob.discard();
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, near(player))) item.discard();
    }

    /** Drops a stack at the player's feet, ready to pick up. */
    private static void drop(ServerPlayer player, ItemStack stack) {
        player.server.execute(() -> {
            ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            item.setNoPickUpDelay();
            player.level().addFreshEntity(item);
        });
    }

    /** Puts the mouse over a slot of the open menu, so its tooltip shows (dev mappings: {@code MouseHandler} fields). */
    private static void hover(Minecraft minecraft, int slotIndex) {
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)) return;
        Slot slot = screen.getMenu().getSlot(slotIndex);
        double scale = (double) minecraft.getWindow().getScreenWidth() / minecraft.getWindow().getGuiScaledWidth();
        try {
            Field x = MouseHandler.class.getDeclaredField("xpos");
            Field y = MouseHandler.class.getDeclaredField("ypos");
            x.setAccessible(true);
            y.setAccessible(true);
            x.setDouble(minecraft.mouseHandler, (screen.getGuiLeft() + slot.x + 8) * scale);
            y.setDouble(minecraft.mouseHandler, (screen.getGuiTop() + slot.y + 8) * scale);
        } catch (ReflectiveOperationException e) {
            DungeonTotems.LOGGER.warn("Zombie self-test couldn't move the mouse", e);
        }
    }

    /** A left click on a slot of the open menu, sent to the server as the player's own click. */
    private static void click(Minecraft minecraft, int slot, ClickType type) {
        minecraft.gameMode.handleInventoryMouseClick(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
    }

    private static void finish(Minecraft minecraft) {
        REPORT.add(failures == 0 ? "ALL PASSED" : failures + " FAILED");
        try {
            Path out = minecraft.gameDirectory.toPath().resolve("selftest");
            Files.createDirectories(out);
            Files.write(out.resolve("zombie.txt"), REPORT);
        } catch (IOException e) {
            DungeonTotems.LOGGER.error("Zombie self-test report couldn't be written", e);
        }
        DungeonTotems.LOGGER.info("Zombie self-test done:\n{}", String.join("\n", REPORT));
        minecraft.stop();
    }
}
