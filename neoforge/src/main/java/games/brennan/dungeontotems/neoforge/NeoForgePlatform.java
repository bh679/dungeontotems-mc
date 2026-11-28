package games.brennan.dungeontotems.neoforge;

import com.mojang.serialization.Codec;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.MorphForm;
import games.brennan.dungeontotems.platform.DataKey;
import games.brennan.dungeontotems.platform.Platform;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** {@link Platform} on NeoForge: deferred registers, attachments, the payload distributor, the morph data map. */
public final class NeoForgePlatform implements Platform {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DungeonTotems.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, DungeonTotems.MOD_ID);
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DungeonTotems.MOD_ID);
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
        DeferredRegister.create(Registries.TRIGGER_TYPE, DungeonTotems.MOD_ID);

    /** The morph table: a data map on entity types, so a datapack can change it. */
    public static final DataMapType<EntityType<?>, MorphForm> MORPH_FORMS =
        DataMapType.builder(MorphForm.TABLE_ID, Registries.ENTITY_TYPE, MorphForm.CODEC).build();

    /** After {@link DungeonTotems#init()} has declared every entry. */
    public static void registerAll(IEventBus modBus) {
        ITEMS.register(modBus);
        EFFECTS.register(modBus);
        ATTACHMENTS.register(modBus);
        TRIGGERS.register(modBus);
    }

    @Override
    public String loaderName() {
        return "neoforge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public Holder<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> factory) {
        return EFFECTS.register(name, factory);
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> factory) {
        // registerItem gives the properties the item's id, which 26.x requires.
        return ITEMS.registerItem(name, factory);
    }

    @Override
    public <T extends CriterionTrigger<?>> Supplier<T> registerTrigger(String name, Supplier<T> factory) {
        return TRIGGERS.register(name, factory);
    }

    @Override
    public <T> DataKey<T> registerData(String name, Supplier<T> defaultValue, Codec<T> codec) {
        Supplier<AttachmentType<T>> type = ATTACHMENTS.register(name,
            () -> AttachmentType.builder(defaultValue).serialize(codec.fieldOf("value")).build());
        return new DataKey<>(name, defaultValue, codec, type);
    }

    @SuppressWarnings("unchecked")
    private static <T> AttachmentType<T> type(DataKey<T> key) {
        return ((Supplier<AttachmentType<T>>) key.handle()).get();
    }

    @Override
    public <T> T getData(Entity entity, DataKey<T> key) {
        return entity.getData(type(key));
    }

    @Override
    public <T> void setData(Entity entity, DataKey<T> key, T value) {
        entity.setData(type(key), value);
    }

    @Override
    public boolean hasData(Entity entity, DataKey<?> key) {
        return entity.hasData(type(key));
    }

    @Override
    public boolean hasChannel(ServerPlayer player, CustomPacketPayload payload) {
        return player.connection.hasChannel(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public Collection<ServerPlayer> trackingPlayers(Entity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return List.of();
        return level.getChunkSource().chunkMap.getPlayersWatching(entity);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public Map<ResourceKey<EntityType<?>>, MorphForm> morphForms() {
        return BuiltInRegistries.ENTITY_TYPE.getDataMap(MORPH_FORMS);
    }

    @Override
    public boolean canLivingConvert(LivingEntity entity, EntityType<? extends LivingEntity> into) {
        return EventHooks.canLivingConvert(entity, into, ticks -> {});
    }

    @Override
    public void onLivingConvert(LivingEntity from, LivingEntity to) {
        EventHooks.onLivingConvert(from, to);
    }
}
