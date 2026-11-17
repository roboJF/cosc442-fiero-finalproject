package edu.towson.cosc442.halls7800.world;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PathfinderTest {

    @Test
    void findsShortestPathAroundAWall() {
        Floor floor = FloorParser.parse("test", 1,
                "#######",
                "#@.#..#",
                "#..#..#",
                "#.....#",
                "#######");

        List<Position> path = Pathfinder.findPath(floor, new Position(1, 1), new Position(5, 1));

        assertEquals(new Position(5, 1), path.get(path.size() - 1));
        // 6, not 5: diagonal steps may not cut the corner at the bottom of the wall.
        assertEquals(6, path.size());
    }
}
