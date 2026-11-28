package games.brennan.dungeontotems.fabric;

import com.mojang.serialization.MapCodec;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.advancement.TotemAdvancements;
import games.brennan.dungeontotems.event.DtEvents;
import games.brennan.dungeontotems.event.LivingEvents;
import games.brennan.dungeontotems.event.PlayerEvents;
import games.brennan.dungeontotems.event.ServerEvents;
import games.brennan.dungeontotems.loot.EvokerTotems;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Fabric entry point: the common mod ({@link DungeonTotems}) plus Fabric's registrations, and the
 * common events from Fabric API callbacks where one sits where NeoForge's event does. The rest come from
 * this mod's own mixins ({@code fabric/mixin/}, through {@link FabricHooks}).
 */
public final class DungeonTotemsFabric implements ModInitializer {

    private static final ResourceKey<LootTable> EVOKER_LOOT =
        ResourceKey.create(Registries.LOOT_TABLE, Identifier.withDefaultNamespace("entities/evoker"));

    @Override
    public void onInitialize() {
        DungeonTotems.init();
        FabricNetwork.register();
        ResourceConditions.register(AdvancementsLaunched.TYPE);
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(MorphFormTable.ID, new MorphFormTable());
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
            .register(output -> TotemRegistries.creativeTabStacks().forEach(output::accept));
        LootTableEvents.MODIFY_DROPS.register((table, context, drops) -> {
            if (!table.is(EVOKER_LOOT)) return;
            List<ItemStack> swapped = EvokerTotems.swap(drops, context.getRandom());
            if (swapped == drops) return;
            drops.clear();
            drops.addAll(swapped);
        });
        callbacks();
        FabricGameTests.register();
    }

    /** Fabric API callbacks that fire where NeoForge's events do. */
    private static void callbacks() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DtEvents.SERVER_STARTED.post(new ServerEvents.Started(server)));
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
            DtEvents.REGISTER_COMMANDS.post(new ServerEvents.RegisterCommands(dispatcher, buildContext)));
        ServerPlayerEvents.JOIN.register(player -> DtEvents.LOGGED_IN.post(new PlayerEvents.LoggedIn(player)));
        ServerPlayerEvents.LEAVE.register(player -> DtEvents.LOGGED_OUT.post(new PlayerEvents.LoggedOut(player)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
            DtEvents.CLONE.post(new PlayerEvents.Clone(newPlayer, oldPlayer, !alive)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
            DtEvents.RESPAWN.post(new PlayerEvents.Respawn(newPlayer)));
        EntityTrackingEvents.START_TRACKING.register((target, player) ->
            DtEvents.START_TRACKING.post(new PlayerEvents.StartTracking(player, target)));
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> DtEvents.JOIN_LEVEL.post(new LivingEvents.JoinLevel(entity, level)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> DtEvents.LEAVE_LEVEL.post(new LivingEvents.LeaveLevel(entity, level)));
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !FabricHooks.breakBlockRefused(player, pos, state));
        UseItemCallback.EVENT.register((player, level, hand) -> rightClickItem(player, hand));
    }

    /** NeoForge returns the cancellation result; Fabric goes on after PASS, so a refusal is at least a FAIL. */
    static InteractionResult rightClickItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        PlayerEvents.RightClickItem event = FabricHooks.rightClickItem(player, hand);
        if (!event.isCanceled()) return InteractionResult.PASS;
        InteractionResult result = event.getCancellationResult();
        return result == InteractionResult.PASS ? InteractionResult.FAIL : result;
    }

    /** {@code {"condition": "dungeontotems:advancements_launched"}}: true once the tab is out ({@link TotemAdvancements}). */
    public static final class AdvancementsLaunched implements ResourceCondition {
        static final AdvancementsLaunched INSTANCE = new AdvancementsLaunched();
        static final ResourceConditionType<AdvancementsLaunched> TYPE =
            ResourceConditionType.create(TotemAdvancements.CONDITION_ID, MapCodec.unit(INSTANCE));

        private AdvancementsLaunched() {}

        @Override
        public ResourceConditionType<?> getType() {
            return TYPE;
        }

        @Override
        public boolean test(@Nullable RegistryOps.RegistryInfoLookup registryInfo) {
            return TotemAdvancements.launched();
        }
    }
}
