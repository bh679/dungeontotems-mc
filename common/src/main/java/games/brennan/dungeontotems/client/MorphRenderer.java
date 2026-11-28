package games.brennan.dungeontotems.client;

import games.brennan.dungeontotems.event.ClientEvents;
import net.minecraft.world.entity.EntitySpawnReason;
import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.morph.FormAction;
import games.brennan.dungeontotems.morph.Morphs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import org.joml.Quaternionf;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws a morphed player as their form: the player model is cancelled and a client-only dummy of
 * the mob is drawn in its place, posed from the player every frame and animated every tick.
 */
public final class MorphRenderer {

    /** One dummy per morphed player, rebuilt when their form changes. */
    private static final Map<UUID, LivingEntity> DUMMIES = new ConcurrentHashMap<>();
    /** How far a form without an eat pose lowers its head to eat. */
    private static final float EATING_PITCH = 60.0F;
    private static int handsReached;

    private MorphRenderer() {}

    /** The stand-in drawn for a morphed player, if there is one yet. */
    static LivingEntity standIn(Player player) {
        return DUMMIES.get(player.getUUID());
    }

    /** The stand-in for a morphed player, made now if none has been drawn yet (in first person none is). */
    @Nullable
    static LivingEntity standInFor(Player player) {
        return dummyFor(player);
    }

    static void forget(Player player) {
        DUMMIES.remove(player.getUUID());
    }

