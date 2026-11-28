package games.brennan.dungeontotems.curse;

import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import games.brennan.dungeontotems.platform.event.SubscribeEvent;
import games.brennan.dungeontotems.platform.event.EventBusSubscriber;
import games.brennan.dungeontotems.platform.event.LivingEntityUseItemEvent;
import games.brennan.dungeontotems.platform.event.TickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Curses that end in death: the rule curses' doom ({@link #doom}), which after a save only takes you to
 * half a heart ({@link SavedCurseShield}). The death comes on your next tick, never inside the event that
 * caused it. Also the cure for <b>Zombified</b>, which has no timer and lasts until cured: eat a golden
 * apple while you have Weakness, like curing a zombie villager. Any Weakness counts: a potion, a splash,
 * or a Totem of Weakness's curse.
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class DeadlineCurses {

    private static final Map<UUID, ResourceKey<DamageType>> PENDING_DEATHS = new ConcurrentHashMap<>();

    private DeadlineCurses() {}

    /** Kills the player on their next tick with {@code cause}, outside whatever event is running now. */
    public static void doom(ServerPlayer player, ResourceKey<DamageType> cause) {
        PENDING_DEATHS.put(player.getUUID(), cause);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        ResourceKey<DamageType> cause = PENDING_DEATHS.remove(player.getUUID());
        if (cause == null || !player.isAlive()) return;
        Holder<DamageType> type = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(cause);
        player.hurt(new DamageSource(type), Float.MAX_VALUE);
    }

    // ---- Zombified ----

    @SubscribeEvent
    public static void onFinishUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.hasEffect(TotemRegistries.ZOMBIFIED.get())) return;
        if (isGoldenApple(event.getItem()) && player.hasEffect(MobEffects.WEAKNESS)) {
            cure(player, TotemRegistries.ZOMBIFIED.get(), "zombified");
        }
    }

    static boolean isGoldenApple(ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    /** Lifts {@code curse}: a message and a sound. */
    static void cure(ServerPlayer player, MobEffect curse, String name) {
        player.removeEffect(curse);
        Curse lifted = Curse.forEffect(curse);
        if (lifted != null) CurseTrials.onLifted(player, lifted);
        player.displayClientMessage(Component.translatable("message.dungeontotems." + name + ".lifted"), false);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
