package edu.towson.cosc442.halls7800.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import edu.towson.cosc442.halls7800.persist.ReplayRunner;
import edu.towson.cosc442.halls7800.persist.RunRecord;
import edu.towson.cosc442.halls7800.persist.SaveStore;
import edu.towson.cosc442.halls7800.ui.GameView;
import edu.towson.cosc442.halls7800.world.Position;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.api.parallel.ResourceLock;

@EnabledIfSystemProperty(named = "halls7800.javafxSmoke", matches = "true")
public class HallsAppSmokeTest {

    @Test
    @Timeout(30)
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void newGameCanBeSavedContinuedAndSavedAgain(@TempDir Path saveDirectory) {
        String previousHome = System.setProperty("halls7800.home", saveDirectory.toString());
        try {
            Application.launch(SmokeApplication.class, "--seed=62");
            SmokeApplication.scenario.join();
        } finally {
            if (previousHome == null) {
                System.clearProperty("halls7800.home");
            } else {
                System.setProperty("halls7800.home", previousHome);
            }
        }
    }

    /** Runs the real application and sends the same events as the title and game keyboard controls. */
    public static class SmokeApplication extends HallsApp {

        private static final CompletableFuture<Void> scenario = new CompletableFuture<>();

        @Override
        public void start(Stage stage) {
            super.start(stage);
            Platform.runLater(() -> {
                try {
                    playAndContinue(stage.getScene(), SaveStore.standard());
                    scenario.complete(null);
                } catch (Throwable failure) {
                    scenario.completeExceptionally(failure);
                } finally {
                    Platform.exit();
                }
            });
        }

        private static void playAndContinue(Scene scene, SaveStore store) {
            press(scene, KeyCode.ENTER);
            Parent firstGame = assertInstanceOf(GameView.class, scene.getRoot());
            press(scene, KeyCode.SPACE);
            press(scene, KeyCode.ESCAPE);
            Parent title = scene.getRoot();
            assertNotSame(firstGame, title);
            RunRecord saved = store.loadRun().orElseThrow();
            assertEquals(62L, saved.seed());
            assertEquals(List.of("W"), saved.actions());

            press(scene, KeyCode.ENTER); // Continue is selected when a save exists.
            GameView continued = assertInstanceOf(GameView.class, scene.getRoot());
            assertNotSame(firstGame, continued);
            press(scene, KeyCode.E);
            press(scene, KeyCode.R);
            press(scene, KeyCode.C);
            press(scene, KeyCode.G);
            press(scene, KeyCode.ESCAPE);
            assertNotSame(continued, scene.getRoot());

            RunRecord updated = store.loadRun().orElseThrow();
            assertEquals(new RunRecord(saved.seed(), saved.difficulty(), saved.playerName(),
                    List.of("W", "M:NORTH_EAST", "R:2", "G"), saved.version()), updated);
            var replayed = ReplayRunner.replay(updated);
            assertEquals(4, replayed.turn());
            assertEquals(new Position(27, 13), replayed.player().position());
        }

        private static void press(Scene scene, KeyCode key) {
            Event.fireEvent(scene, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, false, false, false, false));
        }
    }
}
