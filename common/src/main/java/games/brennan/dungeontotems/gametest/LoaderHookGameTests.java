package games.brennan.dungeontotems.gametest;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.curse.Curses;
import games.brennan.dungeontotems.data.TotemData;
import games.brennan.dungeontotems.morph.Morphs;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The loader hooks themselves. The other suites post events straight to {@code TotemEvents}, which tests the
 * handlers but not how each loader fires them. These go in through vanilla's own entry points (eating,
 * trading, taming, attacking, right- and left-clicking, breaking), so Forge's events through
 * {@code ForgeEventBridge} and Fabric's mixins are what's tested, on each loader.
 */
public final class LoaderHookGameTests {

    private static final String EMPTY = "empty";

    public LoaderHookGameTests() {} // public: Fabric instantiates its gametest entrypoints

    /** Use-item start and finish: a Zombified, weakened player eats a golden apple the normal way and is cured. */
    @GameTest(template = EMPTY)
    public static void eatingAGoldenAppleCuresZombified(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200));
        player.getFoodData().setFoodLevel(10);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_APPLE));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        check(helper, player.isUsingItem(), "a zombie may eat a golden apple");
        tickUntilDone(player);
        check(helper, !player.hasEffect(TotemRegistries.ZOMBIFIED.get()), "eating it should cure Zombified");
        helper.succeed();
    }

    /** Use-item start, cancelled: a zombie can't start eating bread. */
    @GameTest(template = EMPTY)
    public static void aZombieCantStartEatingBread(GameTestHelper helper) {
        ServerPlayer player = zombie(helper);
        player.getFoodData().setFoodLevel(10);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        check(helper, !player.isUsingItem(), "a zombie shouldn't start eating bread");
        helper.succeed();
    }

    /** Use-item finish: bread eaten the normal way breaks the Melon Diet (no totem, no save: it kills). */
    @GameTest(template = EMPTY)
    public static void eatingBreadBreaksTheMelonDiet(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Curses.apply(player, Curse.MELON_DIET, 0, 400);
        player.getFoodData().setFoodLevel(10);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD));
        player.startUsingItem(InteractionHand.MAIN_HAND);
        tickUntilDone(player);
        tick(player, 2);
        check(helper, !player.isAlive(), "bread should break the Melon Diet");
        helper.succeed();
    }

    /** Trade: a villager's notifyTrade counts as Lonely's gift. */
    @GameTest(template = EMPTY)
    public static void tradingWithAVillagerIsAGift(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Villager villager = helper.spawnWithNoFreeWill(EntityType.VILLAGER, 2, 1, 3);
        villager.setTradingPlayer(player);
        TotemData.set(player, TotemRegistries.LAST_GIFT, 0L);
        villager.notifyTrade(new MerchantOffer(new ItemStack(Items.EMERALD), new ItemStack(Items.BREAD), 12, 1, 0.05F));
        long last = TotemData.get(player, TotemRegistries.LAST_GIFT);
        check(helper, last == player.level().getGameTime(), "the trade should count as a gift, last gift " + last);
        helper.succeed();
    }

    /** Tame: a zombie can't tame a wolf however many bones; a human can. */
    @GameTest(template = EMPTY)
    public static void onlyHumansTameWolves(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        Wolf wolf = helper.spawnWithNoFreeWill(EntityType.WOLF, 2, 1, 3);
        for (int i = 0; i < 40; i++) {
            zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            wolf.mobInteract(zombie, InteractionHand.MAIN_HAND);
        }
        check(helper, !wolf.isTame(), "a zombie shouldn't tame a wolf");
        ServerPlayer human = player(helper);
        Wolf other = helper.spawnWithNoFreeWill(EntityType.WOLF, 3, 1, 3);
        for (int i = 0; i < 40 && !other.isTame(); i++) {
            human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE));
            other.mobInteract(human, InteractionHand.MAIN_HAND);
        }
        check(helper, other.isTame(), "a human should tame a wolf within 40 bones");
        helper.succeed();
    }

    /** Attack: a form with nothing to hit with (an allay) leaves the pig whole; a player hurts it. */
    @GameTest(template = EMPTY)
    public static void aFormWithoutAnAttackDoesNoHarm(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, 2, 1, 3);
        Morphs.setForm(player, EntityType.ALLAY);
        player.attack(pig);
        check(helper, pig.getHealth() == pig.getMaxHealth(), "an allay shouldn't hurt the pig, health " + pig.getHealth());
        Morphs.setForm(player, null);
        player.resetAttackStrengthTicker();
        tick(player, 20);
        player.attack(pig);
        check(helper, pig.getHealth() < pig.getMaxHealth(), "a player should hurt the pig");
        helper.succeed();
    }

    /** Entity interact: a zombie offering wheat doesn't breed the cow; a human does. */
    @GameTest(template = EMPTY)
    public static void aZombieCantFeedACow(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, 2, 1, 3);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        InteractionResult refused = zombie.interactOn(cow, InteractionHand.MAIN_HAND);
        check(helper, !cow.isInLove(), "a zombie's wheat shouldn't breed the cow (" + refused + ")");
        ServerPlayer human = player(helper);
        human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
        human.interactOn(cow, InteractionHand.MAIN_HAND);
        check(helper, cow.isInLove(), "a human's wheat should");
        helper.succeed();
    }

    /** Break speed: a zombie can't dig a shulker box; a human can. */
    @GameTest(template = EMPTY)
    public static void aZombieCantDigAShulkerBox(GameTestHelper helper) {
        BlockState box = Blocks.SHULKER_BOX.defaultBlockState();
        check(helper, zombie(helper).getDestroySpeed(box) < 0.0F, "a zombie shouldn't dig a shulker box");
        check(helper, player(helper).getDestroySpeed(box) > 0.0F, "a human should");
        helper.succeed();
    }

    /** Block break: a zombie's break of a shulker box is refused; a human's goes through. */
    @GameTest(template = EMPTY)
    public static void aZombieCantBreakAShulkerBox(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.SHULKER_BOX.defaultBlockState());
        ServerPlayer zombie = zombie(helper);
        zombie.gameMode.destroyBlock(pos);
        check(helper, helper.getLevel().getBlockState(pos).is(Blocks.SHULKER_BOX), "a zombie shouldn't break it");
        player(helper).gameMode.destroyBlock(pos);
        check(helper, helper.getLevel().getBlockState(pos).isAir(), "a human should");
        helper.succeed();
    }

    /** Right-click block, cancelled: opening a barrel with a Restraint totem in hand pops it. */
    @GameTest(template = EMPTY)
    public static void openingABarrelPopsTheRestraintTotem(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TotemRegistries.TOTEMS.get(Curse.RESTRAINT).get()));
        BlockPos barrel = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(barrel, Blocks.BARREL.defaultBlockState());
        useOn(player, barrel);
        tick(player, 2);
        check(helper, player.getMainHandItem().isEmpty(), "opening the barrel should pop the Restraint totem");
        check(helper, player.hasEffect(TotemRegistries.RESTRAINED.get()), "and leave Restrained");
        helper.succeed();
    }

    /** Right-click block, item denied: a zombie can't place a shulker box; a human can. */
    @GameTest(template = EMPTY)
    public static void aZombieCantPlaceAShulkerBox(GameTestHelper helper) {
        BlockPos floor = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        ServerPlayer zombie = zombie(helper);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHULKER_BOX));
        useOn(zombie, floor);
        check(helper, !helper.getLevel().getBlockState(floor.above()).is(Blocks.SHULKER_BOX), "a zombie shouldn't place it");
        ServerPlayer human = player(helper);
        human.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHULKER_BOX));
        useOn(human, floor);
        check(helper, helper.getLevel().getBlockState(floor.above()).is(Blocks.SHULKER_BOX), "a human should");
        helper.succeed();
    }

    /** Right-click item: a zombie's held shulker box is never used. */
    @GameTest(template = EMPTY)
    public static void aZombieCantUseAHeldShulkerBox(GameTestHelper helper) {
        ServerPlayer zombie = zombie(helper);
        ItemStack box = new ItemStack(Items.SHULKER_BOX);
        zombie.setItemInHand(InteractionHand.MAIN_HAND, box);
        InteractionResult result = zombie.gameMode.useItem(zombie, helper.getLevel(), box, InteractionHand.MAIN_HAND);
        check(helper, result == InteractionResult.FAIL, "a zombie's held box should fail, got " + result);
        helper.succeed();
    }

    /** Left-click block: a sheep form hitting grass eats it, leaving dirt. */
    @GameTest(template = EMPTY)
    public static void aSheepFormGrazesOnALeftClick(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Morphs.setForm(player, EntityType.SHEEP);
        BlockPos grass = helper.absolutePos(new BlockPos(2, 0, 3));
        helper.getLevel().setBlockAndUpdate(grass, Blocks.GRASS_BLOCK.defaultBlockState());
        player.gameMode.handleBlockBreakAction(grass, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, Direction.UP,
            helper.getLevel().getMaxBuildHeight(), 0);
        check(helper, helper.getLevel().getBlockState(grass).is(Blocks.DIRT),
            "a sheep should eat the grass, it is " + helper.getLevel().getBlockState(grass));
        helper.succeed();
    }

    private static void useOn(ServerPlayer player, BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0.0, 0.5, 0.0), Direction.UP, pos, false);
        player.gameMode.useItemOn(player, player.serverLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
    }

    /** Ticks until the item in use is done (at most 3 s). */
    private static void tickUntilDone(ServerPlayer player) {
        for (int i = 0; i < 60 && player.isUsingItem() && player.isAlive(); i++) player.doTick();
    }

    private static void tick(ServerPlayer player, int ticks) {
        for (int i = 0; i < ticks && player.isAlive(); i++) player.doTick();
    }

    private static ServerPlayer zombie(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Curses.apply(player, Curse.ZOMBIFIED, 0, MobEffectInstance.INFINITE_DURATION);
        return player;
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = MockPlayers.create(helper);
        player.setGameMode(GameType.SURVIVAL);
        Vec3 at = helper.absoluteVec(new Vec3(2.5, 1, 2.5));
        player.teleportTo(at.x, at.y, at.z);
        return player;
    }

    private static void check(GameTestHelper helper, boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }
}
