package edu.towson.cosc442.halls7800.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.FloorParser;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.List;
import org.junit.jupiter.api.Test;

class FloorValidatorTest {

    private final FloorValidator validator = new FloorValidator();

    @Test
    void anOrdinaryClosedDoorDoesNotBlockProgression() {
        assertTrue(validator.validate(twoRooms(), true).isEmpty());
    }

    @Test
    void aLockedDoorBlocksBothTheExitAndItsRoom() {
        Floor floor = twoRooms();
        floor.setTerrain(new Position(4, 1), Terrain.DOOR_LOCKED);

        assertEquals(List.of("exit is unreachable from entrance", "room 1 is unreachable"),
                validator.validate(floor, true));
    }

    @Test
    void aMissingExitIsAllowedOnlyWhenOneIsNotRequired() {
        Floor floor = twoRooms();
        floor.setTerrain(floor.exit(), Terrain.FLOOR);
        floor.setExit(null);

        assertEquals(List.of("exit is not a down staircase"), validator.validate(floor, true));
        assertTrue(validator.validate(floor, false).isEmpty());
    }

    @Test
    void rejectsAnEntranceWithoutStairs() {
        Floor floor = twoRooms();
        floor.setTerrain(floor.entrance(), Terrain.FLOOR);

        assertEquals(List.of("entrance is not an up staircase"), validator.validate(floor, true));
    }

    @Test
    void rejectsAnOrdinaryClosedDoorOnTheBorder() {
        Floor floor = twoRooms();
        Position edge = new Position(0, 1);
        floor.setTerrain(edge, Terrain.DOOR_CLOSED);

        assertEquals(List.of("open tile on border at " + edge), validator.validate(floor, true));
    }

    @Test
    void rejectsSparseLayoutsEvenWhenTheStairsConnect() {
        Floor floor = FloorParser.parse("Sparse floor", 1,
                "########",
                "#<>#####",
                "########",
                "########");

        assertEquals(List.of("only 2 open tiles"), validator.validate(floor, true));
    }

    private static Floor twoRooms() {
        Floor floor = FloorParser.parse("Two rooms", 1,
                "########",
                "#<..+>.#",
                "#...#..#",
                "########");
        floor.addRoom(new Room(0, 1, 1, 3, 2));
        floor.addRoom(new Room(1, 5, 1, 2, 2));
        return floor;
    }
}
