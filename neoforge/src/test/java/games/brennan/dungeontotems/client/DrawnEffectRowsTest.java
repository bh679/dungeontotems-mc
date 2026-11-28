package games.brennan.dungeontotems.client;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DrawnEffectRowsTest {

    private static final List<DrawnEffectRows.Row> WIDE_ABOVE_PANEL = List.of(
        new DrawnEffectRows.Row(300, 20, 120), new DrawnEffectRows.Row(300, 53, 120));

    @Test
    void hitsTheRowUnderTheMouse() {
        assertEquals(0, DrawnEffectRows.hovered(WIDE_ABOVE_PANEL, 310, 25));
        assertEquals(1, DrawnEffectRows.hovered(WIDE_ABOVE_PANEL, 419, 84));
    }

    @Test
    void missesOutsideTheDrawnRows() {
        // Below the moved stack, where vanilla's stack would have been (and a backpack panel now sits).
        assertEquals(-1, DrawnEffectRows.hovered(WIDE_ABOVE_PANEL, 310, 120));
        assertEquals(-1, DrawnEffectRows.hovered(WIDE_ABOVE_PANEL, 420, 25));
        assertEquals(-1, DrawnEffectRows.hovered(WIDE_ABOVE_PANEL, 299, 25));
        assertEquals(-1, DrawnEffectRows.hovered(List.of(), 310, 25));
    }

    @Test
    void laterRowWinsWhereSqueezedRowsOverlap() {
        List<DrawnEffectRows.Row> squeezed = List.of(
            new DrawnEffectRows.Row(0, 0, 120), new DrawnEffectRows.Row(0, 22, 120));
        assertEquals(1, DrawnEffectRows.hovered(squeezed, 5, 25));
        assertEquals(0, DrawnEffectRows.hovered(squeezed, 5, 10));
    }
}
