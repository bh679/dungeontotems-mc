package games.brennan.dungeontotems.platform.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

/** Client screen events. */
public abstract class ScreenEvent extends Event {
    private final Screen screen;

    protected ScreenEvent(Screen screen) {
        this.screen = screen;
    }

    public Screen getScreen() { return screen; }

    /** Around a screen's whole render. */
    public abstract static class Render extends ScreenEvent {
        private final GuiGraphics guiGraphics;
        private final int mouseX;
        private final int mouseY;
        private final float partialTick;

        protected Render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super(screen);
            this.guiGraphics = guiGraphics;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.partialTick = partialTick;
        }

        public GuiGraphics getGuiGraphics() { return guiGraphics; }
        public int getMouseX() { return mouseX; }
        public int getMouseY() { return mouseY; }
        public float getPartialTick() { return partialTick; }

        public static final class Pre extends Render {
            public Pre(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super(screen, guiGraphics, mouseX, mouseY, partialTick);
            }
        }

        public static final class Post extends Render {
            public Post(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                super(screen, guiGraphics, mouseX, mouseY, partialTick);
            }
        }
    }

    /** A typed character, before the screen gets it. Cancel to swallow it. */
    public abstract static class CharacterTyped extends ScreenEvent {
        private final char codePoint;
        private final int modifiers;

        protected CharacterTyped(Screen screen, char codePoint, int modifiers) {
            super(screen);
            this.codePoint = codePoint;
            this.modifiers = modifiers;
        }

        public char getCodePoint() { return codePoint; }
        public int getModifiers() { return modifiers; }

        public static final class Pre extends CharacterTyped {
            public Pre(Screen screen, char codePoint, int modifiers) {
                super(screen, codePoint, modifiers);
            }
        }
    }
}
