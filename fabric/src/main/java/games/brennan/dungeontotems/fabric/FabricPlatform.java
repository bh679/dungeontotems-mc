package games.brennan.dungeontotems.fabric;

import com.mojang.serialization.Codec;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.MorphForm;
import games.brennan.dungeontotems.platform.DataKey;
import games.brennan.dungeontotems.platform.Platform;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link Platform} on Fabric: registers straight into the built-in registries (Fabric's way: during
 * {@code onInitialize}), Data Attachments, Fabric networking, and the morph table read by {@link MorphFormTable}.
 */
public final class FabricPlatform implements Platform {

    @Override
    public String loaderName() {
        return "fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public Holder<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> factory) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, DungeonTotems.id(name), factory.get());
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory) {
        // 26.x items need their id in their properties.
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, DungeonTotems.id(name));
        T item = Registry.register(BuiltInRegistries.ITEM, key, factory.apply(new Item.Properties().setId(key)));
        return () -> item;
    }

    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String name, Supplier<T> factory) {
        T trigger = Registry.register(BuiltInRegistries.TRIGGER_TYPES, DungeonTotems.id(name), factory.get());
        return () -> trigger;
    }

    @Override
    public <T> DataKey<T> registerData(String name, Supplier<T> defaultValue, Codec<T> codec) {
        AttachmentType<T> type = AttachmentRegistry.create(DungeonTotems.id(name),
            builder -> builder.initializer(defaultValue).persistent(codec));
        return new DataKey<>(name, defaultValue, codec, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> AttachmentType<T> type(DataKey<T> key) {
        return (AttachmentType<T>) key.handle();
    }

    @Override
    public <T> T getData(Entity entity, DataKey<T> key) {
        return entity.getAttachedOrCreate(type(key));
    }

    @Override
    public <T> void setData(Entity entity, DataKey<T> key, T value) {
        entity.setAttached(type(key), value);
    }

    @Override
    public boolean hasData(Entity entity, DataKey<?> key) {
        return entity.hasAttached(type(key));
    }

    @Override
    public boolean hasChannel(ServerPlayer player, CustomPacketPayload payload) {
        return ServerPlayNetworking.canSend(player, payload.type());
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public Collection<ServerPlayer> trackingPlayers(Entity entity) {
        return PlayerLookup.tracking(entity);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        if (ClientPlayNetworking.canSend(payload.type())) ClientPlayNetworking.send(payload);
    }

    @Override
    public Map<ResourceKey<EntityType<?>>, MorphForm> morphForms() {
        return MorphFormTable.current();
    }

    @Override
    public boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into) {
        return true; // Fabric has no conversion event
    }

    @Override
    public void onLivingConvert(LivingEntity from, LivingEntity to) {
        // Fabric's own conversion event (ServerLivingEntityEvents.MOB_CONVERSION) fires from convertTo itself.
    }
}
