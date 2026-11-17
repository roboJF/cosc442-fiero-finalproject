package edu.towson.cosc442.halls7800.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.MoveCommand;
import edu.towson.cosc442.halls7800.engine.SearchCommand;
import edu.towson.cosc442.halls7800.engine.WaitCommand;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.world.Direction;
import org.junit.jupiter.api.Test;

class ReplayRunnerTest {

    @Test
    void savedRecordReplaysToTheSameState() {
        GameSession original = GameFactory.newGame(2024L, "Ada", Difficulty.GRADUATE);
        for (int i = 0; i < 5; i++) {
            original.submit(new MoveCommand(Direction.EAST));
            original.submit(new SearchCommand());
            original.submit(new WaitCommand());
        }

        RunRecord saved = RunRecord.parse(RunRecord.of(original).toText());
        GameSession restored = ReplayRunner.replay(saved);

        assertEquals(original.turn(), restored.turn());
        assertEquals(original.player().position(), restored.player().position());
        assertEquals(original.render(), restored.render());
    }
}
