package edu.towson.cosc442.halls7800.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.gen.FixedFloorSource;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    private GameSession game;

    @BeforeEach
    void setUp() {
        FixedFloorSource source = new FixedFloorSource()
                .add("Hub",
                        "#######",
                        "#@.+.>#",
                        "#######")
                .add("Below",
                        "#####",
                        "#<..#",
                        "#####");
        game = new GameSession(1L, source, "Tester");
    }

    @Test
    void movingIntoOpenFloorTakesATurn() {
        CommandResult result = game.submit(new MoveCommand(Direction.EAST));

        assertTrue(result.performed());
        assertEquals(new Position(2, 1), game.player().position());
        assertEquals(1, game.turn());
    }

    @Test
    void walkingIntoAClosedDoorOpensIt() {
        game.submit(new MoveCommand(Direction.EAST));
        game.submit(new MoveCommand(Direction.EAST));

        assertEquals(Terrain.DOOR_OPEN, game.floor().terrainAt(new Position(3, 1)));
        assertEquals(new Position(2, 1), game.player().position());
    }
}
