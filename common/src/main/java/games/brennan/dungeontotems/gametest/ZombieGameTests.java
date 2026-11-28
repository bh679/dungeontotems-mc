package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.platform.Services;
import games.brennan.dungeontotems.registry.TotemData;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.Curses;
import games.brennan.dungeontotems.curse.ZombieDiet;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import games.brennan.dungeontotems.curse.ZombiePack;
import games.brennan.dungeontotems.morph.MorphEvents;
import games.brennan.dungeontotems.morph.Morphs;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.Slot;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Life as a Zombified player: raw meat only ({@link ZombieDiet}), no trading
 * ({@link games.brennan.dungeontotems.curse.ZombieTrade}), illagers hunt you, and a locked pack
 * ({@link ZombiePack}), and no shulker boxes or taming ({@link games.brennan.dungeontotems.curse.ZombieLimits}).
 */
public final class ZombieGameTests {

    private static final int EAT_TICKS = 32;
    private static final int PACK_SLOT = 10;
    private static final int CHEST_SLOTS = 27;
    /** A wolf takes a bone 1 time in 3; 40 tries leave a human untamed about once in ten million. */
    private static final int TAME_TRIES = 40;
    private static final int SETTLE_TICKS = 10;
    private static final int GIBBERISH_LINES = 50;
    private static final int MUMBLE_SAMPLES = 20000;
    private static final int THOUGHT_SAMPLES = 200000;

    private ZombieGameTests() {}

    // ---- diet ----

    @DtGameTest
    public static void aZombieWontEatBread(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        check(helper, startEating(player, new ItemStack(Items.BREAD)), "bread should be refused");
        check(helper, !startEating(player, new ItemStack(Items.BEEF)), "raw beef should be eaten");
        check(helper, !startEating(player, new ItemStack(Items.GOLDEN_APPLE)), "a golden apple (the cure) should be eaten");
        check(helper, !startEating(player(helper), new ItemStack(Items.BREAD)), "a human eats bread");
        helper.succeed();
    }