    /**
     * 26.x draws from render states: the player's submit is cancelled, and the stand-in is extracted to its
     * own state and submitted in its place.
     */
    public static void onRenderPlayer(ClientEvents.RenderPlayerPre event) {
        AvatarRenderState playerState = event.getRenderState();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getEntity(playerState.id) instanceof Player player)) return;
        LivingEntity dummy = dummyFor(player);
        if (dummy == null) return;

        event.setCanceled(true);
        poseLike(dummy, player);
        float partialTick = event.getPartialTick();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderState formState = dispatcher.extractEntity(dummy, partialTick);
        if (formState instanceof LivingEntityRenderState living && inPreview(playerState, player, partialTick)) {
            // The inventory preview turns the player's state, not the player: the form follows the state there.
            living.bodyRot = playerState.bodyRot;
            living.yRot = playerState.yRot;
            living.xRot = playerState.xRot;
        }
        formState.lightCoords = playerState.lightCoords;
        formState.outlineColor = playerState.outlineColor;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        tilt(pose, dummy, player, partialTick);
        CameraRenderState camera = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        dispatcher.getRenderer(formState).submit(formState, pose, event.getSubmitNodeCollector(), camera);
        pose.popPose();
    }

    /** True when this player state was turned away from the player's own body: the inventory's preview. */
    private static boolean inPreview(AvatarRenderState state, Player player, float partialTick) {
        return Math.abs(Mth.wrapDegrees(state.bodyRot - Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot))) > 0.01F;
    }

    /** An attack's whole-body tilt ({@link FormAnimator#tiltDegrees}), forward about the axis across the body. */
    private static void tilt(PoseStack pose, LivingEntity dummy, Player player, float partialTick) {
        float degrees = FormAnimator.tiltDegrees(player.getUUID(), partialTick);
        if (degrees == 0.0F) return;
        float pivot = FormAnimator.tiltsAboutMiddle(player.getUUID()) ? dummy.getBbHeight() / 2.0F : 0.0F;
        float yaw = Mth.lerp(partialTick, dummy.yBodyRotO, dummy.yBodyRot) * Mth.DEG_TO_RAD;
        pose.translate(0.0F, pivot, 0.0F);
        pose.rotate(new Quaternionf().rotateAxis(degrees * Mth.DEG_TO_RAD, Mth.cos(yaw), 0.0F, Mth.sin(yaw))); // nose toward the facing
        pose.translate(0.0F, -pivot, 0.0F);
    }

    /**
     * In first person a morphed player shows no player hands: a form with arms shows its own ({@link FormArms}),
     * any other nothing. {@code ItemInHandRendererMixin} replaces the hand pass before these events fire;
     * they stay for anything that draws hands or arms another way.
     */
    public static void onRenderHand(ClientEvents.RenderHand event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && Morphs.formOf(player).isPresent() && !FormArms.hasArms(player)) {
            handsReached++;
            event.setCanceled(true);
        }
    }

    public static void onRenderArm(ClientEvents.RenderArm event) {
        Player player = Minecraft.getInstance().player; // first-person arms are only ever the local player's
        if (player == null || Morphs.formOf(player).isEmpty()) return;
        event.setCanceled(true);
        if (FormArms.hasArms(player)) {
            FormArms.render(player, event.getArm(), event.getPoseStack(), event.getSubmitNodeCollector(), event.getLightCoords());
        } else {
            handsReached++;
        }
    }

    /** How often an armless form's first-person hands got as far as these events ({@link FormSelfTest} expects 0). */
    static int handsReached() {
        return handsReached;
    }

    /** Animations advance once a tick (see {@link FormAnimator}). */
    public static void onClientTick(ClientEvents.Tick event) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            DUMMIES.clear();
            return;
        }
        for (Player player : level.players()) {
            LivingEntity dummy = DUMMIES.get(player.getUUID());
            if (dummy != null) FormAnimator.tick(dummy, player);
        }
        DUMMIES.keySet().removeIf(id -> {
            boolean gone = level.getPlayerByUUID(id) == null;
            if (gone) FormAnimator.forget(id);
            return gone;
        });
    }

    /** Shows a form's action (a ram, a swing, a graze) on a morphed player's stand-in. */
    static void perform(Player player, int action) {
        LivingEntity dummy = dummyFor(player);
        if (dummy != null && action >= 0 && action < FormAction.values().length) {
            FormAnimator.perform(dummy, player.getUUID(), FormAction.values()[action]);
        }
    }

    private static LivingEntity dummyFor(Player player) {
        EntityType<?> form = Morphs.formOf(player).orElse(null);
        if (form == null) {
            DUMMIES.remove(player.getUUID());
            return null;
        }
        LivingEntity existing = DUMMIES.get(player.getUUID());
        if (existing != null && existing.getType() == form) return existing;
        if (!(form.create(player.level(), Morphs.STAND_IN) instanceof LivingEntity created)) return null;
        // 26.x renderers read the entity id; real ids are positive, so a negative one never clashes.
        created.setId(-1 - player.getId());
        if (created instanceof Bat bat) bat.setResting(false); // a new bat hangs upside down
        created.setSilent(true); // its own tick runs for animations; the player makes the sounds
        created.setPos(player.getX(), player.getY(), player.getZ());
        created.setOldPosAndRot();
        DUMMIES.put(player.getUUID(), created);
        return created;
    }

    /** Copies look, body turn, motion state and held/worn items from the player. */
    private static void poseLike(LivingEntity dummy, Player player) {
        dummy.setYRot(player.getYRot());
        dummy.yRotO = player.yRotO;
        float pitch = FormAnimator.headDown(player.getUUID()) ? EATING_PITCH : player.getXRot();
        dummy.setXRot(pitch);
        dummy.xRotO = FormAnimator.headDown(player.getUUID()) ? EATING_PITCH : player.xRotO;
        // The body faces where you face, as a ridden horse follows its rider (a player's own body only turns
        // lazily, which left four-legged forms half-turned); a curled armadillo doesn't turn at all.
        Float curled = FormAnimator.curledYaw(player.getUUID());
        dummy.yBodyRot = curled != null ? curled : player.getYRot();
        dummy.yBodyRotO = curled != null ? curled : player.yRotO;
        dummy.yHeadRot = curled != null ? curled : player.getYRot();
        dummy.yHeadRotO = curled != null ? curled : player.yRotO;
        dummy.setOnGround(player.onGround());
        dummy.setShiftKeyDown(player.isShiftKeyDown());
        dummy.setInvisible(player.isInvisible());
        dummy.hurtTime = player.hurtTime;
        dummy.deathTime = player.deathTime;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ANIMAL_ARMOR) continue;
            dummy.setItemSlot(slot, player.getItemBySlot(slot));
        }
    }
}
