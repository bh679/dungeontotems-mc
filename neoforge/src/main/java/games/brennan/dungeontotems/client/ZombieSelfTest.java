package games.brennan.dungeontotems.client;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.entity.EntitySpawnReason;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.Curses;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import games.brennan.dungeontotems.mixin.client.BookEditScreenAccess;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
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
    private static final String TYPED = "hello there my good friend how are you today ";
    /** What a typing step left in its box, read on the client and checked on the server. */
    private static volatile String written = "";
    /** First-person draw counts when a hand step was set up, to compare against once it has been seen. */
    private static volatile int itemsAtSetup;
    private static volatile int armsAtSetup;

    private interface Setup { void run(ServerPlayer player); }

    private interface Act { void run(Minecraft minecraft, ServerPlayer player); }

    private static final List<Step> STEPS = List.of(
        new Step("pack is greyed out, the sword reads as gibberish", true, 14, ZombieSelfTest::packed, (minecraft, player) -> {},
            player -> player.getInventory().getItem(10).is(Items.DIRT)),
        new Step("the hotbar arrows read as gibberish", true, MENU_HOTBAR, ZombieSelfTest::packed, (minecraft, player) -> {},
            player -> player.getInventory().getItem(0).is(Items.ARROW)),
        new Step("can't take from the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> click(minecraft, 10, ContainerInput.PICKUP),
            player -> player.getInventory().getItem(10).getCount() == 8 && player.containerMenu.getCarried().isEmpty()),
        new Step("can't shift-click out of the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> click(minecraft, 10, ContainerInput.QUICK_MOVE),
            player -> player.getInventory().getItem(10).getCount() == 8),
        new Step("Weakness potions and totem stay clear in the pack", true, 15, ZombieSelfTest::weaknessPacked, (minecraft, player) -> {},
            player -> player.getInventory().getItem(15).is(Items.POTION)),
        new Step("a Weakness potion comes out of the pack", true, ZombieSelfTest::weaknessPacked,
            (minecraft, player) -> { click(minecraft, 15, ContainerInput.PICKUP); click(minecraft, MENU_HOTBAR + 2, ContainerInput.PICKUP); },
            player -> player.getInventory().getItem(15).isEmpty() && ZombieGibberish.weakens(player.getInventory().getItem(2))),
        new Step("a Totem of Weakness shift-clicks out of the pack", true, ZombieSelfTest::weaknessPacked,
            (minecraft, player) -> click(minecraft, 17, ContainerInput.QUICK_MOVE),
            player -> player.getInventory().getItem(17).isEmpty() && hotbarHas(player, TotemRegistries.TOTEMS.get(Curse.WEAKNESS).get())),
        new Step("a healing potion stays in the pack", true, ZombieSelfTest::weaknessPacked,
            (minecraft, player) -> click(minecraft, 18, ContainerInput.QUICK_MOVE),
            player -> player.getInventory().getItem(18).is(Items.POTION)),
        new Step("carried arrows go into the pack", true, ZombieSelfTest::packed,
            (minecraft, player) -> { click(minecraft, MENU_HOTBAR, ContainerInput.PICKUP); click(minecraft, 11, ContainerInput.PICKUP); },
            player -> player.getInventory().getItem(11).is(Items.ARROW) && player.getInventory().getItem(0).isEmpty()),
        new Step("new bread stays out of the pack", true, player -> { packed(player); put(player, 1, new ItemStack(Items.BREAD, 4)); },
            (minecraft, player) -> {
                click(minecraft, MENU_HOTBAR + 1, ContainerInput.PICKUP);
                click(minecraft, 12, ContainerInput.PICKUP);
                click(minecraft, MENU_HOTBAR + 1, ContainerInput.PICKUP);
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
            (minecraft, player) -> click(minecraft, 0, ContainerInput.QUICK_MOVE),
            player -> player.getInventory().getItem(10).getCount() == 8 && hotbarHas(player, Items.DIRT)),
        new Step("carried arrows from a chest skip the pack's arrows", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.ARROW, 64));
                put(player, 11, new ItemStack(Items.ARROW, 8));
                SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
                chest.setItem(0, new ItemStack(Items.ARROW, 20));
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.threeRows(id, inventory, chest),
                    Component.literal("Chest")));
            },
            (minecraft, player) -> click(minecraft, 0, ContainerInput.QUICK_MOVE),
            player -> player.getInventory().getItem(11).getCount() == 8 && player.getInventory().countItem(Items.ARROW) == 92),
        new Step("bread is refused", false, player -> { packed(player); hungry(player); put(player, 0, new ItemStack(Items.BREAD)); },
            (minecraft, player) -> eat(minecraft),
            player -> !player.isUsingItem() && player.getInventory().getItem(0).getCount() == 1),
        new Step("rotten flesh is eaten", false, player -> { packed(player); hungry(player); put(player, 0, new ItemStack(Items.ROTTEN_FLESH)); },
            (minecraft, player) -> eat(minecraft),
            player -> player.getInventory().getItem(0).isEmpty() && !player.hasEffect(MobEffects.HUNGER)),
        new Step("a villager turns", false, player -> {
                packed(player);
                Villager villager = EntityTypes.VILLAGER.create(player.level(), EntitySpawnReason.LOAD);
                villager.setVillagerData(villager.getVillagerData().withProfession(villager.level().registryAccess(), VillagerProfession.FARMER));
                villager.snapTo(player.getX(), player.getY(), player.getZ() + 2, 180.0F, 0.0F);
                villager.setNoAi(true);
                player.level().addFreshEntity(villager);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, villager.getEyePosition());
            },
            (minecraft, player) -> {
                List<Villager> near = player.level().getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(4));
                if (!near.isEmpty()) interactWith(minecraft, minecraft.player.level().getEntity(near.get(0).getId()));
            },
            player -> !player.level().getEntitiesOfClass(ZombieVillager.class, player.getBoundingBox().inflate(4)).isEmpty()),
        new Step("a pillager comes for you", false, player -> {
                packed(player);
                Mob pillager = EntityTypes.PILLAGER.create(player.level(), EntitySpawnReason.LOAD);
                pillager.snapTo(player.getX() + 3, player.getY(), player.getZ() + 3, 0.0F, 0.0F);
                player.level().addFreshEntity(pillager);
            },
            (minecraft, player) -> {},
            player -> player.level().getEntitiesOfClass(Mob.class, near(player), m -> m.getType() == EntityTypes.PILLAGER)
                .stream().anyMatch(m -> m.getTarget() == player)),
        new Step("a shulker box won't place", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.SHULKER_BOX));
                lookAtGroundAhead(player);
            },
            (minecraft, player) -> eat(minecraft), // holds use
            player -> player.getInventory().getItem(0).is(Items.SHULKER_BOX) && !shulkerNear(player) && noScreen(player)),
        new Step("a held shulker box won't open (Advanced Shulkerboxes)", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.SHULKER_BOX));
                player.lookAt(EntityAnchorArgument.Anchor.EYES, player.getEyePosition().add(0.0, 10.0, 4.0)); // the sky: the item is used
            },
            (minecraft, player) -> eat(minecraft), // holds use
            player -> player.getInventory().getItem(0).is(Items.SHULKER_BOX) && noScreen(player)),
        new Step("a shulker box won't break", false, player -> {
                packed(player);
                player.level().setBlockAndUpdate(ahead(player), Blocks.SHULKER_BOX.defaultBlockState());
                player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(ahead(player)));
            },
            (minecraft, player) -> minecraft.options.keyAttack.setDown(true),
            ZombieSelfTest::shulkerNear),
        new Step("a wolf runs from a zombie's bone", false, player -> {
                packed(player);
                put(player, 0, new ItemStack(Items.BONE, 16));
                Wolf wolf = EntityTypes.WOLF.create(player.level(), EntitySpawnReason.LOAD);
                wolf.snapTo(player.getX(), player.getY(), player.getZ() + 2, 180.0F, 0.0F);
                player.level().addFreshEntity(wolf);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, wolf.getEyePosition());
            },
            (minecraft, player) -> eat(minecraft), // holds use: a bone every few ticks
            player -> player.getInventory().getItem(0).getCount() == 16
                && player.level().getEntitiesOfClass(Wolf.class, near(player)).stream().noneMatch(Wolf::isTame)),
        new Step("an enchanted sword mumbles", true, MENU_HOTBAR + 1, player -> { packed(player); put(player, 1, enchantedSword(player)); },
            (minecraft, player) -> {}, player -> player.getInventory().getItem(1).isEnchanted()),
        new Step("a weakness potion gives itself away", true, MENU_HOTBAR + 3, player -> {
                packed(player);
                put(player, 3, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS));
            },
            (minecraft, player) -> {}, player -> ZombieGibberish.weakens(player.getInventory().getItem(3))),
        new Step("a lucid item thinks", true, MENU_HOTBAR + 2, player -> { packed(player); put(player, 2, thoughtfulBlade()); },
            (minecraft, player) -> {}, player -> ZombieGibberish.thought(ItemStack.hashItemAndComponents(player.getInventory().getItem(2))) != null),
        new Step("the hotbar name mumbles", false, NO_HOVER, CHECK - HOTBAR_NAME_LEAD,
            player -> { packed(player); put(player, 1, enchantedSword(player)); },
            (minecraft, player) -> minecraft.player.getInventory().setSelectedSlot(1), // client side: the name pops up over the hotbar
            player -> player.getInventory().getItem(1).isEnchanted()),
        new Step("a book reads as gibberish", false, NO_HOVER, ACT, ZombieSelfTest::packed,
            (minecraft, player) -> {
                BookViewScreen.BookAccess book = BookViewScreen.BookAccess.fromItem(storyBook());
                if (book != null) minecraft.gui.setScreen(new BookViewScreen(book));
            },
            player -> true),
        new Step("chat reads as gibberish", false, NO_HOVER, ACT, player -> {
                packed(player);
                player.sendSystemMessage(Component.literal("The train leaves at dawn. Do not be late, and bring bread."));
                // A player's own chat line: 1 word in 3 of it reads.
                player.level().getServer().getPlayerList().broadcastChatMessage(
                    PlayerChatMessage.unsigned(player.getUUID(), "Has anyone seen my lost cat near the third carriage? She is grey and very fluffy."),
                    player, ChatType.bind(ChatType.CHAT, player));
            },
            (minecraft, player) -> minecraft.gui.setScreen(new ChatScreen("", false)),
            player -> true),
        new Step("typing in chat mumbles", false, NO_HOVER, ACT, ZombieSelfTest::packed,
            (minecraft, player) -> {
                ChatScreen chat = new ChatScreen("", false);
                minecraft.gui.setScreen(chat);
                type(chat, TYPED);
                written = chat.getFocused() instanceof EditBox input ? input.getValue() : "";
            },
            player -> !written.equals(TYPED) && !written.isBlank()),
        new Step("writing in a book mumbles", false, NO_HOVER, ACT, ZombieSelfTest::packed,
            (minecraft, player) -> {
                BookEditScreen book = new BookEditScreen(minecraft.player, new ItemStack(Items.WRITABLE_BOOK), InteractionHand.MAIN_HAND, WritableBookContent.EMPTY);
                minecraft.gui.setScreen(book);
                type(book, TYPED);
                written = ((BookEditScreenAccess) book).dungeontotems$pageText();
            },
            player -> !written.equals(TYPED) && !written.isBlank()),
        new Step("a held sword and shield show in first person", false, player -> {
                packed(player);
                put(player, 1, new ItemStack(Items.IRON_SWORD));
                player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                itemsAtSetup = FormHands.itemsDrawn();
            },
            (minecraft, player) -> {}, player -> FormHands.itemsDrawn() > itemsAtSetup),
        new Step("an empty hand shows the zombie's arm", false, player -> {
                packed(player);
                put(player, 1, ItemStack.EMPTY);
                player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                armsAtSetup = FormArms.drawn();
            },
            (minecraft, player) -> {}, player -> FormArms.drawn() > armsAtSetup),
        new Step("Jade mumbles about a chest (with -PwithJade)", false, player -> {
                packed(player);
                BlockPos at = ahead(player);
                player.level().setBlockAndUpdate(at, Blocks.CHEST.defaultBlockState());
                if (player.level().getBlockEntity(at) instanceof ChestBlockEntity chest) {
                    chest.setItem(0, new ItemStack(Items.DIAMOND, 5));
                    chest.setItem(1, new ItemStack(Items.ROTTEN_FLESH, 12));
                }
                player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(at));
            },
            (minecraft, player) -> {}, player -> player.level().getBlockState(ahead(player)).is(Blocks.CHEST)),
        new Step("Jade mumbles about a cow (with -PwithJade)", false, player -> {
                packed(player);
                Mob cow = EntityTypes.COW.create(player.level(), EntitySpawnReason.LOAD);
                cow.snapTo(player.getX(), player.getY(), player.getZ() + 2, 180.0F, 0.0F);
                cow.setNoAi(true);
                player.level().addFreshEntity(cow);
                player.lookAt(EntityAnchorArgument.Anchor.EYES, cow.getEyePosition());
            },
            (minecraft, player) -> {}, player -> true));

    private static final List<String> REPORT = new ArrayList<>();
    private static int tick;
    private static int failures;
    private static boolean worldRequested;

    private ZombieSelfTest() {}

    /** A fresh flat world, so the test needs no saved one ({@code --quickPlaySingleplayer} only opens an existing world). */
    private static void createWorld(Minecraft minecraft) {
        if (!(minecraft.gui.screen() instanceof TitleScreen || minecraft.gui.screen() instanceof CreateWorldScreen)) return;
        worldRequested = true;
        LevelSettings settings = new LevelSettings("Zombie Self Test " + System.currentTimeMillis(), GameType.SURVIVAL,
            new LevelSettings.DifficultySettings(Difficulty.NORMAL, false, false), true, WorldDataConfiguration.DEFAULT);
        minecraft.createWorldOpenFlows().createFreshLevel(settings.levelName(), settings, WorldOptions.defaultWithRandomSeed(),
            registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
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
        if (minecraft.gui.screen() instanceof PauseScreen) minecraft.gui.setScreen(null);
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
            if (minecraft.gui.screen() != null) minecraft.gui.setScreen(null);
            server.execute(() -> {
                player.closeContainer();
                clearMobs(player);
                player.level().setBlockAndUpdate(ahead(player), Blocks.AIR.defaultBlockState());
                server.setDifficulty(Difficulty.NORMAL, true);
                player.level().getServer().clockManager().setTotalTicks(player.level().registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD).orElseThrow(), MIDNIGHT); // no sunburn
                player.setGameMode(GameType.SURVIVAL);
                player.removeEffect(TotemRegistries.ZOMBIFIED);
                player.getInventory().clearContent();
                player.getInventory().setSelectedSlot(0);
                step.setup().run(player);
                player.containerMenu.broadcastChanges();
            });
        }
        if (at == ACT / 2 && step.inventoryOpen()) minecraft.gui.setScreen(new InventoryScreen(minecraft.player));
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
            Screenshot.grab(minecraft.gameDirectory, String.format("selftest_zombie_%02d.png", index + 1), minecraft.gameRenderer.mainRenderTarget(), 1, message -> {});
        }
    }

    /** Zombified while carrying 16 arrows in the hotbar, with dirt and a sword already in the pack. */
    /** No container screen open on the server: a shulker box (Advanced Shulkerboxes' too) never got to open. */
    private static boolean noScreen(ServerPlayer player) {
        return player.containerMenu == player.inventoryMenu;
    }

    private static void packed(ServerPlayer player) {
        put(player, 0, new ItemStack(Items.ARROW, 16));
        put(player, 10, new ItemStack(Items.DIRT, 8));
        put(player, 14, new ItemStack(Items.IRON_SWORD));
        put(player, 20, new ItemStack(Items.COOKED_BEEF, 12));
        Curses.apply(player, Curse.ZOMBIFIED, 0, MobEffectInstance.INFINITE_DURATION);
    }

    /** {@link #packed}, plus the cure's Weakness in the pack (15..17) and a healing potion beside it (18). */
    private static void weaknessPacked(ServerPlayer player) {
        packed(player);
        put(player, 15, PotionContents.createItemStack(Items.POTION, Potions.WEAKNESS));
        put(player, 16, PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WEAKNESS));
        put(player, 17, new ItemStack(TotemRegistries.TOTEMS.get(Curse.WEAKNESS).get()));
        put(player, 18, PotionContents.createItemStack(Items.POTION, Potions.HEALING));
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

    /** Right-clicks {@code target} as the player's own use key does. */
    private static void interactWith(Minecraft minecraft, net.minecraft.world.entity.Entity target) {
        if (target == null) return;
        net.minecraft.world.phys.EntityHitResult hit = new net.minecraft.world.phys.EntityHitResult(target);
        minecraft.gameMode.interact(minecraft.player, target, hit, InteractionHand.MAIN_HAND);
    }

    /** Types {@code text} into {@code screen} key by key, through the same hooks a keyboard goes through. */
    private static void type(Screen screen, String text) {
        for (char c : text.toCharArray()) {
            if (!ClientHooks.onScreenCharTypedPre(screen, new CharacterEvent(c))) screen.charTyped(new CharacterEvent(c));
        }
    }

    /** A written book with a page of plain prose, to see it mumbled. */
    private static ItemStack storyBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        Component page = Component.literal("Dear traveller, the train leaves at dawn. Bring bread for the journey and do not "
            + "open the last carriage, whatever you hear knocking inside it.");
        book.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough("Warning"), "Conductor", 0,
            List.of(Filterable.passThrough(page)), true));
        return book;
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
        player.level().getServer().execute(() -> {
            ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            item.setNoPickUpDelay();
            player.level().addFreshEntity(item);
        });
    }

    /** Puts the mouse over a slot of the open menu, so its tooltip shows (dev mappings: {@code MouseHandler} fields). */
    private static void hover(Minecraft minecraft, int slotIndex) {
        if (!(minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen)) return;
        Slot slot = screen.getMenu().getSlot(slotIndex);
        double scale = (double) minecraft.getWindow().getScreenWidth() / minecraft.getWindow().getGuiScaledWidth();
        try {
            Field x = MouseHandler.class.getDeclaredField("xpos");
            Field y = MouseHandler.class.getDeclaredField("ypos");
            x.setAccessible(true);
            y.setAccessible(true);
            x.setDouble(minecraft.mouseHandler, (((games.brennan.dungeontotems.mixin.client.AbstractContainerScreenAccess) screen).dungeontotems$leftPos() + slot.x + 8) * scale);
            y.setDouble(minecraft.mouseHandler, (((games.brennan.dungeontotems.mixin.client.AbstractContainerScreenAccess) screen).dungeontotems$topPos() + slot.y + 8) * scale);
        } catch (ReflectiveOperationException e) {
            DungeonTotems.LOGGER.warn("Zombie self-test couldn't move the mouse", e);
        }
    }

    /** A left click on a slot of the open menu, sent to the server as the player's own click. */
    private static void click(Minecraft minecraft, int slot, ContainerInput type) {
        minecraft.gameMode.handleContainerInput(minecraft.player.containerMenu.containerId, slot, 0, type, minecraft.player);
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
