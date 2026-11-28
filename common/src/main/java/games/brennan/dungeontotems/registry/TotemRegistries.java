package games.brennan.dungeontotems.registry;

import com.mojang.serialization.Codec;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.TotemItem;
import games.brennan.dungeontotems.advancement.TotemTriggers;
import games.brennan.dungeontotems.effect.CurseEffect;
import games.brennan.dungeontotems.effect.HeartbreakEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import games.brennan.dungeontotems.platform.DataKey;
import games.brennan.dungeontotems.platform.Platform;
import games.brennan.dungeontotems.platform.Services;
import net.minecraft.core.Holder;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Every registry entry, tag and key the mod owns. */
public final class TotemRegistries {

    private static final Platform PLATFORM = Services.PLATFORM;

    /** Three fewer hearts per level, never below half a heart (see {@link HeartbreakEffect}). */
    public static final Holder<MobEffect> HEARTBREAK = PLATFORM.registerEffect("heartbreak",
        () -> new HeartbreakEffect(0xB0213A));

    /** You are a random animal while it lasts (see {@code morph/}). Milk does not cure it. */
    public static final Holder<MobEffect> MORPHED = PLATFORM.registerEffect("morphed",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x7A5CC8, false));

    /**
     * You are a zombie until you eat a golden apple while weakened, or die when it
     * runs out. Milk does not cure it. Other mods find it by id ({@code dungeontotems:zombified}).
     */
    public static final Holder<MobEffect> ZOMBIFIED = PLATFORM.registerEffect("zombified",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x3E6B2F, false));

    /** Hit something within the timer, or take damage until you do (see {@code curse/CurseTimers}). */
    public static final Holder<MobEffect> RAGE = PLATFORM.registerEffect("rage",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x8B0000, false));

    /** Give a gift within the timer, or take damage until you do (see {@code curse/CurseTimers}). */
    public static final Holder<MobEffect> LONELY = PLATFORM.registerEffect("lonely",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0xE68C9C, false));

    /** Hurt anything and it costs you (see {@code curse/VowOfPeace}). Milk does not lift it. */
    public static final Holder<MobEffect> VOW_OF_PEACE = PLATFORM.registerEffect("vow_of_peace",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0xE4E4EC, false));

    /** Eat anything but melon and it costs you (see {@code curse/RuleCurses}). */
    public static final Holder<MobEffect> MELON_DIET = PLATFORM.registerEffect("melon_diet",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x62A236, false));

    /** Open a chest, barrel or ender chest and it costs you (see {@code curse/RuleCurses}). */
    public static final Holder<MobEffect> RESTRAINED = PLATFORM.registerEffect("restrained",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x8A6A3A, false));

    /** You lose air even out of water, and drown on land (see {@code curse/Breathless}). */
    public static final Holder<MobEffect> BREATHLESS = PLATFORM.registerEffect("breathless",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x56B8FF, false));

    /** Phantoms come for you, by day as well as night (see {@code curse/InsomniaPhantoms}). */
    public static final Holder<MobEffect> INSOMNIA = PLATFORM.registerEffect("insomnia",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x2C3664, false));

    /** The entity type a player is shown and played as, by id; empty for their own body. */
    public static final DataKey<String> FORM = PLATFORM.registerData("form",
        () -> "", Codec.STRING);

    /** Game time each curse's after-save protection ends (by Curse name); see {@code curse/SavedCurseShield}. */
    public static final DataKey<Map<String, Long>> SAVED_UNTIL = PLATFORM.registerData("saved_until",
        () -> Map.of(), Codec.unboundedMap(Codec.STRING, Codec.LONG));

    /** Vanilla effects (Weakness, Mining Fatigue, ...) on you that are curses, by Curse name (see {@code curse/CurseGuard}). */
    public static final DataKey<List<String>> CURSE_MARKS = PLATFORM.registerData("curse_marks",
        () -> List.of(), Codec.STRING.listOf());

    /** Game time of your last hit, for Rage. */
    public static final DataKey<Long> LAST_HIT = PLATFORM.registerData("last_hit",
        () -> 0L, Codec.LONG);

    /** Game time of your last gift, for Lonely. */
    public static final DataKey<Long> LAST_GIFT = PLATFORM.registerData("last_gift",
        () -> 0L, Codec.LONG);

    /** Curses popped since you last died, still running (see {@code curse/CurseTrials}). */
    public static final DataKey<List<String>> CURSE_TRIALS = PLATFORM.registerData("curse_trials",
        () -> List.of(), Codec.STRING.listOf());

    /**
     * What you had in your hotbar and offhand when you were Zombified, one of each; only those may go into
     * your locked pack (see {@code curse/ZombiePack}). Not kept through death, which ends the curse.
     */
    public static final DataKey<List<ItemStack>> ZOMBIE_CARRIED = PLATFORM.registerData("zombie_carried",
        () -> List.of(), ItemStack.CODEC.listOf());

    public static final Map<Curse, Supplier<TotemItem>> TOTEMS = registerTotems();

    /** Mobs that hunt a Zombified player as they would anyone (sculk: the warden, Deeper Darker's mobs). */
    public static final TagKey<EntityType<?>> HUNTS_ZOMBIE_PLAYERS =
        TagKey.create(Registries.ENTITY_TYPE, DungeonTotems.id("hunts_zombie_players"));

    /** Forms that fly: always airborne, no walking or jumping (see {@code morph/MorphFlight}). */
    public static final TagKey<EntityType<?>> FLYING_FORMS =
        TagKey.create(Registries.ENTITY_TYPE, DungeonTotems.id("flying_forms"));

    public static final ResourceKey<DamageType> RAGE_FAILED =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("rage_failed"));
    public static final ResourceKey<DamageType> LONELINESS =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("loneliness"));
    public static final ResourceKey<DamageType> BROKEN_VOW =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("broken_vow"));
    public static final ResourceKey<DamageType> BROKEN_DIET =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("broken_diet"));
    public static final ResourceKey<DamageType> BROKEN_RESTRAINT =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("broken_restraint"));
    public static final ResourceKey<DamageType> SUFFOCATION =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("breathless"));
    /** A bee-form player who stung and didn't turn back in time. */
    public static final ResourceKey<DamageType> LOST_STINGER =
        ResourceKey.create(Registries.DAMAGE_TYPE, DungeonTotems.id("lost_stinger"));

    private TotemRegistries() {}

    /** Loads this class (registering everything above) and the advancement triggers. */
    public static void init() {
        TotemTriggers.init();
    }

    private static Map<Curse, Supplier<TotemItem>> registerTotems() {
        Map<Curse, Supplier<TotemItem>> totems = new EnumMap<>(Curse.class);
        for (Curse curse : Curse.values()) {
            // registerItem gives the properties the item's id, which 26.x requires.
            totems.put(curse, PLATFORM.registerItem(curse.itemName(),
                properties -> new TotemItem(curse, properties.stacksTo(curse.maxStack()).rarity(Rarity.UNCOMMON))));
        }
        return Map.copyOf(totems);
    }

    /** Every totem, in table order, for the Combat creative tab (each loader adds them there). */
    public static List<ItemStack> creativeTabStacks() {
        return java.util.Arrays.stream(Curse.values()).map(curse -> new ItemStack(TOTEMS.get(curse).get())).toList();
    }
}
