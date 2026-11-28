package games.brennan.dungeontotems.platform.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;

/** Client input. */
public abstract class InputEvent extends Event {

    /**
     * The attack, use or pick key acting ({@code Minecraft#startAttack}, {@code startUseItem}). Cancel to stop
     * it; {@link #setSwingHand} says whether the hand still swings.
     */
    public static final class InteractionKeyMappingTriggered extends InputEvent {
        public static final int ATTACK = 0;
        public static final int USE = 1;
        public static final int PICK = 2;

        private final int button;
        private final KeyMapping keyMapping;
        private final InteractionHand hand;
        private boolean swingHand = true;

        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        public boolean isAttack() { return button == ATTACK; }
        public boolean isUseItem() { return button == USE; }
        public boolean isPickBlock() { return button == PICK; }
        public KeyMapping getKeyMapping() { return keyMapping; }
        public InteractionHand getHand() { return hand; }
        public boolean shouldSwingHand() { return swingHand; }
        public void setSwingHand(boolean swing) { this.swingHand = swing; }
    }
}
