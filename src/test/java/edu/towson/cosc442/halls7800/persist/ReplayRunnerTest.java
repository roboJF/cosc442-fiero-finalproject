package edu.towson.cosc442.halls7800.persist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.DropCommand;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.MoveCommand;
import edu.towson.cosc442.halls7800.engine.SearchCommand;
import edu.towson.cosc442.halls7800.engine.StairsCommand;
import edu.towson.cosc442.halls7800.engine.WaitCommand;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Position;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ReplayRunnerTest {

    @TempDir
    Path directory;

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

        assertEquals(RunRecord.VERSION, saved.version());
        assertEquals(ActionRules.CURRENT, restored.actionRules());
        assertEquals(original.turn(), restored.turn());
        assertEquals(original.player().position(), restored.player().position());
        assertEquals(original.render(), restored.render());
    }

    @Test
    void continueRestoresTheTurnSpentInASnare() {
        GameSession original = GameFactory.newGame(62L, "Ada", Difficulty.FRESHMAN);
        enterDungeon(original);
        move(original, Direction.NORTH, 11);
        Position snare = original.player().position();
        assertTrue(original.player().effects().has(EffectType.STUNNED));

        int turnBeforeStun = original.turn();
        assertTrue(original.submit(new MoveCommand(Direction.SOUTH)).performed());
        assertEquals(turnBeforeStun + 1, original.turn());
        assertEquals(snare, original.player().position());
        assertFalse(original.player().effects().has(EffectType.STUNNED));

        GameSession restored = saveAndContinue(original);
        assertEquals(original.turn(), restored.turn());
        assertEquals(original.depth(), restored.depth());
        assertEquals(snare, restored.player().position());
        assertEquals(original.player().hp(), restored.player().hp());
        assertFalse(restored.player().effects().has(EffectType.STUNNED));
        assertEquals(original.recording(), restored.recording());
        assertEquals(original.messages().all(), restored.messages().all());
        assertEquals(original.monsters().size(), restored.monsters().size());
        for (int i = 0; i < original.monsters().size(); i++) {
            var expected = original.monsters().get(i);
            var actual = restored.monsters().get(i);
            assertEquals(expected.type(), actual.type());
            assertEquals(expected.position(), actual.position());
            assertEquals(expected.hp(), actual.hp());
            assertEquals(expected.state(), actual.state());
        }
    }

    @Test
    void continuePreservesChangesOnARevisitedFloor() {
        GameSession original = GameFactory.newGame(62L, "Ada", Difficulty.FRESHMAN);
        enterDungeon(original);
        Position entrance = original.player().position();
        String droppedItem = original.describe(original.player().inventory().get(2));
        assertTrue(original.submit(new DropCommand(2)).performed());
        assertTrue(original.submit(StairsCommand.ascend()).performed());
        assertEquals(0, original.depth());

        GameSession restored = saveAndContinue(original);
        assertEquals(original.turn(), restored.turn());
        assertEquals(original.depth(), restored.depth());
        assertEquals(original.player().position(), restored.player().position());
        assertEquals(original.visitedDepths(), restored.visitedDepths());
        assertEquals(original.player().inventory().items().stream().map(original::describe).toList(),
                restored.player().inventory().items().stream().map(restored::describe).toList());

        assertTrue(restored.submit(StairsCommand.descend()).performed());
        assertEquals(1, restored.depth());
        assertEquals(entrance, restored.player().position());
        assertEquals(List.of(droppedItem), restored.itemsAt(entrance).stream().map(restored::describe).toList());
    }

    @Test
    void readsVersionOneRecordsWithRejectedInputs() {
        RunRecord record = RunRecord.parse("""
                HALLS7800-RUN 1
                seed=62
                difficulty=FRESHMAN
                name=Ada
                M:NORTH_EAST
                F
                T:-1
                W
                """);

        GameSession restored = ReplayRunner.replay(record);

        assertEquals(3, restored.turn());
        assertEquals(new Position(27, 13), restored.player().position());
        assertEquals(record.actions(), restored.recording());
        assertEquals(ActionRules.LEGACY, restored.actionRules());

        assertTrue(restored.submit(new WaitCommand()).performed());
        RunRecord savedAgain = RunRecord.parse(RunRecord.of(restored).toText());
        assertEquals(1, savedAgain.version());
        assertEquals(restored.turn(), ReplayRunner.replay(savedAgain).turn());
    }

    @Test
    void continuedVersionTwoRecordKeepsItsVersionAndRules() {
        RunRecord record = RunRecord.parse("""
                HALLS7800-RUN 2
                seed=62
                difficulty=FRESHMAN
                name=Ada
                M:NORTH_EAST
                W
                """);

        GameSession continued = ReplayRunner.replay(record);

        assertEquals(ActionRules.VERSION_2, continued.actionRules());
        assertEquals(new Position(27, 13), continued.player().position());
        assertEquals(2, continued.turn());

        assertTrue(continued.submit(new WaitCommand()).performed());
        RunRecord savedAgain = RunRecord.parse(RunRecord.of(continued).toText());
        assertEquals(2, savedAgain.version());
        assertEquals(List.of("M:NORTH_EAST", "W", "W"), savedAgain.actions());
        assertEquals(continued.turn(), ReplayRunner.replay(savedAgain).turn());
    }

    @ParameterizedTest
    @ValueSource(strings = {"S:TYPO", "S", "SHOP:OPEN", "SHOP", "W:extra", "F:", "G:extra",
            "X", "M:TYPO", "BUY:", "SELL:words", "U:2147483648", "PERK:TYPO", "UNKNOWN"})
    void reportsTheOneBasedIndexOfAnInvalidAction(String token) {
        RunRecord record = new RunRecord(62L, Difficulty.FRESHMAN, "Ada", List.of("W", token));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> ReplayRunner.replay(record));

        assertTrue(error.getMessage().contains("Invalid action 2 ('" + token + "')"));
        assertEquals(1, ReplayRunner.replay(record, 1).turn());
    }

    private GameSession saveAndContinue(GameSession original) {
        SaveStore store = new SaveStore(directory);
        store.saveRun(RunRecord.of(original));
        return ReplayRunner.replay(store.loadRun().orElseThrow());
    }

    private static void enterDungeon(GameSession game) {
        move(game, Direction.NORTH_EAST, 3);
        move(game, Direction.EAST, 1);
        move(game, Direction.NORTH_EAST, 4);
        move(game, Direction.EAST, 5);
        assertTrue(game.submit(StairsCommand.descend()).performed());
        assertEquals(1, game.depth());
    }

    private static void move(GameSession game, Direction direction, int count) {
        for (int i = 0; i < count; i++) {
            assertTrue(game.submit(new MoveCommand(direction)).performed());
        }
    }
}
