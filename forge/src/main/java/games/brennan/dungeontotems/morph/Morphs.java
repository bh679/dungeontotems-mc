package games.brennan.dungeontotems.morph;

import games.brennan.dungeontotems.util.Modifiers;
import games.brennan.dungeontotems.data.TotemData;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.advancement.TotemTriggers;
import games.brennan.dungeontotems.net.MorphSyncPayload;
import games.brennan.dungeontotems.registry.TotemAttributes;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import games.brennan.dungeontotems.net.TotemNetwork;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToIntFunction;

/**
 * The morph engine shared by the Morphing and Zombified totems: a player's form is an entity type
 * kept in {@link TotemRegistries#FORM}. It drives their stats (here), hitbox and mob reactions
 * ({@link MorphEvents}) and how everyone sees them (client {@code MorphRenderer}).
 */
public final class Morphs {

    private static final double AGGRO_DROP_RADIUS = 32.0;

    private static final ResourceLocation MAX_HEALTH_ID = DungeonTotems.id("morph.max_health");
    private static final ResourceLocation SPEED_ID = DungeonTotems.id("morph.speed");
    private static final ResourceLocation ATTACK_ID = DungeonTotems.id("morph.attack_damage");
    private static final ResourceLocation JUMP_ID = DungeonTotems.id("morph.jump");
    private static final ResourceLocation STEP_ID = DungeonTotems.id("morph.step_height");
    private static final ResourceLocation FORM_JUMP_ID = DungeonTotems.id("morph.form_jump");
    /**
     * Jump strengths a form's default attributes don't capture: horses always make their best jump (vanilla
     * rolls 0.4 to 1.0 per horse), camels can't jump (they step up blocks), frogs leap.
     */
    private static final Map<EntityType<?>, Double> FORM_JUMP = Map.of(
        EntityType.HORSE, 1.0, EntityType.SKELETON_HORSE, 1.0, EntityType.ZOMBIE_HORSE, 1.0,
        EntityType.CAMEL, 0.0, EntityType.FROG, 0.7);

    private Morphs() {}

    /**
     * Turns the player into a random form from the pool for where they are (in lava, underwater, or
     * anywhere else; an empty lava or water pool falls back to land), weighted by the
     * {@link MorphFormTable}, among the forms that fit where they stand. Returns the form, or null
     * when none fits and they stay a player.
     */
    @Nullable
    public static EntityType<?> startRandomMorph(ServerPlayer player) {
        MorphForm.Pool pool = poolFor(player);
        List<EntityType<?>> fitting = fitting(player, pool);
        if (fitting.isEmpty() && pool != MorphForm.Pool.LAND) fitting = fitting(player, MorphForm.Pool.LAND);
        if (fitting.isEmpty()) {
            DungeonTotems.LOGGER.debug("No morph fits around {}; staying a player", player.getScoreboardName());
            return null;
        }
        EntityType<?> form = pick(fitting, Morphs::weightOf, player.getRandom());
        setForm(player, form);
        TotemTriggers.BECAME_FORM.get().trigger(player, BuiltInRegistries.ENTITY_TYPE.getKey(form).toString());
        return form;
    }

    static MorphForm.Pool poolFor(Player player) {
        if (player.isInLava()) return MorphForm.Pool.LAVA;
        if (player.isEyeInFluid(FluidTags.WATER)) return MorphForm.Pool.WATER;
        return MorphForm.Pool.LAND;
    }

    /** The pool's forms with weight above 0 that fit around the player. */
    private static List<EntityType<?>> fitting(ServerPlayer player, MorphForm.Pool pool) {
        List<EntityType<?>> fitting = new ArrayList<>();
        MorphFormTable.all().forEach((key, form) -> {
            if (form.pool() != pool || form.weight() <= 0) return;
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
            if (type != null && DefaultAttributes.hasSupplier(type) && fits(player, type)) fitting.add(type);
        });
        return fitting;
    }

    /** The form's pool, weight and time, if it has one. */
    public static Optional<MorphForm> formData(EntityType<?> type) {
        return MorphFormTable.get(type);
    }

    private static int weightOf(EntityType<?> type) {
        return formData(type).map(MorphForm::weight).orElse(0);
    }

    /** A weighted random pick; every item needs a weight above 0. */
    static <T> T pick(List<T> items, ToIntFunction<T> weight, RandomSource random) {
        int total = items.stream().mapToInt(weight).sum();
        int roll = random.nextInt(total);
        for (T item : items) {
            roll -= weight.applyAsInt(item);
            if (roll < 0) return item;
        }
        return items.get(items.size() - 1);
    }

    /** Called when the Morphed or Zombified curse ends: falls back to whichever is still running. */
    public static void onCurseEnded(ServerPlayer player, MobEffect ending) {
        boolean zombified = !ending.is(TotemRegistries.ZOMBIFIED.get()) && player.hasEffect(TotemRegistries.ZOMBIFIED.get());
        boolean morphed = !ending.is(TotemRegistries.MORPHED.get()) && player.hasEffect(TotemRegistries.MORPHED.get());
        if (zombified) setForm(player, EntityType.ZOMBIE);
        else if (morphed) startRandomMorph(player);
        else setForm(player, null);
    }

    /** Sets (or with null, clears) the player's form, then re-applies stats, hitbox and sync. */
    public static void setForm(ServerPlayer player, @Nullable EntityType<?> type) {
        EntityType<?> previous = formOf(player).orElse(null);
        String id = type == null ? "" : BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        TotemData.set(player, TotemRegistries.FORM, id);
        applyStats(player, type);
        player.refreshDimensions();
        if (type != null) dropMonsterAggro(player);
        FormTimers.onFormChanged(player);
        MorphFlight.onFormChanged(player, previous, type);
        FormAttacks.onFormChanged(player, type);
        FormTraits.onFormChanged(player, type);
        FormUse.onFormChanged(player, type);
        WaterLife.onFormChanged(player);
        TotemNetwork.sendToTrackingAndSelf(player, new MorphSyncPayload(player.getId(), id));
    }

