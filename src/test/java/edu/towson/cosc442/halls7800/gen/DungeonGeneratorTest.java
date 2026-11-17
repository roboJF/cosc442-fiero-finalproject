package edu.towson.cosc442.halls7800.gen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.towson.cosc442.halls7800.engine.AsciiRenderer;
import edu.towson.cosc442.halls7800.world.Floor;
import org.junit.jupiter.api.Test;

class DungeonGeneratorTest {

    @Test
    void sameSeedProducesSameFloor() {
        Floor first = new DungeonGenerator().generate(3, 12345L, true);
        Floor second = new DungeonGenerator().generate(3, 12345L, true);

        assertEquals(AsciiRenderer.render(first, null, false), AsciiRenderer.render(second, null, false));
    }
}
