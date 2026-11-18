package edu.towson.cosc442.halls7800.app;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.gen.GameFactory;
import edu.towson.cosc442.halls7800.persist.HighScoreTable;
import edu.towson.cosc442.halls7800.persist.LifetimeStats;
import edu.towson.cosc442.halls7800.persist.ReplayRunner;
import edu.towson.cosc442.halls7800.persist.RunRecord;
import edu.towson.cosc442.halls7800.persist.SaveStore;
import edu.towson.cosc442.halls7800.progression.Achievement;
import edu.towson.cosc442.halls7800.progression.ScoreCalculator;
import java.util.Set;
import edu.towson.cosc442.halls7800.ui.GameView;
import edu.towson.cosc442.halls7800.ui.Screen;
import edu.towson.cosc442.halls7800.ui.TitleScreen;
import edu.towson.cosc442.halls7800.ui.audio.AudioPlayer;
import edu.towson.cosc442.halls7800.ui.audio.Tune;
import javafx.scene.input.KeyCode;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * The JavaFX application: moves between the title screen and the game, and handles saving,
 * loading and high scores.
 *
 * <p>Accepts {@code --seed=<number or word>} for the first new game. Without a seed (or with
 * {@code --seed=random}) a random seed is chosen; it is shown in the side panel so the run can be
 * reproduced.
 */
public class HallsApp extends Application implements TitleScreen.Actions, GameView.Listener {

    private final SaveStore store;
    private AudioPlayer audio;
    private Scene scene;
    private Screen screen;
    private GameSession current;
    private String seedArgument;
    private String playerName = GameFactory.DEFAULT_PLAYER_NAME;
    private Difficulty difficulty = Difficulty.UNDERGRADUATE;

    /** Creates the application with the standard save location. */
    public HallsApp() {
        this(SaveStore.standard());
    }

    HallsApp(SaveStore store) {
        this.store = store;
    }

    @Override
    public void start(Stage stage) {
        audio = new AudioPlayer();
        this.seedArgument = getParameters().getNamed().get("seed");
        showTitle(0, null);
        scene = new Scene(screen.root(), 1024, 640);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F2) {
                audio.toggleMusic();
            } else if (e.getCode() == KeyCode.F3) {
                audio.toggleSoundEffects();
            }
            screen.keyPressed(e.getCode());
            e.consume();
        });
        scene.addEventHandler(KeyEvent.KEY_TYPED, e -> screen.keyTyped(e.getCharacter()));
        stage.setTitle("The Halls of 7800");
        stage.setScene(scene);
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setOnCloseRequest(event -> closeWindow(event, current));
        stage.show();
    }

    // ----- title screen actions -----

    @Override
    public void newGame(String name, Difficulty chosen) {
        rememberChoices(name, chosen);
        long seed = parseSeed(seedArgument);
        seedArgument = null;
        showGame(GameFactory.newGame(seed, name, chosen));
    }

    @Override
    public void tutorial(String name) {
        rememberChoices(name, difficulty);
        showGame(GameFactory.newTutorial(name));
    }

    @Override
    public void continueGame() {
        try {
            RunRecord record = store.loadRun().orElseThrow();
            showGame(ReplayRunner.replay(record));
        } catch (RuntimeException e) {
            showTitle(0, "Could not load the saved game: " + e.getMessage());
        }
    }

    @Override
    public void quit() {
        Platform.exit();
    }

    @Override
    public void stop() {
        audio.close();
    }

    // ----- game screen events -----

    @Override
    public void quitToTitle(GameSession session) {
        if (!saveBeforeLeaving(session)) {
            return;
        }
        current = null;
        showTitle(0, session.isTutorial() ? null : "Game saved. Choose Continue to pick up where you left off.");
    }

    @Override
    public void runFinished(GameSession session) {
        current = null;
        recordAchievements(session);
        if (session.isTutorial()) {
            showTitle(0, "Orientation complete. Choose New Game when you are ready.");
            return;
        }
        int rank = 0;
        String notice;
        try {
            store.saveLastRun(RunRecord.of(session));
            store.deleteRun();
            HighScoreTable table = store.loadScores();
            rank = table.add(new HighScoreTable.Entry(ScoreCalculator.score(session), session.player().name(),
                    session.deepestReached(), session.phase(), session.difficulty(), session.turn(), session.seed(),
                    LocalDate.now().toString()));
            store.saveScores(table);
            LifetimeStats lifetime = store.loadLifetimeStats();
            lifetime.record(session, ScoreCalculator.score(session));
            store.saveLifetimeStats(lifetime);
            notice = rank > 0 ? "New high score! You placed #" + rank + "." : "Your run has been recorded.";
        } catch (UncheckedIOException e) {
            notice = "Could not record the run: " + e.getMessage();
        }
        showTitle(rank, notice);
    }

    // ----- screens -----

    private void showTitle(int highlightRank, String notice) {
        HighScoreTable scores;
        boolean hasSave;
        Set<Achievement> earned;
        LifetimeStats lifetime;
        try {
            scores = store.loadScores();
            hasSave = store.hasSavedRun();
            earned = store.loadAchievements();
            lifetime = store.loadLifetimeStats();
        } catch (UncheckedIOException e) {
            scores = new HighScoreTable();
            hasSave = false;
            earned = Set.of();
            lifetime = new LifetimeStats();
        }
        TitleScreen title = new TitleScreen(this, playerName, difficulty, hasSave, scores, earned, lifetime,
                highlightRank, notice);
        setScreen(title);
        audio.playMusic(Tune.TITLE);
        title.draw();
    }

    private void showGame(GameSession session) {
        current = session;
        GameView view = new GameView(session, this, audio);
        setScreen(view);
        view.refresh();
    }

    private void setScreen(Screen next) {
        screen = next;
        if (scene != null) {
            scene.setRoot(next.root());
        }
    }

    private void rememberChoices(String name, Difficulty chosen) {
        playerName = name;
        difficulty = chosen;
    }

    private void recordAchievements(GameSession session) {
        try {
            store.recordAchievements(session.achievements().earned());
        } catch (UncheckedIOException e) {
            // achievements are a bonus; never block leaving the game over them
        }
    }

    void closeWindow(WindowEvent event, GameSession session) {
        if (session != null && !saveBeforeLeaving(session)) {
            event.consume();
        }
    }

    private boolean saveBeforeLeaving(GameSession session) {
        if (!session.isTutorial() && session.phase() == GamePhase.PLAYING) {
            try {
                store.saveRun(RunRecord.of(session));
            } catch (UncheckedIOException e) {
                session.message("Could not save the game. Check the save folder and try again.");
                if (screen instanceof GameView view) {
                    view.refresh();
                }
                return false;
            }
        }
        recordAchievements(session);
        return true;
    }

    /**
     * Converts a seed argument to a number. Numbers are used as-is; other words are hashed so
     * that a memorable word always gives the same run.
     *
     * @param text the argument value, possibly {@code null}
     * @return the seed
     */
    static long parseSeed(String text) {
        if (text == null || text.isBlank() || text.equalsIgnoreCase("random")) {
            return ThreadLocalRandom.current().nextLong(1, 1_000_000_000L);
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return text.trim().toLowerCase().hashCode();
        }
    }
}
