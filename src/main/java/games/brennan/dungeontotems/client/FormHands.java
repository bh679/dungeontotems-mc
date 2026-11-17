package games.brennan.dungeontotems.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

/**
 * Which first-person hands a morphed player sees, for {@code ItemInHandRendererMixin}: the same hands
 * vanilla would show a player (one hand while drawing a bow or crossbow, the main hand with a charged
 * crossbow), less an empty hand with no arm to show.
 */
public final class FormHands {

    private static final List<InteractionHand> BOTH = List.of(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND);
    private static final List<InteractionHand> MAIN = List.of(InteractionHand.MAIN_HAND);
    private static final List<InteractionHand> OFF = List.of(InteractionHand.OFF_HAND);

    private static int itemsDrawn;

    private FormHands() {}

    /** Vanilla's {@code ItemInHandRenderer#evaluateWhichHandsToRender}, whose result type is private. */
    public static List<InteractionHand> handsToDraw(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        boolean bow = main.is(Items.BOW) || off.is(Items.BOW);
        boolean crossbow = main.is(Items.CROSSBOW) || off.is(Items.CROSSBOW);
        if (!bow && !crossbow) return BOTH;
        if (player.isUsingItem()) {
            ItemStack using = player.getUseItem();
            InteractionHand hand = player.getUsedItemHand();
            if (using.is(Items.BOW) || using.is(Items.CROSSBOW)) return hand == InteractionHand.MAIN_HAND ? MAIN : OFF;
            return hand == InteractionHand.MAIN_HAND && isChargedCrossbow(off) ? MAIN : BOTH;
        }
        return isChargedCrossbow(main) ? MAIN : BOTH;
    }

    /** An item is always drawn; an empty hand only as the main hand of a form with arms (vanilla's empty arm). */
    public static boolean shouldDraw(ItemStack stack, boolean mainHand, boolean armed) {
        return !stack.isEmpty() || (mainHand && armed);
    }

    /** Counts a held item drawn in first person ({@link ZombieSelfTest} expects some). */
    public static void itemDrawn() {
        itemsDrawn++;
    }

    static int itemsDrawn() {
        return itemsDrawn;
    }

    private static boolean isChargedCrossbow(ItemStack stack) {
        return stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack);
    }
}
