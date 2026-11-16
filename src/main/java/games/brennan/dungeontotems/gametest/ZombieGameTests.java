package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.curse.Curses;
import games.brennan.dungeontotems.curse.ZombieDiet;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import games.brennan.dungeontotems.curse.ZombiePack;
import games.brennan.dungeontotems.morph.Morphs;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

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