    @DtGameTest
    public static void rottenFleshGivesAZombieNoHunger(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH);
        startEating(player, flesh);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600)); // the 80% roll, landed
        Services.testHooks().finishUsingItem(player, flesh);
        check(helper, !player.hasEffect(MobEffects.HUNGER), "rotten flesh should not make a zombie hungry");
        helper.succeed();
    }

    @DtGameTest
    public static void hungerFromBeforeStays(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600));
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH);
        startEating(player, flesh);
        Services.testHooks().finishUsingItem(player, flesh);
        check(helper, player.hasEffect(MobEffects.HUNGER), "Hunger you already had is not the food's");
        helper.succeed();
    }

    // ---- trading ----

    @DtGameTest
    public static void aVillagerAZombieTradesWithTurns(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        Villager villager = helper.spawn(EntityTypes.VILLAGER, 2, 1, 3);
        villager.setVillagerData(villager.getVillagerData().withProfession(villager.level().registryAccess(), VillagerProfession.LIBRARIAN));
        boolean refused = Services.testHooks().interactEntity(player, InteractionHand.MAIN_HAND, villager);
        check(helper, refused, "the trade should not open");
        check(helper, villager.isRemoved(), "the villager should be gone");
        List<ZombieVillager> zombies = helper.getLevel().getEntitiesOfClass(ZombieVillager.class, villager.getBoundingBox().inflate(2));
        check(helper, zombies.size() == 1, "one zombie villager should stand there, found " + zombies.size());
        check(helper, zombies.get(0).getVillagerData().profession().is(VillagerProfession.LIBRARIAN), "it should still be a librarian");
        zombies.get(0).discard();
        helper.succeed();
    }

    @DtGameTest
    public static void aHumanStillTrades(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Villager villager = helper.spawn(EntityTypes.VILLAGER, 2, 1, 3);
        boolean refused = Services.testHooks().interactEntity(player, InteractionHand.MAIN_HAND, villager);
        check(helper, !refused && !villager.isRemoved(), "a human's trade goes ahead");
        helper.succeed();
    }

    @DtGameTest
    public static void aWanderingTraderDrinksAndRuns(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        WanderingTrader trader = helper.spawn(EntityTypes.WANDERING_TRADER, 2, 1, 3);
        boolean refused = Services.testHooks().interactEntity(player, InteractionHand.MAIN_HAND, trader);
        check(helper, refused, "the trade should not open");
        check(helper, trader.hasEffect(MobEffects.INVISIBILITY), "the trader should drink invisibility");
        check(helper, trader.getLastHurtByMob() == player, "the trader should panic");
        helper.succeed();
    }

    // ---- illagers ----

    @DtGameTest
    public static void illagersHuntAZombie(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        Vindicator vindicator = helper.spawn(EntityTypes.VINDICATOR, 2, 1, 3);
        vindicator.setTarget(player);
        check(helper, vindicator.getTarget() == player, "a vindicator should go for a zombie player");
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, 3, 1, 3);
        zombie.setTarget(player);
        check(helper, zombie.getTarget() == null, "zombies still leave one of their own alone");
        helper.succeed();
    }

    @DtGameTest
    public static void illagersLeaveAMorphedZombieFormAlone(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Morphs.setForm(player, EntityTypes.ZOMBIE); // a zombie form without the curse
        Vindicator vindicator = helper.spawn(EntityTypes.VINDICATOR, 2, 1, 3);
        vindicator.setTarget(player);
        check(helper, vindicator.getTarget() == null, "only the Zombified curse draws illagers");
        helper.succeed();
    }

    // ---- the locked pack ----

    @DtGameTest
    public static void thePackIsLocked(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(0, new ItemStack(Items.ARROW, 16));
        player.getInventory().setItem(PACK_SLOT, new ItemStack(Items.DIRT, 8));
        zombify(player);
        Slot pack = player.inventoryMenu.getSlot(PACK_SLOT); // InventoryMenu slots 9..35 are inventory 9..35
        Slot empty = player.inventoryMenu.getSlot(PACK_SLOT + 1);
        check(helper, !pack.mayPickup(player), "nothing comes out of the pack");
        check(helper, !empty.mayPlace(new ItemStack(Items.BREAD)), "nothing new goes in");
        check(helper, empty.mayPlace(new ItemStack(Items.ARROW)), "arrows you carried can be put away");
        check(helper, player.inventoryMenu.getSlot(36).mayPickup(player), "the hotbar still works");
        player.setGameMode(GameType.CREATIVE);
        check(helper, pack.mayPickup(player), "creative players are not locked");
        helper.succeed();
    }

    @DtGameTest
    public static void weaknessComesOutOfThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        hostBackpackOwner = player;
        ItemStack totem = new ItemStack(TotemRegistries.TOTEMS.get(Curse.WEAKNESS).get());
        List<ItemStack> reachable = List.of(PotionContents.createItemStack(Items.POTION, Potions.WEAKNESS),
            PotionContents.createItemStack(Items.SPLASH_POTION, Potions.LONG_WEAKNESS),
            PotionContents.createItemStack(Items.LINGERING_POTION, Potions.WEAKNESS), totem);
        List<ItemStack> kept = List.of(PotionContents.createItemStack(Items.POTION, Potions.HEALING),
            PotionContents.createItemStack(Items.TIPPED_ARROW, Potions.WEAKNESS),
            new ItemStack(TotemRegistries.TOTEMS.get(Curse.FATIGUE).get()), new ItemStack(Items.TOTEM_OF_UNDYING));
        zombify(player);
        Slot pack = player.inventoryMenu.getSlot(PACK_SLOT);
        Slot backpack = new Slot(HOST_BACKPACK, 0, 0, 0);
        for (ItemStack stack : reachable) {
            pack.set(stack.copy());
            backpack.set(stack.copy());
            check(helper, pack.mayPickup(player), stack + " should come out of the pack");
            check(helper, backpack.mayPickup(player), stack + " should come out of a declared backpack slot");
            check(helper, !ZombiePack.keepsItem(pack), stack + " should not be greyed");
        }
        for (ItemStack stack : kept) {
            pack.set(stack.copy());
            backpack.set(stack.copy());
            check(helper, !pack.mayPickup(player), stack + " should stay in the pack");
            check(helper, !backpack.mayPickup(player), stack + " should stay in the backpack");
        }
        pack.set(ItemStack.EMPTY);
        check(helper, !pack.mayPlace(totem.copy()), "a weakness totem you didn't carry still can't go back in");
        HOST_BACKPACK.clearContent();
        hostBackpackOwner = null;
        helper.succeed();
    }

    @DtGameTest
    public static void pickupsSkipThePack(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        Inventory inventory = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) inventory.setItem(i, new ItemStack(Items.STONE));
        ItemStack picked = new ItemStack(Items.DIRT, 5);
        inventory.add(picked);
        check(helper, picked.getCount() == 5, "with a full hotbar the pickup should stay on the ground");
        for (int i = Inventory.getSelectionSize(); i < Inventory.INVENTORY_SIZE; i++) {
            check(helper, inventory.getItem(i).isEmpty(), "pack slot " + i + " should stay empty");
        }
        inventory.setItem(3, ItemStack.EMPTY);
        check(helper, inventory.add(new ItemStack(Items.DIRT, 5)) && inventory.getItem(3).is(Items.DIRT), "a free hotbar slot takes it");
        helper.succeed();
    }

    @DtGameTest
    public static void shiftClickDoesNotFillThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(PACK_SLOT, new ItemStack(Items.DIRT, 8));
        zombify(player);
        SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
        chest.setItem(0, new ItemStack(Items.DIRT, 8));
        ChestMenu menu = openChest(player, chest);
        menu.quickMoveStack(player, 0);
        check(helper, player.getInventory().getItem(PACK_SLOT).getCount() == 8, "the pack's dirt should not grow, has "
            + player.getInventory().getItem(PACK_SLOT).getCount());
        boolean inHotbar = false;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) inHotbar |= player.getInventory().getItem(i).is(Items.DIRT);
        check(helper, inHotbar, "the dirt should land in the hotbar");
        helper.succeed();
    }

    @DtGameTest
    public static void shiftClickingWhatYouCarriedFillsOnlyTheHotbar(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.MOSS_BLOCK, 32));
        for (int i = 1; i < Inventory.getSelectionSize(); i++) inventory.setItem(i, new ItemStack(Items.STONE));
        inventory.setItem(PACK_SLOT, new ItemStack(Items.MOSS_BLOCK, 20));
        zombify(player);
        SimpleContainer barrel = new SimpleContainer(CHEST_SLOTS);
        barrel.setItem(0, new ItemStack(Items.MOSS_BLOCK, 40));
        ChestMenu menu = openChest(player, barrel);
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        check(helper, inventory.getItem(0).getCount() == 64, "the hotbar moss should fill to 64, has " + inventory.getItem(0).getCount());
        check(helper, inventory.getItem(PACK_SLOT).getCount() == 20, "the pack's moss should not grow, has "
            + inventory.getItem(PACK_SLOT).getCount());
        check(helper, barrel.getItem(0).getCount() == 8, "the rest should stay in the barrel, has " + barrel.getItem(0).getCount());
        helper.succeed();
    }

    @DtGameTest
    public static void shiftClickingWhatYouCarriedUsesAFreeHotbarSlot(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Inventory inventory = player.getInventory();
        inventory.setItem(0, new ItemStack(Items.MOSS_BLOCK, 60));
        inventory.setItem(PACK_SLOT, new ItemStack(Items.MOSS_BLOCK, 20));
        zombify(player);
        SimpleContainer barrel = new SimpleContainer(CHEST_SLOTS);
        barrel.setItem(0, new ItemStack(Items.MOSS_BLOCK, 40));
        ChestMenu menu = openChest(player, barrel);
        menu.clicked(0, 0, ContainerInput.QUICK_MOVE, player);
        int inHotbar = 0;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (inventory.getItem(i).is(Items.MOSS_BLOCK)) inHotbar += inventory.getItem(i).getCount();
        }
        check(helper, inHotbar == 100, "all 40 moss should land in the hotbar, it holds " + inHotbar);
        check(helper, inventory.getItem(PACK_SLOT).getCount() == 20, "the pack's moss should not grow, has "
            + inventory.getItem(PACK_SLOT).getCount());
        check(helper, barrel.getItem(0).isEmpty(), "the barrel should be empty");
        helper.succeed();
    }

    /** A chest menu over {@code container}, open as {@code player}'s current menu, as a real chest screen is. */
    private static ChestMenu openChest(ServerPlayer player, SimpleContainer container) {
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), container);
        player.containerMenu = menu;
        return menu;
    }

    /** A stand-in for a backpack mod's slots: only slots over this container are declared pack slots. */
    private static final SimpleContainer HOST_BACKPACK = new SimpleContainer(2);
    private static volatile ServerPlayer hostBackpackOwner;

    static {
        ZombiePack.addPackSlots(slot -> slot.container == HOST_BACKPACK ? hostBackpackOwner : null);
    }

    @DtGameTest
    public static void hostDeclaredSlotsLockWithThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        hostBackpackOwner = player;
        player.getInventory().setItem(0, new ItemStack(Items.ARROW, 16));
        HOST_BACKPACK.setItem(0, new ItemStack(Items.DIRT, 8));
        Slot full = new Slot(HOST_BACKPACK, 0, 0, 0);
        Slot empty = new Slot(HOST_BACKPACK, 1, 0, 0);
        check(helper, !ZombiePack.isLockedSlot(full), "an unzombified player's backpack is open");
        zombify(player);
        check(helper, ZombiePack.isLockedSlot(full), "a declared slot locks with the pack");
        check(helper, !full.mayPickup(player), "nothing comes out of it");
        check(helper, !empty.mayPlace(new ItemStack(Items.ARROW)), "nothing goes in, not even what you carried");
        check(helper, !ZombiePack.isLockedSlot(new Slot(new SimpleContainer(1), 0, 0, 0)), "other containers are untouched");
        player.removeEffect(TotemRegistries.ZOMBIFIED);
        check(helper, full.mayPickup(player) && empty.mayPlace(new ItemStack(Items.BREAD)), "curing opens it again");
        hostBackpackOwner = null;
        helper.succeed();
    }

    @DtGameTest
    public static void curingUnlocksThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(0, new ItemStack(Items.ARROW));
        zombify(player);
        check(helper, !TotemData.get(player, TotemRegistries.ZOMBIE_CARRIED).isEmpty(), "what you carried is remembered");
        player.removeEffect(TotemRegistries.ZOMBIFIED);
        check(helper, TotemData.get(player, TotemRegistries.ZOMBIE_CARRIED).isEmpty(), "and forgotten once cured");
        check(helper, player.inventoryMenu.getSlot(PACK_SLOT).mayPlace(new ItemStack(Items.BREAD)), "the pack is open again");
        helper.succeed();
    }

    // ---- shulker boxes ----

    @DtGameTest
    public static void aZombieCantOpenOrPlaceAShulkerBox(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        BlockPos box = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(box, Blocks.SHULKER_BOX.defaultBlockState());
        check(helper, rightClick(player, box), "a zombie shouldn't open a shulker box");
        BlockPos stone = helper.absolutePos(new BlockPos(3, 1, 1));
        helper.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHULKER_BOX));
        check(helper, Services.testHooks().rightClickBlockItemRefused(player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(stone), Direction.UP, stone, false)), "a zombie shouldn't place a shulker box");
        helper.succeed();
    }

    @DtGameTest
    public static void aZombieCantOpenAShulkerBoxFromTheirHand(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DYED_SHULKER_BOX.blue()));
        check(helper, Services.testHooks().rightClickItem(zombie, InteractionHand.MAIN_HAND),
            "a zombie shouldn't use a shulker box in hand");
        ServerPlayer human = player(helper);
        human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DYED_SHULKER_BOX.blue()));
        check(helper, !Services.testHooks().rightClickItem(human, InteractionHand.MAIN_HAND),
            "a human still can");
        helper.succeed();
    }

    @DtGameTest
    public static void aShulkerBoxScreenShutsOnAZombie(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        zombie.openMenu(shulkerScreen());
        check(helper, zombie.containerMenu == zombie.inventoryMenu, "a shulker box screen shouldn't stay open on a zombie");
        ServerPlayer human = player(helper);
        human.openMenu(shulkerScreen());
        check(helper, human.containerMenu instanceof ShulkerBoxMenu, "a human's stays open");
        helper.succeed();
    }

    /**
     * The real server path for a held item (what Advanced Shulkerboxes hooks to open a box from the hand).
     * With that mod loaded, a human's box opens and a zombie's doesn't; without it nothing opens either way.
     */
    @DtGameTest
    public static void aZombieUsingAHeldShulkerBoxOpensNothing(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        useHeldShulkerBox(helper, zombie);
        check(helper, zombie.containerMenu == zombie.inventoryMenu, "a zombie's held shulker box shouldn't open");
        if (Services.PLATFORM.isModLoaded("shulkerbox")) {
            ServerPlayer human = player(helper);
            useHeldShulkerBox(helper, human);
            check(helper, human.containerMenu instanceof ShulkerBoxMenu, "Advanced Shulkerboxes should open a human's");
        }
        helper.succeed();
    }

    private static void useHeldShulkerBox(GameTestHelper helper, ServerPlayer player) {
        ItemStack box = new ItemStack(Items.SHULKER_BOX);
        player.setItemInHand(InteractionHand.MAIN_HAND, box);
        player.gameMode.useItem(player, helper.getLevel(), box, InteractionHand.MAIN_HAND);
    }

    private static SimpleMenuProvider shulkerScreen() {
        return new SimpleMenuProvider((id, inventory, p) -> new ShulkerBoxMenu(id, inventory, new SimpleContainer(CHEST_SLOTS)),
            Component.literal("box"));
    }

    @DtGameTest
    public static void aZombieCantMineAShulkerBox(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        BlockPos box = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = Blocks.DYED_SHULKER_BOX.red().defaultBlockState();
        helper.getLevel().setBlockAndUpdate(box, state);
        check(helper, Services.testHooks().breakSpeedRefused(player, state, box),
            "a zombie shouldn't crack a shulker box");
        check(helper, Services.testHooks().breakBlock(player, box, state),
            "a zombie shouldn't break a shulker box");
        check(helper, !Services.testHooks().breakBlock(player(helper), box, state),
            "a human still can");
        helper.succeed();
    }

    // ---- taming ----

    @DtGameTest
    public static void anAnimalAZombieFeedsRunsAway(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        Wolf wolf = helper.spawn(EntityTypes.WOLF, 2, 1, 3);
        helper.runAfterDelay(SETTLE_TICKS, () -> feedAndCheck(helper, zombie, wolf)); // a wolf in the air can't plan a path
    }

    private static void feedAndCheck(GameTestHelper helper, ServerPlayer zombie, Wolf wolf) {
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE, 4));
        boolean refused = Services.testHooks().interactEntity(zombie, InteractionHand.MAIN_HAND, wolf);
        check(helper, refused, "the wolf shouldn't take a zombie's bone");
        check(helper, zombie.getMainHandItem().getCount() == 4, "the bone isn't used");
        check(helper, wolf.getNavigation().isInProgress(), "the wolf should run");
        Cow cow = helper.spawn(EntityTypes.COW, 3, 1, 3);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        check(helper, Services.testHooks().interactEntity(zombie, InteractionHand.MAIN_HAND, cow),
            "a cow shouldn't take a zombie's wheat");
        ServerPlayer human = player(helper);
        for (int i = 0; i < TAME_TRIES && !wolf.isTame(); i++) {
            human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            wolf.mobInteract(human, InteractionHand.MAIN_HAND);
        }
        check(helper, wolf.isTame() && wolf.isOwnedBy(human), "a human still tames it");
        helper.succeed();
    }

    @DtGameTest
    public static void horsesAndCatsWontBeTamedByAZombie(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        for (EntityType<? extends Animal> type : List.of(EntityTypes.HORSE, EntityTypes.LLAMA, EntityTypes.CAT, EntityTypes.PARROT)) {
            Animal animal = helper.spawn(type, 2, 1, 3);
            // onAnimalTame is true when the taming is blocked.
            check(helper, Services.testHooks().tameRefused(animal, zombie), type.toShortString() + " should refuse a zombie");
            check(helper, !Services.testHooks().tameRefused(animal, player(helper)), type.toShortString() + " should accept a human");
            animal.discard();
        }
        helper.succeed();
    }

    /** Whether right-clicking the block at {@code pos} was refused. */
    private static boolean rightClick(ServerPlayer player, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return Services.testHooks().rightClickBlock(player, InteractionHand.MAIN_HAND, hit);
    }

    // ---- reading ----

    @DtGameTest
    public static void zombieGibberishIsStableAndMumbled(GameTestHelper helper) {
        int seed = ItemStack.hashItemAndComponents(new ItemStack(Items.IRON_SWORD));
        for (int line = 0; line < GIBBERISH_LINES; line++) {
            String first = ZombieGibberish.line(seed, line).getString();
            check(helper, first.equals(ZombieGibberish.line(seed, line).getString()), "a line must read the same every time");
            check(helper, !first.isBlank(), "a line is never empty");
        }
        check(helper, !ZombieGibberish.line(seed, 0).getString().equals(
            ZombieGibberish.line(ItemStack.hashItemAndComponents(new ItemStack(Items.BREAD)), 0).getString())
            || !ZombieGibberish.line(seed, 1).getString().equals(
            ZombieGibberish.line(ItemStack.hashItemAndComponents(new ItemStack(Items.BREAD)), 1).getString()),
            "different items should mostly read differently");
        helper.succeed();
    }

    @DtGameTest
    public static void aboutOneItemIn500StirsAThought(GameTestHelper helper) {
        int thoughtful = 0;
        Set<String> seen = new HashSet<>();
        for (int seed = 0; seed < THOUGHT_SAMPLES; seed++) {
            String thought = ZombieGibberish.thought(seed);
            if (thought == null) continue;
            thoughtful++;
            seen.add(thought);
            check(helper, key(ZombieGibberish.line(seed, 0)).equals("zombie.dungeontotems.thought." + thought), "a thoughtful item's name is its thought");
            check(helper, !key(ZombieGibberish.line(seed, 1)).startsWith("zombie.dungeontotems.thought."), "only the name line thinks");
        }
        double share = thoughtful / (double) THOUGHT_SAMPLES;
        check(helper, share > 0.5 / ZombieGibberish.THOUGHT_ODDS && share < 2.0 / ZombieGibberish.THOUGHT_ODDS,
            "about 1 item in " + ZombieGibberish.THOUGHT_ODDS + " should think, got " + thoughtful + " of " + THOUGHT_SAMPLES);
        check(helper, seen.size() == ZombieGibberish.THOUGHTS.size(), "every thought turns up, saw " + seen.size());
        helper.succeed();
    }

    @DtGameTest
    public static void aboutOneWordInThreeComesThrough(GameTestHelper helper) {
        Random random = new Random(7);
        int through = 0;
        for (int i = 0; i < MUMBLE_SAMPLES; i++) {
            ZombieGibberish.Edit edit = ZombieGibberish.writeWord("hello there", 11, random, key -> "<" + key + ">");
            if (edit.text().equals("hello there")) {
                through++;
            } else {
                check(helper, edit.text().startsWith("hello <zombie.dungeontotems.mumble.") && edit.cursor() == edit.text().length(),
                    "only the word before the cursor turns, and the cursor follows it: " + edit);
            }
        }
        double share = through / (double) MUMBLE_SAMPLES;
        check(helper, Math.abs(share - 1.0 / ZombieGibberish.WORD_THROUGH_ODDS) < 0.02, "about 1 word in 3 comes through, got " + share);
        check(helper, ZombieGibberish.writeWord("hello ", 6, random, key -> "x").text().equals("hello "), "no word, no change");
        helper.succeed();
    }

    @DtGameTest
    public static void theWardenHuntsAZombie(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        // Not warden.setTarget: a GameTest mock player always reports creative, which no warden targets.
        Warden warden = helper.spawn(EntityTypes.WARDEN, 2, 1, 3);
        check(helper, !MorphEvents.ignoresMorphed(warden, player), "sculk still goes for a zombie player");
        Zombie zombie = helper.spawn(EntityTypes.ZOMBIE, 3, 1, 3);
        check(helper, MorphEvents.ignoresMorphed(zombie, player), "a zombie still leaves one of its own alone");
        warden.discard();
        helper.succeed();
    }

    @DtGameTest
    public static void weaknessPotionsGiveThemselvesAway(GameTestHelper helper) {
        Set<String> labels = new HashSet<>();
        for (Holder<Potion> potion : List.of(Potions.WEAKNESS, Potions.LONG_WEAKNESS)) {
            for (Item item : List.of(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION, Items.TIPPED_ARROW)) {
                ItemStack stack = PotionContents.createItemStack(item, potion);
                check(helper, ZombieGibberish.weakens(stack), stack + " should read as weakness");
            }
        }
        check(helper, !ZombieGibberish.weakens(PotionContents.createItemStack(Items.POTION, Potions.HEALING)), "healing isn't weakness");
        for (int seed = 0; seed < ZombieGibberish.WEAKNESS_LABELS * 4; seed++) labels.add(key(ZombieGibberish.weaknessLabel(seed)));
        check(helper, labels.size() == ZombieGibberish.WEAKNESS_LABELS, "all six labels turn up, saw " + labels);
        helper.succeed();
    }

    @DtGameTest
    public static void readingMumblesAboutOneWordInThree(GameTestHelper helper) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < MUMBLE_SAMPLES / 10; i++) text.append("word").append(i).append(i % 7 == 0 ? "\n" : " ");
        Component read = ZombieGibberish.read(text.toString(), 3);
        check(helper, read.getString().equals(ZombieGibberish.read(text.toString(), 3).getString()), "the same text reads the same");
        // Kept words are plain text; mumbles are lang keys (and some of them start with "word" too).
        long kept = read.getSiblings().stream()
            .filter(c -> !(c.getContents() instanceof TranslatableContents) && c.getString().startsWith("word")).count();
        double share = kept / (double) (MUMBLE_SAMPLES / 10);
        check(helper, Math.abs(share - (1.0 - 1.0 / ZombieGibberish.READ_MUMBLE_ODDS)) < 0.03, "about 2 words in 3 read, got " + share);
        check(helper, read.getString().contains("\n"), "line breaks stay");
        helper.succeed();
    }

    @DtGameTest
    public static void aBookPageMumblesToItsLength(GameTestHelper helper) {
        Component page = ZombieGibberish.page(42, 30);
        check(helper, page.getString().equals(ZombieGibberish.page(42, 30).getString()), "a page must read the same every time");
        check(helper, page.getSiblings().stream().filter(c -> c.getContents() instanceof TranslatableContents).count() == 30,
            "30 words read as 30 mumbles");
        check(helper, ZombieGibberish.page(42, 0).getSiblings().stream().anyMatch(c -> c.getContents() instanceof TranslatableContents),
            "even an empty page mumbles");
        helper.succeed();
    }

    @DtGameTest
    public static void halfWordsAreRarerThanBrains(GameTestHelper helper) {
        Random random = new Random(1);
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < MUMBLE_SAMPLES; i++) counts.merge(ZombieGibberish.mumble(random), 1, Integer::sum);
        int brains = counts.getOrDefault("brains", 0);
        for (String rare : ZombieGibberish.RARE_MUMBLES) {
            check(helper, counts.getOrDefault(rare, 0) * 2 < brains, rare + " should be rarer than brains");
        }
        helper.succeed();
    }

    @DtGameTest
    public static void readingReturnsWhenCured(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        Component name = sword.getHoverName();
        player.removeEffect(TotemRegistries.ZOMBIFIED);
        check(helper, sword.getHoverName().equals(name) && !sword.has(DataComponents.CUSTOM_NAME),
            "the item itself never changes, so it reads again once cured");
        helper.succeed();
    }

    // ---- helpers ----

    /** The lang key a line is, or "" for a line of several mumbles. */
    private static String key(Component line) {
        return line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
    }

    /** Whether starting to eat {@code food} was refused. */
    private static boolean startEating(ServerPlayer player, ItemStack food) {
        return Services.testHooks().startUsingItem(player, food, InteractionHand.MAIN_HAND, EAT_TICKS);
    }

    private static ServerPlayer zombie(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        zombify(player);
        return player;
    }

    private static void zombify(ServerPlayer player) {
        Curses.apply(player, Curse.ZOMBIFIED, 0, MobEffectInstance.INFINITE_DURATION);
    }

    @SuppressWarnings("removal")
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.mock(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.teleportTo(at.x, at.y, at.z);
        return player;
    }

    private static void check(GameTestHelper helper, boolean ok, String message) {
        if (!ok) helper.fail(message);
    }
}
