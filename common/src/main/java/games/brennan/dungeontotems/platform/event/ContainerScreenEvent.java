package games.brennan.dungeontotems.platform.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** Client container screen events. */
public abstract class ContainerScreenEvent extends Event {
    private final AbstractContainerScreen<?> containerScreen;

    protected ContainerScreenEvent(AbstractContainerScreen<?> containerScreen) {
        this.containerScreen = containerScreen;
    }

    public AbstractContainerScreen<?> getContainerScreen() { return containerScreen; }

    public abstract static class Render extends ContainerScreenEvent {
        private final GuiGraphics guiGraphics;
        private final int mouseX;
        private final int mouseY;

        protected Render(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
            super(screen);
            this.guiGraphics = guiGraphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public GuiGraphics getGuiGraphics() { return guiGraphics; }
        public int getMouseX() { return mouseX; }
        public int getMouseY() { return mouseY; }

        /** After the slots and labels, in the screen's own coordinates (translated to its corner). */
        public static final class Foreground extends Render {
            public Foreground(AbstractContainerScreen<?> screen, GuiGraphics guiGraphics, int mouseX, int mouseY) {
                super(screen, guiGraphics, mouseX, mouseY);
            }
        }
    }
}
