package games.brennan.dungeontotems.neoforge;

import games.brennan.dungeontotems.platform.TestHooks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.TriState;
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
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

/** {@link TestHooks} on NeoForge: posts NeoForge's own events, as the game does. */
public final class NeoForgeTestHooks implements TestHooks {

    @Override
    public void finishUsingItem(LivingEntity entity, ItemStack stack) {
        EventHooks.onItemUseFinish(entity, stack, 0, ItemStack.EMPTY);
    }

    @Override
    public boolean startUsingItem(LivingEntity entity, ItemStack stack, InteractionHand hand, int duration) {
        return NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Start(entity, stack, hand, duration)).isCanceled();
    }

    @Override
    public boolean leftClickBlock(Player player, BlockPos pos, Direction face) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, pos, face,
            PlayerInteractEvent.LeftClickBlock.Action.START)).isCanceled();
    }

    @Override
    public boolean attackEntity(Player player, Entity target) {
        return NeoForge.EVENT_BUS.post(new AttackEntityEvent(player, target)).isCanceled();
    }

    @Override
    public boolean interactEntity(Player player, InteractionHand hand, Entity target) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.EntityInteract(player, hand, target, target.position())).isCanceled();
    }

    @Override
    public boolean rightClickBlock(Player player, InteractionHand hand, BlockHitResult hit) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit)).isCanceled();
    }

    @Override
    public boolean rightClickBlockItemRefused(Player player, InteractionHand hand, BlockHitResult hit) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit))
            .getUseItem() == TriState.FALSE;
    }

    @Override
    public boolean rightClickItem(Player player, InteractionHand hand) {
        return NeoForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickItem(player, hand)).isCanceled();
    }

    @Override
    public void trade(Player player, MerchantOffer offer, AbstractVillager villager) {
        NeoForge.EVENT_BUS.post(new TradeWithVillagerEvent(player, offer, villager));
    }

    @Override
    public boolean breakSpeedRefused(Player player, BlockState state, BlockPos pos) {
        return NeoForge.EVENT_BUS.post(new PlayerEvent.BreakSpeed(player, state, 1.0F, pos)).isCanceled();
    }

    @Override
    public boolean breakBlock(Player player, BlockPos pos, BlockState state) {
        return NeoForge.EVENT_BUS.post(new BreakBlockEvent(player.level(), pos, state, player)).isCanceled();
    }

    @Override
    public boolean tameRefused(Animal animal, Player tamer) {
        return EventHooks.onAnimalTame(animal, tamer); // true when the taming is blocked
    }

    @Override
    public void typeChar(Screen screen, char c) {
        if (!ClientHooks.onScreenCharTypedPre(screen, new CharacterEvent(c))) screen.charTyped(new CharacterEvent(c));
    }
}