    /** The player's current form, if any. Safe on both sides and during entity construction. */
    public static Optional<EntityType<?>> formOf(Player player) {
        if (!TotemData.has(player, TotemRegistries.FORM)) return Optional.empty();
        String id = TotemData.get(player, TotemRegistries.FORM);
        if (id.isEmpty()) return Optional.empty();
        ResourceLocation key = ResourceLocation.tryParse(id);
        return key == null ? Optional.empty() : BuiltInRegistries.ENTITY_TYPE.getOptional(key);
    }

    /** Re-applies stat modifiers for the stored form (they are transient, so a relog needs this). */
    @SuppressWarnings("unchecked")
    static void applyStats(Player player, @Nullable EntityType<?> type) {
        clear(player, Attributes.MAX_HEALTH, MAX_HEALTH_ID);
        clear(player, Attributes.MOVEMENT_SPEED, SPEED_ID);
        clear(player, Attributes.ATTACK_DAMAGE, ATTACK_ID);
        clear(player, TotemAttributes.jumpStrength(), JUMP_ID);
        clear(player, TotemAttributes.stepHeight(), STEP_ID);
        clear(player, TotemAttributes.jumpStrength(), FORM_JUMP_ID);
        if (type == null || !DefaultAttributes.hasSupplier(type)) return;

        AttributeSupplier mob = DefaultAttributes.getSupplier((EntityType<? extends LivingEntity>) type);
        add(player, mob, Attributes.MAX_HEALTH, MAX_HEALTH_ID, MorphStats::maxHealthDelta, AttributeModifier.Operation.ADDITION);
        MobGaits.Gait gait = MobGaits.of(type);
        if (WaterLife.isLandlocked(type)) { // can't walk on land (swimming doesn't use this speed)
            add(player, mob, Attributes.MOVEMENT_SPEED, SPEED_ID, mobSpeed -> -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else if (gait == null) { // flyers
            add(player, mob, Attributes.MOVEMENT_SPEED, SPEED_ID, MorphStats::speedBonus, AttributeModifier.Operation.MULTIPLY_TOTAL);
        } else { // land forms walk at their mob's stroll
            add(player, mob, Attributes.MOVEMENT_SPEED, SPEED_ID,
                mobSpeed -> MobGaits.walkSpeed(gait, mobSpeed) / MobGaits.PLAYER_WALK_SPEED - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        add(player, mob, Attributes.ATTACK_DAMAGE, ATTACK_ID, MorphStats::attackDelta, AttributeModifier.Operation.ADDITION);
        if (!FORM_JUMP.containsKey(type)) {
            addFrom(player, TotemAttributes.jumpStrength(), JUMP_ID, MorphStats.jumpBonus(mobJump(mob)), AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        addFrom(player, TotemAttributes.stepHeight(), STEP_ID, MorphStats.stepDelta(mobStep(player, type)), AttributeModifier.Operation.ADDITION);
        AttributeInstance jump = player.getAttribute(TotemAttributes.jumpStrength());
        if (jump != null && FORM_JUMP.containsKey(type)) { // to exactly this strength, from the player's own
            jump.addTransientModifier(Modifiers.of(FORM_JUMP_ID, FORM_JUMP.get(type) - jump.getBaseValue(), AttributeModifier.Operation.ADDITION));
        }
        if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
    }

    private static void add(Player player, AttributeSupplier mob, Attribute attribute, ResourceLocation id,
                            DoubleUnaryOperator toAmount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null || !mob.hasAttribute(attribute)) return;
        double amount = toAmount.applyAsDouble(mob.getBaseValue(attribute));
        if (amount == 0.0) return;
        instance.addTransientModifier(Modifiers.of(id, amount, operation));
    }

    private static void addFrom(Player player, Attribute attribute, ResourceLocation id, double amount,
                                AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null || amount == 0.0) return;
        instance.addTransientModifier(Modifiers.of(id, amount, operation));
    }

    /**
     * The mob's jump strength as 1.21 has it: 1.20.1 gives only horses, donkeys, mules and llamas one
     * ({@code horse.jump_strength}); every other mob jumps at the player's 0.42.
     */
    private static double mobJump(AttributeSupplier mob) {
        return mob.hasAttribute(Attributes.JUMP_STRENGTH) ? mob.getBaseValue(Attributes.JUMP_STRENGTH) : TotemAttributes.PLAYER_JUMP;
    }

    /** The mob's step height: a field on each mob in 1.20.1 (an attribute in 1.21), so read from a throwaway one. */
    private static double mobStep(Player player, EntityType<?> type) {
        net.minecraft.world.entity.Entity probe = type.create(player.level());
        if (probe == null) return MorphStats.PLAYER_STEP_HEIGHT;
        double step = probe.maxUpStep();
        probe.discard();
        return step;
    }

    private static void clear(Player player, Attribute attribute, ResourceLocation id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) Modifiers.remove(instance, id);
    }

    private static boolean fits(ServerPlayer player, EntityType<?> type) {
        AABB box = type.getDimensions().makeBoundingBox(player.position());
        return player.level().noCollision(player, box);
    }

    /** Monsters already chasing the player lose interest; {@link MorphEvents} stops new ones. */
    private static void dropMonsterAggro(ServerPlayer player) {
        AABB area = player.getBoundingBox().inflate(AGGRO_DROP_RADIUS);
        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, area, m -> m.getTarget() == player)) {
            if (MorphEvents.ignoresMorphed(mob, player)) mob.setTarget(null);
        }
    }
}
