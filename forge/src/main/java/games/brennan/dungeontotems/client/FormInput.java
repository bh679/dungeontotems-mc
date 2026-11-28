package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.FormUse;
import games.brennan.dungeontotems.morph.Morphs;
import games.brennan.dungeontotems.net.FormAttackPayload;
import games.brennan.dungeontotems.net.FormJumpPayload;
import games.brennan.dungeontotems.net.FormUsePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.InputEvent;
import games.brennan.dungeontotems.net.TotemNetwork;

/**
 * A morphed player's keys, sent to the server where the form does something of its own:
 * <ul>
 *   <li>"Use" with an empty hand: the form's move, or its sound ({@link FormUse}). Doors, chests,
 *       buttons, levers, beds and the like still work, and you can still trade with villagers.</li>
 *   <li>"Attack" on the air: a shot (llama, snow golem), a goat's ram, or the form's sound. Attacks on mobs
 *       and blocks go the usual way.</li>
 *   <li>"Jump" as a camel: its dash. Moving while curled up as an armadillo: it unrolls.</li>
 * </ul>
 */
@EventBusSubscriber(modid = DungeonTotems.MOD_ID, value = Dist.CLIENT)
public final class FormInput {

    private static boolean jumpWasDown;

    private FormInput() {}

    @SubscribeEvent
    public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        if (Morphs.formOf(player).isEmpty() || !player.getMainHandItem().isEmpty()) return;
        HitResult hit = minecraft.hitResult;
        if (hit instanceof EntityHitResult entity && entity.getEntity() instanceof Merchant) return; // you can still trade
        if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK && FormUse.isUsable(player.level(), block.getBlockPos())) return;
        event.setCanceled(true);
        event.setSwingHand(false);
        TotemNetwork.sendToServer(FormUsePayload.INSTANCE);
    }

    @SubscribeEvent
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !event.isAttack() || Morphs.formOf(player).isEmpty()) return;
        boolean air = minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS;
        if (!air) return; // mobs and blocks go the usual way
        event.setCanceled(true);
        event.setSwingHand(false);
        TotemNetwork.sendToServer(FormAttackPayload.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        EntityType<?> form = player == null ? null : Morphs.formOf(player).orElse(null);
        boolean jumpDown = minecraft.options.keyJump.isDown();
        if (form != null && FormUse.OWN_JUMP.contains(form) && jumpDown && !jumpWasDown && minecraft.screen == null) {
            TotemNetwork.sendToServer(FormJumpPayload.INSTANCE);
        }
        jumpWasDown = jumpDown;

    }
}
