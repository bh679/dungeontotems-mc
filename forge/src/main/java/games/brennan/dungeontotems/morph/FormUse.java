package games.brennan.dungeontotems.morph;

import games.brennan.dungeontotems.registry.TotemAttributes;
import games.brennan.dungeontotems.util.Modifiers;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.net.TotemNetwork;
import games.brennan.dungeontotems.net.MorphActionPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * "Use" with an empty hand, as a form, does the mob's own extra move: a chicken lays an egg; the rest
 * are in {@link LandMoves} and {@link WaterMoves}. (Main's armadillo roll is left out: 1.20.1 has no armadillo.)
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID)
public final class FormUse {

    private static final int EGG_COOLDOWN_TICKS = 100;

    private static final Map<UUID, Long> LAST_EGG = new ConcurrentHashMap<>();

    private FormUse() {}

    /** From the {@link games.brennan.dungeontotems.net.FormUsePayload} handler. */
    public static void use(ServerPlayer player) {
        EntityType<?> form = Morphs.formOf(player).orElse(null);
        if (form == null) return;
        if (form == EntityType.CHICKEN) layEgg(player);
        else if (form == EntityType.CAMEL || form == EntityType.CAT) LandMoves.sit(player);
        else if (form == EntityType.BAT) LandMoves.hang(player);
        else if (form == EntityType.FROG) LandMoves.croak(player);
        else if (form == EntityType.SNIFFER) LandMoves.dig(player);
        else if (form == EntityType.SNOW_GOLEM) LandMoves.throwSnowball(player);
        else if (form == EntityType.TURTLE) LandMoves.layTurtleEgg(player);
        else if (form == EntityType.PARROT) LandMoves.imitate(player);
        else if (form == EntityType.AXOLOTL) WaterMoves.playDead(player);
        else if (form == EntityType.PUFFERFISH) WaterMoves.puff(player);
        else if (form == EntityType.SQUID || form == EntityType.GLOW_SQUID) WaterMoves.ink(player);
        else if (form == EntityType.DOLPHIN) WaterMoves.leap(player);
        else FormSounds.speak(player); // no move of its own: its noise
    }

    /** From the {@link games.brennan.dungeontotems.net.FormJumpPayload} handler: a camel's jump is its dash. */
    public static void jump(ServerPlayer player) {
        if (Morphs.formOf(player).orElse(null) == EntityType.CAMEL) LandMoves.dash(player);
    }

    /** Forms that jump their own way: the client sends their jump to the server. */
    public static final Set<EntityType<?>> OWN_JUMP = Set.of(EntityType.CAMEL);

    /** Forms with a "use" move; the rest make their sound. */
    public static final Set<EntityType<?>> USABLE = Set.of(EntityType.CHICKEN, EntityType.CAMEL,
        EntityType.CAT, EntityType.BAT, EntityType.FROG, EntityType.SNIFFER, EntityType.SNOW_GOLEM, EntityType.TURTLE,
        EntityType.PARROT, EntityType.AXOLOTL, EntityType.PUFFERFISH, EntityType.SQUID, EntityType.GLOW_SQUID, EntityType.DOLPHIN);

    /** Blocks a hand is for (right-clicking them as a form still uses them): anything with a block entity (chests, furnaces, signs), doors, switches, beds. */
    public static boolean isUsable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return level.getBlockEntity(pos) != null || state.getMenuProvider(level, pos) != null
            || state.is(BlockTags.DOORS) || state.is(BlockTags.TRAPDOORS) || state.is(BlockTags.FENCE_GATES)
            || state.is(BlockTags.BUTTONS) || state.is(BlockTags.BEDS) || state.is(BlockTags.CANDLES)
            || state.getBlock() instanceof LeverBlock
            || state.getBlock() instanceof BellBlock || state.getBlock() instanceof NoteBlock
            || state.getBlock() instanceof RepeaterBlock || state.getBlock() instanceof ComparatorBlock
            || state.getBlock() instanceof CakeBlock;
    }

    /** {@code Chicken#aiStep}: an egg drops with the chicken's plop. */
    private static void layEgg(ServerPlayer player) {
        long now = player.level().getGameTime();
        if (now - LAST_EGG.getOrDefault(player.getUUID(), Long.MIN_VALUE / 2) < EGG_COOLDOWN_TICKS) return;
        LAST_EGG.put(player.getUUID(), now);
        float pitch = (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F;
        player.level().playSound(null, player.blockPosition(), SoundEvents.CHICKEN_EGG, SoundSource.PLAYERS, 1.0F, pitch);
        ItemEntity egg = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(Items.EGG));
        egg.setDefaultPickUpDelay(); // as Mob#spawnAtLocation: you see it laid before you pick it up
        player.level().addFreshEntity(egg);
    }

    /** Pins an attribute to nothing (playing dead), or lets it go. */
    static void hold(ServerPlayer player, Attribute attribute, ResourceLocation id, boolean held) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        Modifiers.remove(instance, id);
        if (held) instance.addTransientModifier(Modifiers.of(id, -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    /** Hurt, a squid inks, as the mob does. */
    @SubscribeEvent(priority = EventPriority.LOWEST) // main's LivingDamageEvent.Post: run after anything that changes or cancels the hit
    public static void onHurtAfter(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F
            || player.getHealth() - event.getAmount() <= 0.0F) return; // Forge fires before health drops: a killing blow doesn't ink
        EntityType<?> form = Morphs.formOf(player).orElse(null);
        if (form == EntityType.SQUID || form == EntityType.GLOW_SQUID) {
            WaterMoves.ink(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player instanceof ServerPlayer player) WaterMoves.tickPuffed(player);
    }

    /** From {@link Morphs#setForm}. */
    static void onFormChanged(ServerPlayer player, @Nullable EntityType<?> form) {
        WaterMoves.onFormChanged(player);
    }
}
