package games.brennan.dungeontotems.registry;

import com.mojang.serialization.Codec;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.Curse;
import games.brennan.dungeontotems.TotemItem;
import games.brennan.dungeontotems.advancement.TotemAdvancements;
import games.brennan.dungeontotems.advancement.TotemTriggers;
import games.brennan.dungeontotems.effect.CurseEffect;
import games.brennan.dungeontotems.effect.HeartbreakEffect;
import games.brennan.dungeontotems.data.DataKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Every registry entry, tag and key the mod owns. */
public final class TotemRegistries {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DungeonTotems.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, DungeonTotems.MOD_ID);

    /** Three fewer hearts per level, never below half a heart (see {@link HeartbreakEffect}). */
    public static final RegistryObject<MobEffect> HEARTBREAK = EFFECTS.register("heartbreak",
        () -> new HeartbreakEffect(0xB0213A));

    /** You are a random animal while it lasts (see {@code morph/}). Milk does not cure it. */
    public static final RegistryObject<MobEffect> MORPHED = EFFECTS.register("morphed",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x7A5CC8, false));

    /**
     * You are a zombie until you eat a golden apple while weakened, or die when it
     * runs out. Milk does not cure it. Other mods find it by id ({@code dungeontotems:zombified}).
     */
    public static final RegistryObject<MobEffect> ZOMBIFIED = EFFECTS.register("zombified",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x3E6B2F, false));

    /** Hit something within the timer, or take damage until you do (see {@code curse/CurseTimers}). */
    public static final RegistryObject<MobEffect> RAGE = EFFECTS.register("rage",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x8B0000, false));

    /** Give a gift within the timer, or take damage until you do (see {@code curse/CurseTimers}). */
    public static final RegistryObject<MobEffect> LONELY = EFFECTS.register("lonely",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0xE68C9C, false));

    /** Hurt anything and it costs you (see {@code curse/VowOfPeace}). Milk does not lift it. */
    public static final RegistryObject<MobEffect> VOW_OF_PEACE = EFFECTS.register("vow_of_peace",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0xE4E4EC, false));

    /** Eat anything but melon and it costs you (see {@code curse/RuleCurses}). */
    public static final RegistryObject<MobEffect> MELON_DIET = EFFECTS.register("melon_diet",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x62A236, false));

    /** Open a chest, barrel or ender chest and it costs you (see {@code curse/RuleCurses}). */
    public static final RegistryObject<MobEffect> RESTRAINED = EFFECTS.register("restrained",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x8A6A3A, false));

    /** You lose air even out of water, and drown on land (see {@code curse/Breathless}). */
    public static final RegistryObject<MobEffect> BREATHLESS = EFFECTS.register("breathless",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x56B8FF, false));

    /** Phantoms come for you, by day as well as night (see {@code curse/InsomniaPhantoms}). */
    public static final RegistryObject<MobEffect> INSOMNIA = EFFECTS.register("insomnia",
        () -> new CurseEffect(MobEffectCategory.HARMFUL, 0x2C3664, false));

    /** The entity type a player is shown and played as, by id; empty for their own body. */
    public static final DataKey<String> FORM =
        DataKey.create("form", () -> "", Codec.STRING);

    /** Game time each curse's after-save protection ends (by Curse name); see {@code curse/SavedCurseShield}. */
    public static final DataKey<Map<String, Long>> SAVED_UNTIL =
        DataKey.create("saved_until", () -> Map.of(), Codec.unboundedMap(Codec.STRING, Codec.LONG));

    /** Game time of your last hit, for Rage. */
    public static final DataKey<Long> LAST_HIT =
        DataKey.create("last_hit", () -> 0L, Codec.LONG);

    /** Game time of your last gift, for Lonely. */
    public static final DataKey<Long> LAST_GIFT =
        DataKey.create("last_gift", () -> 0L, Codec.LONG);

    /** Curses popped since you last died, still running (see {@code curse/CurseTrials}). */
    public static final DataKey<List<String>> CURSE_TRIALS =
        DataKey.create("curse_trials", () -> List.of(), Codec.STRING.listOf());

    /**
     * What you had in your hotbar and offhand when you were Zombified, one of each; only those may go into
     * your locked pack (see {@code curse/ZombiePack}). Not kept through death, which ends the curse.
     */
    public static final DataKey<List<ItemStack>> ZOMBIE_CARRIED =
        DataKey.create("zombie_carried", () -> List.of(), ItemStack.CODEC.listOf());

    public static final Map<Curse, RegistryObject<TotemItem>> TOTEMS = registerTotems();

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

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        TotemAttributes.register(modBus);
        EFFECTS.register(modBus);
        // CriteriaTriggers' map isn't thread-safe and mods construct in parallel: register on the main thread.
        modBus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) -> event.enqueueWork(TotemTriggers::register));
        TotemAdvancements.register(modBus);
        modBus.addListener(TotemRegistries::addToCreativeTab);
    }

    private static Map<Curse, RegistryObject<TotemItem>> registerTotems() {
        Map<Curse, RegistryObject<TotemItem>> totems = new EnumMap<>(Curse.class);
        for (Curse curse : Curse.values()) {
            totems.put(curse, ITEMS.register(curse.itemName(),
                () -> new TotemItem(curse, new Item.Properties().stacksTo(curse.maxStack()).rarity(Rarity.UNCOMMON))));
        }
        return Map.copyOf(totems);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.COMBAT) return;
        for (Curse curse : Curse.values()) {
            event.accept(TOTEMS.get(curse));
        }
    }
}
