package games.brennan.dungeontotems.morph;

import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import net.minecraft.world.entity.EntityReference;
import games.brennan.dungeontotems.DungeonTotems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * How other mobs treat a form, and what a form does for those around it:
 * <ul>
 *   <li>Creepers flee a cat or ocelot and phantoms won't dive at one, as with the real cats.</li>
 *   <li>Zombified piglins never turn on one of their own, and join in when something hurts it.</li>
 *   <li>An axolotl playing dead is ignored.</li>
 *   <li>A dolphin gives Dolphin's Grace to players swimming nearby.</li>
 *   <li>A strider stands and walks on lava (sneak to sink); see {@link FormTraits#standsOn}.</li>
 *   <li>Another player with a bowl can milk a mooshroom for mushroom stew.</li>
 * </ul>
 */
public final class FormAllies {

    private static final float CREEPER_FLEE_DISTANCE = 6.0F;
    private static final double PACK_RADIUS = 20.0;
    private static final double DOLPHIN_GRACE_RADIUS = 10.0;
    private static final int DOLPHIN_GRACE_TICKS = 100;
    private static final double LAVA_RISE = 0.1;

    private FormAllies() {}

    private static boolean isCat(@Nullable LivingEntity entity) {
        EntityType<?> form = entity instanceof Player player ? Morphs.formOf(player).orElse(null) : null;
        return form == EntityTypes.CAT || form == EntityTypes.OCELOT;
    }

    private static boolean is(@Nullable LivingEntity entity, EntityType<?> form) {
        return entity instanceof Player player && Morphs.formOf(player).orElse(null) == form;
    }

    public static void onTarget(LivingEvents.ChangeTarget event) {
        LivingEntity mob = event.getEntity();
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null) return;
        boolean afraid = (mob instanceof Creeper || mob instanceof Phantom) && isCat(target);
        boolean kin = mob instanceof ZombifiedPiglin && is(target, EntityTypes.ZOMBIFIED_PIGLIN);
        boolean fooled = target instanceof Player player && WaterMoves.isPlayingDead(player);
        if (afraid || kin || fooled) event.setCanceled(true);
    }

    /** Creepers get the same flee goal they have for real cats. */
    public static void onJoin(LivingEvents.JoinLevel event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Creeper creeper)) return;
        ((games.brennan.dungeontotems.mixin.MobInvoker) creeper).dungeontotems$goalSelector().addGoal(3, new AvoidEntityGoal<>(creeper, Player.class, CREEPER_FLEE_DISTANCE, 1.0, 1.2, FormAllies::isCat));
    }

    /** {@code ZombifiedPiglin#alertOthers}: hurt one of the pack and the pack comes for you. */
    public static void onHurt(LivingEvents.DamagePost event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !is(player, EntityTypes.ZOMBIFIED_PIGLIN)) return;
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == player) return;
        AABB area = player.getBoundingBox().inflate(PACK_RADIUS);
        for (ZombifiedPiglin piglin : player.level().getEntitiesOfClass(ZombifiedPiglin.class, area, p -> p != attacker)) {
            piglin.setTarget(attacker);
            piglin.setPersistentAngerTarget(EntityReference.of(attacker));
            piglin.startPersistentAngerTimer();
        }
    }

    /** {@code MushroomCow#mobInteract}: a bowl on a mooshroom gives mushroom stew. */
    public static void onInteract(PlayerEvents.EntityInteract event) {
        Player milker = event.getEntity();
        ItemStack held = event.getItemStack();
        if (!held.is(Items.BOWL) || !is(event.getTarget() instanceof LivingEntity target ? target : null, EntityTypes.MOOSHROOM)) return;
        if (!milker.level().isClientSide()) {
            milker.setItemInHand(event.getHand(), ItemUtils.createFilledResult(held, milker, new ItemStack(Items.MUSHROOM_STEW), false));
            milker.level().playSound(null, event.getTarget().blockPosition(), SoundEvents.MOOSHROOM_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void onPlayerTick(PlayerEvents.TickPre event) {
        Player player = event.getEntity();
        EntityType<?> form = Morphs.formOf(player).orElse(null);
        if (form == EntityTypes.STRIDER) walkOnLava(player);
        if (form == EntityTypes.DOLPHIN && player instanceof ServerPlayer server && server.tickCount % 20 == 0) shareGrace(server);
    }

    /**
     * {@code Strider#floatStrider}: a strider that sank into lava bobs back up to stand on it (sneak to
     * stay under). Runs on both sides, because the player's own client moves them.
     */
    private static void walkOnLava(Player player) {
        if (!player.isInLava() || player.isShiftKeyDown()) return;
        CollisionContext context = CollisionContext.of(player);
        BlockPos at = player.blockPosition();
        if (context.isAbove(player.getLiquidCollisionShape(), at, true) && !player.level().getFluidState(at.above()).is(FluidTags.LAVA)) {
            player.setOnGround(true);
        } else { // under the surface: rise until it can stand on it (gravity would outpull vanilla's 0.05)
            Vec3 motion = player.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0);
            player.setDeltaMovement(motion.x, Math.max(motion.y, LAVA_RISE), motion.z);
        }
        player.resetFallDistance();
    }

    /** {@code DolphinSwimWithPlayerGoal}: swimmers near a dolphin get Dolphin's Grace. */
    private static void shareGrace(ServerPlayer dolphin) {
        AABB area = dolphin.getBoundingBox().inflate(DOLPHIN_GRACE_RADIUS);
        for (Player swimmer : dolphin.level().getEntitiesOfClass(Player.class, area, p -> p != dolphin && p.isSwimming())) {
            swimmer.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, DOLPHIN_GRACE_TICKS), dolphin);
        }
    }
}
