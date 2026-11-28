package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.platform.TestHooks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
 * {@link TestHooks} on Fabric: Fabric API's own callback where the mod listens through one (item use, block
 * break), else the same {@link FabricHooks} call this mod's mixin makes at that spot in the game.
 */
public final class FabricTestHooks implements TestHooks {

    @Override
    public void finishUsingItem(LivingEntity entity, ItemStack stack) {
        FabricHooks.finishUsingItem(entity, stack);
    }

    @Override
    public boolean startUsingItem(LivingEntity entity, ItemStack stack, InteractionHand hand, int duration) {
        return FabricHooks.startUsingItem(entity, stack);
    }

    @Override
    public boolean leftClickBlock(Player player, BlockPos pos, Direction face) {
        return FabricHooks.leftClickBlock(player, pos, face);
    }

    @Override
    public boolean attackEntity(Player player, Entity target) {
        return FabricHooks.attack(player, target);
    }

    @Override
    public boolean interactEntity(Player player, InteractionHand hand, Entity target) {
        return FabricHooks.interactEntity(player, hand, target) != null;
    }

    @Override
    public boolean rightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
        return FabricHooks.rightClickBlock(player, hand, hit).isCanceled();
    }

    @Override
    public boolean rightClickBlockItemRefused(Player player, InteractionHand hand, BlockHitResult hit) {
        return !FabricHooks.rightClickBlock(player, hand, hit).useItem();
    }

    @Override
    public boolean rightClickItem(Player player, InteractionHand hand) {
        return UseItemCallback.EVENT.invoker().interact(player, player.level(), hand) != InteractionResult.PASS;
    }

    @Override
    public void trade(Player player, MerchantOffer offer, AbstractVillager villager) {
        DtEvents.TRADE.post(new PlayerEvents.Trade(player, villager));
    }

    @Override
    public boolean breakSpeedRefused(Player player, BlockState state, BlockPos pos) {
        return FabricHooks.breakSpeedRefused(player, state);
    }

    @Override
    public boolean breakBlock(Player player, BlockPos pos, BlockState state) {
        return !PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(player.level(), player, pos, state, null);
    }

    @Override
    public boolean tameRefused(Animal animal, Player tamer) {
        return FabricHooks.tameRefused(animal, tamer);
    }

    @Override
    public void typeChar(Screen screen, char c) {
        CharacterEvent event = new CharacterEvent(c);
        games.brennan.dungeontotems.fabric.client.FabricClientHooks.charTyped(screen, event);
        screen.charTyped(event);
    }
}
