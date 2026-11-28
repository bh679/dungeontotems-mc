package games.brennan.dungeontotems.compat.jade;

import games.brennan.dungeontotems.DungeonTotems;
import games.brennan.dungeontotems.client.ZombieReading;
import games.brennan.dungeontotems.curse.ZombieGibberish;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import net.minecraft.client.gui.layouts.LayoutElement;
import snownee.jade.api.ui.BoxElement;
import snownee.jade.api.ui.JadeUI;
import snownee.jade.api.ui.TextElement;
import snownee.jade.impl.Tooltip;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Zombies can't read Jade either: while you are Zombified, every line of Jade's "what am I looking at"
 * panel reads as {@link ZombieGibberish}, nested boxes (a chest's contents) included. Icons, item
 * pictures and health bars stay. The same block or mob always reads the same.
 *
 * <p>Optional: Jade finds this class by its {@link WailaPlugin} annotation, so nothing loads it, or any
 * Jade class, when Jade is not installed.</p>
 */
@WailaPlugin(DungeonTotems.MOD_ID)
public final class ZombieJadePlugin implements IWailaPlugin {

    /** Last of all, after every other plugin has added its lines. */
    private static final int AFTER_EVERYONE = Integer.MAX_VALUE;

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addTooltipCollectedCallback(AFTER_EVERYONE, ZombieJadePlugin::mumble);
    }

    private static void mumble(BoxElement box, Accessor<?> accessor) {
        if (!ZombieReading.cantRead(accessor.getPlayer())) return;
        mumble(box.getTooltip(), seedOf(accessor), new int[] {0});
    }

    /** Swaps every text element for a mumble; {@code line} counts lines across nested boxes. */
    private static void mumble(Tooltip tooltip, int seed, int[] line) {
        boolean changed = false;
        for (Tooltip.Line row : tooltip.lines) {
            List<LayoutElement> elements = row.elements();
            boolean hadText = false;
            for (int e = 0; e < elements.size(); e++) {
                LayoutElement element = elements.get(e);
                if (element instanceof BoxElement nested) {
                    mumble(nested.getTooltip(), seed, line);
                } else if (element instanceof TextElement text) {
                    Component mumble = ZombieGibberish.line(seed, line[0]).withStyle(styleOf(text));
                    elements.set(e, JadeUI.text(mumble));
                    hadText = true;
                }
            }
            if (hadText) {
                line[0]++;
                changed = true;
            }
        }
        // Jade caches the layout; re-measure it for the new words, or they run past the box.
        if (changed) tooltip.isDirty = true;
    }

    /** Jade 26.x keeps a text element's component in a protected field; read once, by reflection. */
    private static final Field TEXT = textField();

    private static Field textField() {
        try {
            Field field = Class.forName("snownee.jade.impl.ui.TextElementImpl").getDeclaredField("text");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private static Style styleOf(TextElement element) {
        if (TEXT == null || !TEXT.getDeclaringClass().isInstance(element)) return Style.EMPTY;
        try {
            return TEXT.get(element) instanceof Component component ? component.getStyle() : Style.EMPTY;
        } catch (IllegalAccessException e) {
            return Style.EMPTY;
        }
    }

    /** A block reads by its kind; a mob by itself, so two zombies mumble differently. */
    private static int seedOf(Accessor<?> accessor) {
        if (accessor instanceof BlockAccessor block) return BuiltInRegistries.BLOCK.getKey(block.getBlock()).hashCode();
        if (accessor instanceof EntityAccessor entity) return entity.getEntity().getUUID().hashCode();
        return 0;
    }
}
