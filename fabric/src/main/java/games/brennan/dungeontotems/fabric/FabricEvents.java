package games.brennan.dungeontotems.fabric;

import games.brennan.dungeontotems.platform.event.BlockEvent;
import games.brennan.dungeontotems.platform.event.EntityJoinLevelEvent;
import games.brennan.dungeontotems.platform.event.EntityLeaveLevelEvent;
import games.brennan.dungeontotems.platform.event.PlayerEvent;
import games.brennan.dungeontotems.platform.event.PlayerInteractEvent;
import games.brennan.dungeontotems.platform.event.RegisterCommandsEvent;
import games.brennan.dungeontotems.platform.event.ServerStartedEvent;
import games.brennan.dungeontotems.platform.event.TotemEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

/**
 * The shared events Fabric API already fires at Forge's spot (or near enough, noted). The rest come from our
 * own mixins in {@code fabric/mixin}, at Forge's exact patch points.
 */
final class FabricEvents {

    private FabricEvents() {}

    static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> TotemEvents.post(new ServerStartedEvent(server)));
        CommandRegistrationCallback.EVENT.register((dispatcher, context, selection) ->
            TotemEvents.post(new RegisterCommandsEvent(dispatcher, selection, context)));

        // Both at the end of PlayerList#respawn / ServerPlayer#restoreFrom, as Forge's.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
            TotemEvents.post(new PlayerEvent.PlayerRespawnEvent(newPlayer, alive)));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
            TotemEvents.post(new PlayerEvent.Clone(newPlayer, oldPlayer, !alive)));
        // After ServerEntity#addPairing, as Forge's.
        EntityTrackingEvents.START_TRACKING.register((entity, player) ->
            TotemEvents.post(new PlayerEvent.StartTracking(player, entity)));
        // Fabric fires these as the level starts and stops tracking an entity; Forge as it is added and removed.
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> TotemEvents.post(new EntityJoinLevelEvent(entity, level)));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> TotemEvents.post(new EntityLeaveLevelEvent(entity, level)));

        // ServerPlayerGameMode#useItem and MultiPlayerGameMode#useItem, at their top, as Forge's RightClickItem.
        UseItemCallback.EVENT.register((player, level, hand) -> {
            PlayerInteractEvent.RightClickItem event = new PlayerInteractEvent.RightClickItem(player, hand);
            if (!TotemEvents.post(event)) return InteractionResultHolder.pass(player.getItemInHand(hand));
            return new InteractionResultHolder<>(event.getCancellationResult(), player.getItemInHand(hand));
        });
        // Starting to break a block, both sides, as Forge's LeftClickBlock START.
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            PlayerInteractEvent.LeftClickBlock event = new PlayerInteractEvent.LeftClickBlock(player, pos, direction,
                PlayerInteractEvent.LeftClickBlock.Action.START);
            return TotemEvents.post(event) ? InteractionResult.FAIL : InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !TotemEvents.post(new BlockEvent.BreakEvent(level, pos, state, player)));
    }
}
