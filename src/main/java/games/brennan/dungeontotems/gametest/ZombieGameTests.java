package games.brennan.dungeontotems.gametest;

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
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
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
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

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
@GameTestHolder(DungeonTotems.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ZombieGameTests {

    private static final String EMPTY = "empty";
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

    @GameTest(template = EMPTY)
    public static void aZombieWontEatBread(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        check(helper, startEating(player, new ItemStack(Items.BREAD)), "bread should be refused");
        check(helper, !startEating(player, new ItemStack(Items.BEEF)), "raw beef should be eaten");
        check(helper, !startEating(player, new ItemStack(Items.GOLDEN_APPLE)), "a golden apple (the cure) should be eaten");
        check(helper, !startEating(player(helper), new ItemStack(Items.BREAD)), "a human eats bread");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rottenFleshGivesAZombieNoHunger(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH);
        startEating(player, flesh);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600)); // the 80% roll, landed
        EventHooks.onItemUseFinish(player, flesh, 0, ItemStack.EMPTY);
        check(helper, !player.hasEffect(MobEffects.HUNGER), "rotten flesh should not make a zombie hungry");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hungerFromBeforeStays(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600));
        ItemStack flesh = new ItemStack(Items.ROTTEN_FLESH);
        startEating(player, flesh);
        EventHooks.onItemUseFinish(player, flesh, 0, ItemStack.EMPTY);
        check(helper, player.hasEffect(MobEffects.HUNGER), "Hunger you already had is not the food's");
        helper.succeed();
    }

    // ---- trading ----

    @GameTest(template = EMPTY)
    public static void aVillagerAZombieTradesWithTurns(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        Villager villager = helper.spawn(EntityType.VILLAGER, 2, 1, 3);
        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.LIBRARIAN));
        boolean refused = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, villager)).isCanceled();
        check(helper, refused, "the trade should not open");
        check(helper, villager.isRemoved(), "the villager should be gone");
        List<ZombieVillager> zombies = helper.getLevel().getEntitiesOfClass(ZombieVillager.class, villager.getBoundingBox().inflate(2));
        check(helper, zombies.size() == 1, "one zombie villager should stand there, found " + zombies.size());
        check(helper, zombies.get(0).getVillagerData().getProfession() == VillagerProfession.LIBRARIAN, "it should still be a librarian");
        zombies.get(0).discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aHumanStillTrades(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Villager villager = helper.spawn(EntityType.VILLAGER, 2, 1, 3);
        boolean refused = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, villager)).isCanceled();
        check(helper, !refused && !villager.isRemoved(), "a human's trade goes ahead");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aWanderingTraderDrinksAndRuns(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        WanderingTrader trader = helper.spawn(EntityType.WANDERING_TRADER, 2, 1, 3);
        boolean refused = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, trader)).isCanceled();
        check(helper, refused, "the trade should not open");
        check(helper, trader.hasEffect(MobEffects.INVISIBILITY), "the trader should drink invisibility");
        check(helper, trader.getLastHurtByMob() == player, "the trader should panic");
        helper.succeed();
    }

    // ---- illagers ----

    @GameTest(template = EMPTY)
    public static void illagersHuntAZombie(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        Vindicator vindicator = helper.spawn(EntityType.VINDICATOR, 2, 1, 3);
        vindicator.setTarget(player);
        check(helper, vindicator.getTarget() == player, "a vindicator should go for a zombie player");
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 3, 1, 3);
        zombie.setTarget(player);
        check(helper, zombie.getTarget() == null, "zombies still leave one of their own alone");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void illagersLeaveAMorphedZombieFormAlone(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Morphs.setForm(player, EntityType.ZOMBIE); // a zombie form without the curse
        Vindicator vindicator = helper.spawn(EntityType.VINDICATOR, 2, 1, 3);
        vindicator.setTarget(player);
        check(helper, vindicator.getTarget() == null, "only the Zombified curse draws illagers");
        helper.succeed();
    }

    // ---- the locked pack ----

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
    public static void shiftClickDoesNotFillThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(PACK_SLOT, new ItemStack(Items.DIRT, 8));
        zombify(player);
        SimpleContainer chest = new SimpleContainer(CHEST_SLOTS);
        chest.setItem(0, new ItemStack(Items.DIRT, 8));
        ChestMenu menu = ChestMenu.threeRows(0, player.getInventory(), chest);
        menu.quickMoveStack(player, 0);
        check(helper, player.getInventory().getItem(PACK_SLOT).getCount() == 8, "the pack's dirt should not grow, has "
            + player.getInventory().getItem(PACK_SLOT).getCount());
        boolean inHotbar = false;
        for (int i = 0; i < Inventory.getSelectionSize(); i++) inHotbar |= player.getInventory().getItem(i).is(Items.DIRT);
        check(helper, inHotbar, "the dirt should land in the hotbar");
        helper.succeed();
    }

    /** A stand-in for a backpack mod's slots: only slots over this container are declared pack slots. */
    private static final SimpleContainer HOST_BACKPACK = new SimpleContainer(2);
    private static volatile ServerPlayer hostBackpackOwner;

    static {
        ZombiePack.addPackSlots(slot -> slot.container == HOST_BACKPACK ? hostBackpackOwner : null);
    }

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
    public static void curingUnlocksThePack(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().setItem(0, new ItemStack(Items.ARROW));
        zombify(player);
        check(helper, !player.getData(TotemRegistries.ZOMBIE_CARRIED).isEmpty(), "what you carried is remembered");
        player.removeEffect(TotemRegistries.ZOMBIFIED);
        check(helper, player.getData(TotemRegistries.ZOMBIE_CARRIED).isEmpty(), "and forgotten once cured");
        check(helper, player.inventoryMenu.getSlot(PACK_SLOT).mayPlace(new ItemStack(Items.BREAD)), "the pack is open again");
        helper.succeed();
    }

    // ---- shulker boxes ----

    @GameTest(template = EMPTY)
    public static void aZombieCantOpenOrPlaceAShulkerBox(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        BlockPos box = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(box, Blocks.SHULKER_BOX.defaultBlockState());
        check(helper, rightClick(player, box).isCanceled(), "a zombie shouldn't open a shulker box");
        BlockPos stone = helper.absolutePos(new BlockPos(3, 1, 1));
        helper.getLevel().setBlockAndUpdate(stone, Blocks.STONE.defaultBlockState());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHULKER_BOX));
        check(helper, rightClick(player, stone).getUseItem() == TriState.FALSE, "a zombie shouldn't place a shulker box");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void aZombieCantOpenAShulkerBoxFromTheirHand(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_SHULKER_BOX));
        check(helper, NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(zombie, InteractionHand.MAIN_HAND)).isCanceled(),
            "a zombie shouldn't use a shulker box in hand");
        ServerPlayer human = player(helper);
        human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BLUE_SHULKER_BOX));
        check(helper, !NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(human, InteractionHand.MAIN_HAND)).isCanceled(),
            "a human still can");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
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
    @GameTest(template = EMPTY)
    public static void aZombieUsingAHeldShulkerBoxOpensNothing(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        useHeldShulkerBox(helper, zombie);
        check(helper, zombie.containerMenu == zombie.inventoryMenu, "a zombie's held shulker box shouldn't open");
        if (ModList.get().isLoaded("shulkerbox")) {
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

    @GameTest(template = EMPTY)
    public static void aZombieCantMineAShulkerBox(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        BlockPos box = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = Blocks.RED_SHULKER_BOX.defaultBlockState();
        helper.getLevel().setBlockAndUpdate(box, state);
        check(helper, NeoForge.EVENT_BUS.post(new PlayerEvent.BreakSpeed(player, state, 1.0F, box)).isCanceled(),
            "a zombie shouldn't crack a shulker box");
        check(helper, NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), box, state, player)).isCanceled(),
            "a zombie shouldn't break a shulker box");
        check(helper, !NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), box, state, player(helper))).isCanceled(),
            "a human still can");
        helper.succeed();
    }

    // ---- taming ----

    @GameTest(template = EMPTY)
    public static void anAnimalAZombieFeedsRunsAway(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        Wolf wolf = helper.spawn(EntityType.WOLF, 2, 1, 3);
        helper.runAfterDelay(SETTLE_TICKS, () -> feedAndCheck(helper, zombie, wolf)); // a wolf in the air can't plan a path
    }

    private static void feedAndCheck(GameTestHelper helper, ServerPlayer zombie, Wolf wolf) {
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE, 4));
        boolean refused = NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(zombie, InteractionHand.MAIN_HAND, wolf)).isCanceled();
        check(helper, refused, "the wolf shouldn't take a zombie's bone");
        check(helper, zombie.getMainHandItem().getCount() == 4, "the bone isn't used");
        check(helper, wolf.getNavigation().isInProgress(), "the wolf should run");
        Cow cow = helper.spawn(EntityType.COW, 3, 1, 3);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        check(helper, NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(zombie, InteractionHand.MAIN_HAND, cow)).isCanceled(),
            "a cow shouldn't take a zombie's wheat");
        ServerPlayer human = player(helper);
        for (int i = 0; i < TAME_TRIES && !wolf.isTame(); i++) {
            human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            wolf.mobInteract(human, InteractionHand.MAIN_HAND);
        }
        check(helper, wolf.isTame() && wolf.isOwnedBy(human), "a human still tames it");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void horsesAndCatsWontBeTamedByAZombie(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        for (EntityType<? extends Animal> type : List.of(EntityType.HORSE, EntityType.LLAMA, EntityType.CAT, EntityType.PARROT)) {
            Animal animal = helper.spawn(type, 2, 1, 3);
            // onAnimalTame is true when the taming is blocked.
            check(helper, EventHooks.onAnimalTame(animal, zombie), type.toShortString() + " should refuse a zombie");
            check(helper, !EventHooks.onAnimalTame(animal, player(helper)), type.toShortString() + " should accept a human");
            animal.discard();
        }
        helper.succeed();
    }

    private static PlayerInteractEvent.RightClickBlock rightClick(ServerPlayer player, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, hit));
    }

    // ---- reading ----

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
    public static void theWardenHuntsAZombie(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        // Not warden.setTarget: a GameTest mock player always reports creative, which no warden targets.
        Warden warden = helper.spawn(EntityType.WARDEN, 2, 1, 3);
        check(helper, !MorphEvents.ignoresMorphed(warden, player), "sculk still goes for a zombie player");
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 3, 1, 3);
        check(helper, MorphEvents.ignoresMorphed(zombie, player), "a zombie still leaves one of its own alone");
        warden.discard();
        helper.succeed();
    }

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
    public static void aBookPageMumblesToItsLength(GameTestHelper helper) {
        Component page = ZombieGibberish.page(42, 30);
        check(helper, page.getString().equals(ZombieGibberish.page(42, 30).getString()), "a page must read the same every time");
        check(helper, page.getSiblings().stream().filter(c -> c.getContents() instanceof TranslatableContents).count() == 30,
            "30 words read as 30 mumbles");
        check(helper, ZombieGibberish.page(42, 0).getSiblings().stream().anyMatch(c -> c.getContents() instanceof TranslatableContents),
            "even an empty page mumbles");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
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

    @GameTest(template = EMPTY)
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
        return NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Start(player, food, InteractionHand.MAIN_HAND, EAT_TICKS)).isCanceled();
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
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.teleportTo(at.x, at.y, at.z);
        return player;
    }

    private static void check(GameTestHelper helper, boolean ok, String message) {
        if (!ok) helper.fail(message);
    }
}
