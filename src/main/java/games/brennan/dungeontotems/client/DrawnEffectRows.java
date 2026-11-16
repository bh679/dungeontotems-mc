package games.brennan.dungeontotems.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Where the inventory screen actually drew its effect list this frame: one row per effect, in the order the
 * screen drew them (its sorted, filtered effect list). Recorded by {@code EffectRenderingInventoryScreenMixin}
 * as the backgrounds are drawn, so it follows any mod that moves the list (Edible Backpacks lifts it above or
 * beside its right panel) instead of assuming vanilla's spot. Cleared at the start of every screen frame.
 */
public final class DrawnEffectRows {

    /** Height of one effect background. */
    static final int ROW_HEIGHT = 32;

    /** One drawn effect background. */
    public record Row(int x, int y, int width) {}

    private static final List<Row> ROWS = new ArrayList<>();
    private static int x;
    private static int width;

    private DrawnEffectRows() {}

    /** A new frame: forget last frame's rows. */
    static void clear() {
        ROWS.clear();
    }

    /** The screen is about to draw its effect backgrounds at {@code x}, wide (120) or compact (32). */
    public static void begin(int left, boolean wide) {
        ROWS.clear();
        x = left;
        width = wide ? 120 : 32;
    }

    /** The screen drew the next effect background at {@code y}. */
    public static void add(int y) {
        ROWS.add(new Row(x, y, width));
    }

    /** This frame's rows (empty when no effect list was drawn). */
    static List<Row> rows() {
        return List.copyOf(ROWS);
    }

    /** Index of the row under the mouse, or -1. Later rows win, as they are drawn on top when squeezed. */
    static int hovered(List<Row> rows, int mouseX, int mouseY) {
        int hit = -1;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (mouseX >= row.x() && mouseX < row.x() + row.width()
                && mouseY >= row.y() && mouseY < row.y() + ROW_HEIGHT) {
                hit = i;
            }
        }
        return hit;
    }
}
