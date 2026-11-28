package games.brennan.dungeontotems.forge;

import games.brennan.dungeontotems.platform.event.ClientChatEvent;
import games.brennan.dungeontotems.platform.event.ClientChatReceivedEvent;
import games.brennan.dungeontotems.platform.event.ContainerScreenEvent;
import games.brennan.dungeontotems.platform.event.InputEvent;
import games.brennan.dungeontotems.platform.event.RenderArmEvent;
import games.brennan.dungeontotems.platform.event.RenderHandEvent;
import games.brennan.dungeontotems.platform.event.RenderPlayerEvent;
import games.brennan.dungeontotems.platform.event.ScreenEvent;

import static games.brennan.dungeontotems.forge.ForgeBridges.forward;

/** The client-only half of {@link ForgeEventBridge}: only called on a physical client. */
final class ForgeClientEventBridge {

    private ForgeClientEventBridge() {}

    static void register() {
        forward(net.minecraftforge.client.event.RenderPlayerEvent.Pre.class, RenderPlayerEvent.Pre.class,
            f -> new RenderPlayerEvent.Pre(f.getEntity(), f.getRenderer(), f.getPartialTick(), f.getPoseStack(),
                f.getMultiBufferSource(), f.getPackedLight()));
        forward(net.minecraftforge.client.event.RenderHandEvent.class, RenderHandEvent.class,
            f -> new RenderHandEvent(f.getHand(), f.getPoseStack(), f.getMultiBufferSource(), f.getPackedLight(),
                f.getPartialTick(), f.getItemStack()));
        forward(net.minecraftforge.client.event.RenderArmEvent.class, RenderArmEvent.class,
            f -> new RenderArmEvent(f.getPoseStack(), f.getMultiBufferSource(), f.getPackedLight(), f.getPlayer(), f.getArm()));
        forward(net.minecraftforge.client.event.InputEvent.InteractionKeyMappingTriggered.class, InputEvent.InteractionKeyMappingTriggered.class,
            f -> {
                int button = f.isAttack() ? InputEvent.InteractionKeyMappingTriggered.ATTACK
                    : f.isUseItem() ? InputEvent.InteractionKeyMappingTriggered.USE : InputEvent.InteractionKeyMappingTriggered.PICK;
                InputEvent.InteractionKeyMappingTriggered e = new InputEvent.InteractionKeyMappingTriggered(button, f.getKeyMapping(), f.getHand());
                e.setSwingHand(f.shouldSwingHand());
                return e;
            },
            (f, e) -> f.setSwingHand(e.shouldSwingHand()));
        forward(net.minecraftforge.client.event.ClientChatEvent.class, ClientChatEvent.class,
            f -> new ClientChatEvent(f.getMessage()), (f, e) -> f.setMessage(e.getMessage()));
        // Forge posts the Player and System subclasses; a listener on the base class gets both, as ours did.
        forward(net.minecraftforge.client.event.ClientChatReceivedEvent.class, ClientChatReceivedEvent.class,
            f -> f instanceof net.minecraftforge.client.event.ClientChatReceivedEvent.System system
                ? new ClientChatReceivedEvent.System(f.getMessage(), system.isOverlay())
                : new ClientChatReceivedEvent(f.getMessage(), f.getSender()),
            (f, e) -> f.setMessage(e.getMessage()));
        forward(net.minecraftforge.client.event.ScreenEvent.Render.Pre.class, ScreenEvent.Render.Pre.class,
            f -> new ScreenEvent.Render.Pre(f.getScreen(), f.getGuiGraphics(), f.getMouseX(), f.getMouseY(), f.getPartialTick()));
        forward(net.minecraftforge.client.event.ScreenEvent.Render.Post.class, ScreenEvent.Render.Post.class,
            f -> new ScreenEvent.Render.Post(f.getScreen(), f.getGuiGraphics(), f.getMouseX(), f.getMouseY(), f.getPartialTick()));
        forward(net.minecraftforge.client.event.ScreenEvent.CharacterTyped.Pre.class, ScreenEvent.CharacterTyped.Pre.class,
            f -> new ScreenEvent.CharacterTyped.Pre(f.getScreen(), f.getCodePoint(), f.getModifiers()));
        forward(net.minecraftforge.client.event.ContainerScreenEvent.Render.Foreground.class, ContainerScreenEvent.Render.Foreground.class,
            f -> new ContainerScreenEvent.Render.Foreground(f.getContainerScreen(), f.getGuiGraphics(), f.getMouseX(), f.getMouseY()));
    }
}
