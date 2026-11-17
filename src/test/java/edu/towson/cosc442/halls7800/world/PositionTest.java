package edu.towson.cosc442.halls7800.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PositionTest {

    @Test
    void stepEastIncreasesColumn() {
        assertEquals(new Position(4, 2), new Position(3, 2).step(Direction.EAST));
    }

    @Test
    void stepNorthDecreasesRow() {
        assertEquals(new Position(3, 1), new Position(3, 2).step(Direction.NORTH));
    }
}
