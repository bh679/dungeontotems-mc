package games.brennan.dungeontotems.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/** Client-only events, named after NeoForge's. Only the client fires or loads them. */
public final class ClientEvents {

    private ClientEvents() {}

    /** End of a client tick. */
    public static final class Tick extends DtEvent {
    }

    /** A screen has drawn its contents (a container's slots and items); draw over them here. */
    public static final class ScreenForeground extends DtEvent {
        private final Screen screen;
        private final GuiGraphicsExtractor graphics;

        public ScreenForeground(Screen screen, GuiGraphicsExtractor graphics) {
            this.screen = screen;
            this.graphics = graphics;
        }

        public Screen getScreen() {
            return screen;
        }

        public GuiGraphicsExtractor getGuiGraphics() {
            return graphics;
        }
    }

    /** The use or attack key was pressed. Cancel to skip vanilla's action; say whether the hand swings. */
    public static final class InteractionKey extends DtEvent {
        private final boolean useItem;
        private final InteractionHand hand;
        private boolean swingHand = true;

        public InteractionKey(boolean useItem, InteractionHand hand) {
            this.useItem = useItem;
            this.hand = hand;
        }

        public boolean isUseItem() {
            return useItem;
        }

        public boolean isAttack() {
            return !useItem;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public boolean shouldSwingHand() {
            return swingHand;
        }

        public void setSwingHand(boolean swingHand) {
            this.swingHand = swingHand;
        }
    }

    /** A player is about to be drawn. Cancel to draw something else instead. */
    public static final class RenderPlayerPre extends DtEvent {
        private final AvatarRenderState renderState;
        private final PoseStack poseStack;
        private final SubmitNodeCollector collector;
        private final float partialTick;

        public RenderPlayerPre(AvatarRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, float partialTick) {
            this.renderState = renderState;
            this.poseStack = poseStack;
            this.collector = collector;
            this.partialTick = partialTick;
        }

        public AvatarRenderState getRenderState() {
            return renderState;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getSubmitNodeCollector() {
            return collector;
        }

        public float getPartialTick() {
            return partialTick;
        }
    }

    /** First-person hands are about to be drawn. Cancel for none. */
    public static final class RenderHand extends DtEvent {
    }

    /** A first-person bare arm is about to be drawn. Cancel to draw something else. */
    public static final class RenderArm extends DtEvent {
        private final HumanoidArm arm;
        private final PoseStack poseStack;
        private final SubmitNodeCollector collector;
        private final int lightCoords;

        public RenderArm(HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
            this.arm = arm;
            this.poseStack = poseStack;
            this.collector = collector;
            this.lightCoords = lightCoords;
        }

        public HumanoidArm getArm() {
            return arm;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getSubmitNodeCollector() {
            return collector;
        }

        public int getLightCoords() {
            return lightCoords;
        }
    }

    /** A character is about to be typed into a screen. */
    public static final class CharTyped extends DtEvent {
        private final Screen screen;
        private final int codePoint;

        public CharTyped(Screen screen, int codePoint) {
            this.screen = screen;
            this.codePoint = codePoint;
        }

        public Screen getScreen() {
            return screen;
        }

        public int getCodePoint() {
            return codePoint;
        }
    }

    /** A chat message (or command) is about to be sent. */
    public static final class ChatSend extends DtEvent {
        private String message;

        public ChatSend(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /** A chat or system message arrived. */
    public static final class ChatReceived extends DtEvent {
        private Component message;
        private final boolean system;
        private final boolean overlay;

        public ChatReceived(Component message, boolean system, boolean overlay) {
            this.message = message;
            this.system = system;
            this.overlay = overlay;
        }

        public Component getMessage() {
            return message;
        }

        public void setMessage(Component message) {
            this.message = message;
        }

        public boolean isSystem() {
            return system;
        }

        /** An action-bar line. */
        public boolean isOverlay() {
            return overlay;
        }
    }

    /** An item's tooltip lines, for changing in place. */
    public static final class ItemTooltip extends DtEvent {
        private final ItemStack stack;
        private final List<Component> toolTip;
        @Nullable private final Player player;

        public ItemTooltip(ItemStack stack, List<Component> toolTip, @Nullable Player player) {
            this.stack = stack;
            this.toolTip = toolTip;
            this.player = player;
        }

        public ItemStack getItemStack() {
            return stack;
        }

        public List<Component> getToolTip() {
            return toolTip;
        }

        @Nullable
        public Player getEntity() {
            return player;
        }
    }

    /** An effect's hover lines in the inventory's effect list. */
    public static final class EffectTooltip extends DtEvent {
        private final MobEffectInstance effectInstance;
        private final List<Component> tooltip;

        public EffectTooltip(MobEffectInstance effectInstance, List<Component> tooltip) {
            this.effectInstance = effectInstance;
            this.tooltip = tooltip;
        }

        public MobEffectInstance getEffectInstance() {
            return effectInstance;
        }

        public List<Component> getTooltip() {
            return tooltip;
        }
    }
}
