package games.brennan.dungeontotems.platform;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * For GameTests and the client self-tests only: fires the loader's own hook for each game event a test
 * needs (NeoForge posts its event; Fabric runs its callback or mixin path), so a test checks the real
 * wiring on each loader. Each method returns whether the action was refused (cancelled).
 */
public interface TestHooks {

    /** As if {@code entity} finished eating or drinking {@code stack}. */
    void finishUsingItem(LivingEntity entity, ItemStack stack);

    /** As if {@code entity} began eating {@code stack}; true when refused. */
    boolean startUsingItem(LivingEntity entity, ItemStack stack, InteractionHand hand, int duration);

    boolean leftClickBlock(Player player, BlockPos pos, Direction face);

    boolean attackEntity(Player player, Entity target);

    boolean interactEntity(Player player, InteractionHand hand, Entity target);

    boolean rightClickBlock(Player player, InteractionHand hand, BlockHitResult hit);

    /** Right-clicks the block; true when the held item may not be used on it (a block item placed). */
    boolean rightClickBlockItemRefused(Player player, InteractionHand hand, BlockHitResult hit);

    boolean rightClickItem(Player player, InteractionHand hand);

    void trade(Player player, MerchantOffer offer, AbstractVillager villager);

    /** True when the player can't start breaking {@code state} at all. */
    boolean breakSpeedRefused(Player player, BlockState state, BlockPos pos);

    boolean breakBlock(Player player, BlockPos pos, BlockState state);

    boolean tameRefused(Animal animal, Player tamer);

    /** Client only: types one character into {@code screen} as a keyboard would. */
    void typeChar(Screen screen, char c);
}
