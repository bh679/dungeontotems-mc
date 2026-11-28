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
import snownee.jade.api.ui.IBoxElement;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ITextElement;
import snownee.jade.impl.Tooltip;
import snownee.jade.impl.ui.TextElement;

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

    private static void mumble(IBoxElement box, Accessor<?> accessor) {
        if (!ZombieReading.cantRead(accessor.getPlayer())) return;
        mumble(box.getTooltip(), seedOf(accessor), new int[] {0});
    }

    /** Swaps every text element for a mumble; {@code line} counts lines across nested boxes. */
    private static void mumble(ITooltip tooltip, int seed, int[] line) {
        for (int i = 0; i < tooltip.size(); i++) {
            boolean hadText = false;
            for (IElement.Align align : IElement.Align.VALUES) {
                List<IElement> elements = tooltip.get(i, align);
                for (int e = 0; e < elements.size(); e++) {
                    IElement element = elements.get(e);
                    if (element instanceof IBoxElement nested) {
                        mumble(nested.getTooltip(), seed, line);
                    } else if (element instanceof ITextElement) {
                        Component text = ZombieGibberish.line(seed, line[0]).withStyle(styleOf(element));
                        elements.set(e, IElementHelper.get().text(text));
                        hadText = true;
                    }
                }
            }
            if (hadText) {
                line[0]++;
                // Jade caches a line's width; re-measure it for the new words, or they run past the box.
                if (tooltip instanceof Tooltip impl) impl.lines.get(i).markDirty();
            }
        }
    }

    private static Style styleOf(IElement element) {
        return element instanceof TextElement text && text.text instanceof Component component ? component.getStyle() : Style.EMPTY;
    }

    /** A block reads by its kind; a mob by itself, so two zombies mumble differently. */
    private static int seedOf(Accessor<?> accessor) {
        if (accessor instanceof BlockAccessor block) return BuiltInRegistries.BLOCK.getKey(block.getBlock()).hashCode();
        if (accessor instanceof EntityAccessor entity) return entity.getEntity().getUUID().hashCode();
        return 0;
    }
}
