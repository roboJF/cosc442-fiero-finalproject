package edu.towson.cosc442.halls7800.world;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class FieldOfViewTest {

    @Test
    void wallHidesTilesBehindIt() {
        Floor floor = FloorParser.parse("test", 1,
                "#########",
                "#@..#...#",
                "#########");

        Set<Position> visible = FieldOfView.compute(floor, new Position(1, 1), 8);

        assertTrue(visible.contains(new Position(3, 1)));
        assertTrue(visible.contains(new Position(4, 1)), "the wall itself is visible");
        assertFalse(visible.contains(new Position(5, 1)));
    }
}
