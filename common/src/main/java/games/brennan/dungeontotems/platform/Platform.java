package games.brennan.dungeontotems.platform;

import com.mojang.serialization.Codec;
import games.brennan.dungeontotems.morph.MorphForm;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * What the mod needs from its loader (NeoForge or Fabric), found with {@link java.util.ServiceLoader}
 * through {@link Services}. Everything else in {@code common/} is plain Minecraft.
 */
public interface Platform {

    /** "neoforge" or "fabric". */
    String loaderName();

    boolean isModLoaded(String modId);

    // Registration. Called while the mod sets up, in the order the entries are declared.

    Holder<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> factory);

    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory);

    <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String name, Supplier<T> factory);

    /** Saved data on an entity, persisted with {@code codec}; not copied on death or respawn. */
    <T> DataKey<T> registerData(String name, Supplier<T> defaultValue, Codec<T> codec);

    // Per-entity data.

    <T> T getData(Entity entity, DataKey<T> key);

    <T> void setData(Entity entity, DataKey<T> key, T value);

    boolean hasData(Entity entity, DataKey<?> key);

    // Network. Payloads are registered by each loader.

    /** Whether the player's connection has our channel for this payload (a vanilla client or a mock player doesn't). */
    boolean hasChannel(ServerPlayer player, CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    /** Everyone else who can see {@code entity}. */
    java.util.Collection<ServerPlayer> trackingPlayers(Entity entity);

    /** Client only. */
    void sendToServer(CustomPacketPayload payload);

    // Data.

    /** The {@code dungeontotems:morph_forms} table as loaded now, by entity type. */
    Map<ResourceKey<EntityType<?>>, MorphForm> morphForms();

    // Hooks other mods listen to.

    /** Whether another mod lets {@code entity} turn into {@code into}. */
    boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into);

    /** Tells other mods {@code from} became {@code to}. */
    void onLivingConvert(LivingEntity from, LivingEntity to);
}
