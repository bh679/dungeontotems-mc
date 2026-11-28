package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Zombified player eats only what a zombie would: raw meat, raw fish and rotten flesh
 * ({@code #dungeontotems:zombie_food}), plus golden apples, which are the cure. Nothing in that list
 * gives them Hunger (a zombie's stomach is used to it); pufferfish still poisons. Drinks are untouched,
 * so a Weakness potion still works.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class ZombieDiet {

    public static final TagKey<Item> ZOMBIE_FOOD = TagKey.create(Registries.ITEM, DungeonTotems.id("zombie_food"));

    /** Zombified players who started eating while already hungry, so their Hunger is not ours to lift. */
    private static final Set<UUID> HUNGRY_BEFORE = ConcurrentHashMap.newKeySet();

    private ZombieDiet() {}

    public static boolean isZombie(Player player) {
        return player.hasEffect(TotemRegistries.ZOMBIFIED);
    }

    /** Food a zombie won't eat. Drinks and non-food items are never refused. */
    public static boolean refuses(Player player, ItemStack stack) {
        if (!isZombie(player) || stack.is(ZOMBIE_FOOD) || stack.getUseAnimation() != UseAnim.EAT) return false;
        return stack.getFoodProperties(player) != null;
    }

    /** Both sides, so the client never starts the eating animation. */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!refuses(event.getEntity(), event.getItemStack())) return;
        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
        tell(event.getEntity());
    }

    /** Backstop for anything that starts eating without a right click. */
    @SubscribeEvent
    public static void onStartEating(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (refuses(player, event.getItem())) {
            event.setCanceled(true);
            tell(player);
        } else if (isZombie(player) && player.hasEffect(MobEffects.HUNGER)) {
            HUNGRY_BEFORE.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        boolean hungryBefore = HUNGRY_BEFORE.remove(player.getUUID());
        if (!isZombie(player) || hungryBefore || !event.getItem().is(ZOMBIE_FOOD)) return;
        player.removeEffect(MobEffects.HUNGER);
    }

    @SubscribeEvent
    public static void onStopEating(LivingEntityUseItemEvent.Stop event) {
        HUNGRY_BEFORE.remove(event.getEntity().getUUID());
    }

    /** Cake isn't meat. */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!isZombie(player) || player.isSecondaryUseActive()) return;
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (!(block instanceof CakeBlock || block instanceof CandleCakeBlock)) return;
        event.setCanceled(true);
        tell(player);
    }

    private static void tell(Player player) {
        if (player instanceof ServerPlayer server) {
            server.displayClientMessage(Component.translatable("message.dungeontotems.zombified.diet"), true);
        }
    }
}
