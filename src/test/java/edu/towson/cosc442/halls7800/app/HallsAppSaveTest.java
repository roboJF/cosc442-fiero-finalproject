package edu.towson.cosc442.halls7800.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.WaitCommand;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.persist.RunRecord;
import edu.towson.cosc442.halls7800.persist.SaveStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HallsAppSaveTest {

    @TempDir
    Path directory;

    @Test
    void failedQuitToTitleLeavesTheRunPlayable() throws IOException {
        Path blockedLocation = Files.createFile(directory.resolve("save-location"));
        HallsApp app = new HallsApp(new SaveStore(blockedLocation));
        GameSession game = GameFactory.newGame(62L);

        app.quitToTitle(game);

        assertTrue(game.messages().lastText().startsWith("Could not save the game."));
        assertTrue(game.submit(new WaitCommand()).performed());
    }

    @Test
    void failedWindowCloseIsConsumedAndCanBeRetried() throws IOException {
        Path blockedLocation = Files.createFile(directory.resolve("save-location"));
        SaveStore store = new SaveStore(blockedLocation);
        HallsApp app = new HallsApp(store);
        GameSession game = GameFactory.newGame(62L);
        WindowEvent close = new WindowEvent(null, WindowEvent.WINDOW_CLOSE_REQUEST);

        app.closeWindow(close, game);

        assertTrue(close.isConsumed());
        assertTrue(game.messages().lastText().startsWith("Could not save the game."));
        Files.delete(blockedLocation);
        assertTrue(game.submit(new WaitCommand()).performed());

        WindowEvent retry = new WindowEvent(null, WindowEvent.WINDOW_CLOSE_REQUEST);
        app.closeWindow(retry, game);

        assertFalse(retry.isConsumed());
        assertEquals(RunRecord.of(game), store.loadRun().orElseThrow());
    }
}
