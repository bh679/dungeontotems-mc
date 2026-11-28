package games.brennan.dungeontotems.neoforge;

import games.brennan.dungeontotems.client.TimerCurseIcons;
import games.brennan.dungeontotems.event.ClientEvents;
import games.brennan.dungeontotems.event.DtClientEvents;
import games.brennan.dungeontotems.registry.TotemRegistries;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.GatherEffectScreenTooltipsEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderArmEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import static games.brennan.dungeontotems.neoforge.NeoForgeEvents.bridge;

/** The client's common events ({@link DtClientEvents}) from NeoForge's, and the Rage/Lonely effect icons. */
public final class NeoForgeClientEvents {

    private NeoForgeClientEvents() {}

    public static void register(IEventBus modBus) {
        IEventBus neo = NeoForge.EVENT_BUS;
        bridge(neo, DtClientEvents.TICK, ClientTickEvent.Post.class, e -> new ClientEvents.Tick());
        bridge(neo, DtClientEvents.SCREEN_FOREGROUND, ScreenEvent.Render.Foreground.class,
            e -> new ClientEvents.ScreenForeground(e.getScreen(), e.getGuiGraphics()));
        bridge(neo, DtClientEvents.INTERACTION_KEY, InputEvent.InteractionKeyMappingTriggered.class,
            e -> e.isUseItem() || e.isAttack() ? new ClientEvents.InteractionKey(e.isUseItem(), e.getHand()) : null, // not pick block
            (e, c) -> e.setSwingHand(c.shouldSwingHand()));
        bridge(neo, DtClientEvents.RENDER_PLAYER, RenderPlayerEvent.Pre.class,
            e -> new ClientEvents.RenderPlayerPre((net.minecraft.client.renderer.entity.state.AvatarRenderState) e.getRenderState(), e.getPoseStack(), e.getSubmitNodeCollector(), e.getPartialTick()));
        bridge(neo, DtClientEvents.RENDER_HAND, RenderHandEvent.class, e -> new ClientEvents.RenderHand());
        bridge(neo, DtClientEvents.RENDER_ARM, RenderArmEvent.class,
            e -> new ClientEvents.RenderArm(e.getArm(), e.getPoseStack(), e.getSubmitNodeCollector(), e.getLightCoords()));
        bridge(neo, DtClientEvents.CHAR_TYPED, ScreenEvent.CharacterTyped.Pre.class,
            e -> new ClientEvents.CharTyped(e.getScreen(), e.getCodePoint()));
        bridge(neo, DtClientEvents.CHAT_SEND, ClientChatEvent.class,
            e -> new ClientEvents.ChatSend(e.getMessage()), (e, c) -> e.setMessage(c.getMessage()));
        bridge(neo, DtClientEvents.CHAT_RECEIVED, ClientChatReceivedEvent.class,
            e -> new ClientEvents.ChatReceived(e.getMessage(), e.isSystem(),
                e instanceof ClientChatReceivedEvent.System system && system.isOverlay()),
            (e, c) -> e.setMessage(c.getMessage()));
        bridge(neo, DtClientEvents.ITEM_TOOLTIP, ItemTooltipEvent.class,
            e -> new ClientEvents.ItemTooltip(e.getItemStack(), e.getToolTip(), e.getEntity()));
        bridge(neo, DtClientEvents.EFFECT_TOOLTIP, GatherEffectScreenTooltipsEvent.class,
            e -> new ClientEvents.EffectTooltip(e.getEffectInstance(), e.getTooltip()));
        modBus.addListener(NeoForgeClientEvents::registerEffectIcons);
    }

    private static void registerEffectIcons(RegisterClientExtensionsEvent event) {
        event.registerMobEffect(TimerIcon.INSTANCE, TotemRegistries.RAGE.value());
        event.registerMobEffect(TimerIcon.INSTANCE, TotemRegistries.LONELY.value());
    }

    /** Rage's and Lonely's timer on the effect box's border ({@link TimerCurseIcons}). */
    private static final class TimerIcon implements IClientMobEffectExtensions {
        static final TimerIcon INSTANCE = new TimerIcon();

        @Override
        public boolean extractHudIcon(MobEffectInstance instance, Hud hud, GuiGraphicsExtractor graphics,
                                      int x, int y, int width, int height, int color) {
            TimerCurseIcons.extractHudIcon(instance, graphics, x, y);
            return false; // vanilla draws the icon on top
        }

        @Override
        public boolean extractInventoryIcon(MobEffectInstance instance, AbstractContainerScreen<?> screen, GuiGraphicsExtractor graphics,
                                            int x, int y, int width, int height, int color) {
            TimerCurseIcons.extractInventoryIcon(instance, screen, graphics, x, y);
            return false;
        }
    }
}
