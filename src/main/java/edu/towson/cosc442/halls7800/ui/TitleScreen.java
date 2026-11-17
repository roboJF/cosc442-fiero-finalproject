package edu.towson.cosc442.halls7800.ui;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.persist.HighScoreTable;
import edu.towson.cosc442.halls7800.persist.LifetimeStats;
import edu.towson.cosc442.halls7800.progression.Achievement;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * The opening screen: the story of the Halls, the adventurer's name, difficulty, and the main
 * menu (new game, Orientation tutorial, continue, high scores, quit).
 */
public class TitleScreen implements Screen {

    /** What the player chose on the title screen. */
    public interface Actions {
        /** Starts a new run. @param name adventurer name @param difficulty chosen difficulty */
        void newGame(String name, Difficulty difficulty);

        /** Starts the Orientation tutorial. @param name adventurer name */
        void tutorial(String name);

        /** Continues the saved run. */
        void continueGame();

        /** Exits the application. */
        void quit();
    }

    private static final String[] STORY = {
        "It is the year 2026.",
        "Long ago, before the YR building rose at 7800 York Road, a great tower of learning stood on the very "
                + "same ground. Its scholars, the first TU Tigers, robed in black and gold, built marvels of logic "
                + "and light, and kept them sound with an ancient discipline the old course catalogs call COSC 442: "
                + "Software Quality Assurance and Testing. In the tower's deepest vault they kept the Diploma of "
                + "Ascendance, said to grant true mastery to whoever earns it.",
        "Then the tower sank into the earth beneath Towson University, and its testers were lost with it. With no "
                + "one left to test them, the Halls below filled with bugs, broken machines and stranger things, "
                + "and the Hollow Registrar seized the Diploma. No Tiger has commenced since.",
        "Today, a forgotten stairwell in YR 7800 leads down to the Atrium at the top of the Halls, where a few "
                + "faithful residents keep watch beside an old tiger statue. They are waiting for a Tiger who knows "
                + "how to find bugs, and who is brave enough to go down.",
        "That Tiger is you.",
    };

    /** Width of one monospaced character relative to the font size. */
    private static final double CHAR_WIDTH = 0.6;
    /** Line height relative to the font size. */
    private static final double LINE_HEIGHT = 1.35;

    private static final String[] MENU = {"New Game", "Orientation (tutorial)", "Continue", "High Scores",
        "Achievements", "Statistics", "Quit"};
    private static final int MAX_NAME = 16;

    private final Actions actions;
    private final Canvas canvas = new Canvas();
    private final Pane root = new Pane(canvas);
    private final boolean hasSave;
    private final HighScoreTable scores;
    private final int highlightRank;
    private final Set<Achievement> achievements;
    private final LifetimeStats lifetime;
    private boolean showAchievements;
    private boolean showStats;
    private double scale = 1.0;
    private String name;
    private Difficulty difficulty;
    private int selected;
    private boolean showScores;
    private String notice;

    /**
     * @param actions what to do when the player chooses
     * @param name initial adventurer name
     * @param difficulty initial difficulty
     * @param hasSave whether "Continue" is available
     * @param scores the high-score table to show
     * @param highlightRank a rank to highlight (1-based), or 0
     * @param notice a message to show (e.g. "Game saved."), or {@code null}
     */
    public TitleScreen(Actions actions, String name, Difficulty difficulty, boolean hasSave,
                       HighScoreTable scores, Set<Achievement> achievements, LifetimeStats lifetime,
                       int highlightRank, String notice) {
        this.achievements = achievements;
        this.lifetime = lifetime;
        this.actions = actions;
        this.name = name;
        this.difficulty = difficulty;
        this.hasSave = hasSave;
        this.scores = scores;
        this.highlightRank = highlightRank;
        this.notice = notice;
        this.showScores = highlightRank > 0;
        this.selected = hasSave ? 2 : 0;
        root.setStyle("-fx-background-color: #0b0b10;");
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        canvas.widthProperty().addListener((o, a, b) -> draw());
        canvas.heightProperty().addListener((o, a, b) -> draw());
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void keyPressed(KeyCode code) {
        if (showScores || showAchievements || showStats) {
            if (code == KeyCode.ESCAPE || code == KeyCode.ENTER || code == KeyCode.SPACE) {
                showScores = false;
                showAchievements = false;
                showStats = false;
            }
            draw();
            return;
        }
        switch (code) {
            case UP -> selected = (selected + MENU.length - 1) % MENU.length;
            case DOWN -> selected = (selected + 1) % MENU.length;
            case LEFT -> difficulty = difficulty.easier();
            case RIGHT -> difficulty = difficulty.harder();
            case BACK_SPACE -> {
                if (!name.isEmpty()) {
                    name = name.substring(0, name.length() - 1);
                }
            }
            case ENTER -> choose();
            case ESCAPE -> actions.quit();
            default -> {
                // letters are handled as typed text
            }
        }
        draw();
    }

    @Override
    public void keyTyped(String character) {
        if (showScores || showAchievements || showStats || character.isEmpty()) {
            return;
        }
        char c = character.charAt(0);
        if ((Character.isLetterOrDigit(c) || c == ' ' || c == '-' || c == '\'') && name.length() < MAX_NAME) {
            name += c;
            draw();
        }
    }

    private void choose() {
        String chosenName = name.isBlank() ? "Adventurer" : name.trim();
        switch (selected) {
            case 0 -> actions.newGame(chosenName, difficulty);
            case 1 -> actions.tutorial(chosenName);
            case 2 -> {
                if (hasSave) {
                    actions.continueGame();
                } else {
                    notice = "There is no saved game yet.";
                }
            }
            case 3 -> showScores = true;
            case 4 -> showAchievements = true;
            case 5 -> showStats = true;
            default -> actions.quit();
        }
    }

    /** Redraws the screen. */
    public void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        g.setFill(Color.web("#0b0b10"));
        g.fillRect(0, 0, w, h);
        drawFrame(g, w, h);
        double base = Math.max(1.0, Math.min(1.7, Math.min(w / 1024, h / 640)));
        scale = base;
        while (scale > 0.7 && mainLayoutHeight(w) > h - 30) {
            scale -= 0.05;
        }

        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(Color.web("#f2c14e"));
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 44 * scale));
        g.fillText("THE HALLS OF 7800", w / 2, 66 * scale);
        g.setFill(Color.web("#b8b4c8"));
        g.setFont(Font.font("Monospaced", 14 * scale));
        g.fillText("a TU retro dungeon adventure  -  est. 2026  -  by Claude & Josh Dehlinger", w / 2, 90 * scale);
        g.setTextAlign(TextAlignment.LEFT);

        if (showScores) {
            drawScores(g, w);
            return;
        }
        if (showAchievements) {
            drawAchievements(g, w);
            return;
        }
        if (showStats) {
            drawStats(g, w);
            return;
        }
        double storySize = 15 * scale;
        double column = storyColumn(w);
        double x = (w - column) / 2;
        double y = 118 * scale;
        g.setFont(Font.font("Monospaced", storySize));
        g.setFill(Color.web("#e4e2ee"));
        for (String line : storyLines(column)) {
            g.fillText(line, x, y);
            y += storySize * LINE_HEIGHT;
        }

        y += 16 * scale;
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 16 * scale));
        g.setFill(Color.web("#f4f4f8"));
        g.fillText("Name:        " + name + "_", x, y);
        g.fillText("Difficulty:  < " + difficulty.title() + " >", x, y + 23 * scale);
        g.setFont(Font.font("Monospaced", 13 * scale));
        g.setFill(Color.web("#a8a4b8"));
        g.fillText("             " + difficulty.description(), x, y + 42 * scale);

        double menuTop = y + 74 * scale;
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 17 * scale));
        for (int i = 0; i < MENU.length; i++) {
            boolean disabled = i == 2 && !hasSave;
            boolean active = i == selected;
            g.setFill(disabled ? Color.web("#4a4858") : active ? Color.web("#f2c14e") : Color.web("#e4e2ee"));
            g.fillText((active ? "> " : "  ") + MENU[i], x, menuTop + i * 24 * scale);
        }
        double after = menuTop + MENU.length * 24 * scale;
        g.setFont(Font.font("Monospaced", 13 * scale));
        g.setFill(Color.web("#a8a4b8"));
        g.fillText("Up/Down choose   Enter select   Left/Right difficulty   type your name   F2 music   F3 sound",
                x, after);
        if (notice != null) {
            g.setFill(Color.web("#7fd0ff"));
            g.fillText(notice, x, after + 20 * scale);
        }
    }

    /** Height the main menu layout needs at the current scale. */
    private double mainLayoutHeight(double w) {
        int storyLines = storyLines(storyColumn(w)).size();
        return 118 * scale + storyLines * 15 * scale * LINE_HEIGHT + 16 * scale + 74 * scale
                + MENU.length * 24 * scale + 40 * scale;
    }

    private double storyColumn(double w) {
        return Math.min(w - 80, 940 * scale);
    }

    private List<String> storyLines(double column) {
        int maxChars = Math.max(20, (int) (column / (15 * scale * CHAR_WIDTH)));
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < STORY.length; i++) {
            if (i > 0) {
                lines.add("");
            }
            lines.addAll(wrap(STORY[i], maxChars));
        }
        return lines;
    }

    /**
     * Splits text into lines of at most {@code maxChars} characters, breaking between words.
     *
     * @param text the text
     * @param maxChars the line length limit
     * @return the lines
     */
    static List<String> wrap(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (line.length() > 0 && line.length() + 1 + word.length() > maxChars) {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (line.length() > 0) {
                line.append(' ');
            }
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private void drawFrame(GraphicsContext g, double w, double h) {
        g.setStroke(Color.web("#5b4f72"));
        g.setLineWidth(3);
        g.strokeRect(10, 10, w - 20, h - 20);
        g.setFill(Color.web("#f2c14e"));
        for (double px = 24; px < w - 24; px += 48) {
            g.fillRect(px, 14, 24, 4);
            g.fillRect(px + 24, h - 18, 24, 4);
        }
    }

    private void drawStats(GraphicsContext g, double w) {
        double x = Math.max(30, w / 2 - 380 * scale);
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 17 * scale));
        g.setFill(Color.web("#f2c14e"));
        g.fillText("LIFETIME STATISTICS", x, 130 * scale);
        g.setFont(Font.font("Monospaced", 14 * scale));
        g.setFill(Color.web("#e4e2ee"));
        String cause = lifetime.mostCommonCause();
        String[] lines = {
            "Runs finished        " + lifetime.runs() + "   (won " + lifetime.wins() + ", fell " + lifetime.deaths()
                    + ", " + lifetime.winPercent() + "% graduated)",
            "Best score           " + lifetime.bestScore(),
            "Deepest depth        " + lifetime.deepestDepth(),
            "Turns played         " + lifetime.totalTurns() + "   (about " + lifetime.averageTurns() + " per run)",
            "Monsters defeated    " + lifetime.totalKills() + "   (bosses " + lifetime.bossesDefeated() + ")",
            "Gold collected       " + lifetime.goldCollected(),
            "Potions / scrolls    " + lifetime.potionsDrunk() + " / " + lifetime.scrollsRead(),
            "Most common end      " + (cause == null ? "-" : cause),
            "Runs by difficulty   " + lifetime.runsByDifficulty(),
            "Achievements         " + achievements.size() + " / " + Achievement.values().length,
        };
        for (int i = 0; i < lines.length; i++) {
            g.fillText(lines[i], x, 164 * scale + i * 24 * scale);
        }
        g.setFill(Color.web("#a8a4b8"));
        g.fillText("Press Enter or Esc to return", x, 164 * scale + lines.length * 24 * scale + 20 * scale);
    }

    private void drawAchievements(GraphicsContext g, double w) {
        Achievement[] all = Achievement.values();
        double size = Math.min(13 * scale, (canvas.getHeight() - 200 * scale) / (all.length * 1.45));
        double x = Math.max(30, w / 2 - size * 0.6 * 48);
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 17 * scale));
        g.setFill(Color.web("#f2c14e"));
        g.fillText("ACHIEVEMENTS  " + achievements.size() + " / " + all.length, x, 130 * scale);
        g.setFont(Font.font("Monospaced", size));
        for (int i = 0; i < all.length; i++) {
            boolean earned = achievements.contains(all[i]);
            g.setFill(earned ? Color.web("#f2c14e") : Color.web("#8a8898"));
            g.fillText((earned ? "[*] " : "[ ] ") + String.format("%-22s %s", all[i].title(), all[i].description()),
                    x, 158 * scale + i * size * 1.45);
        }
        g.setFill(Color.web("#a8a4b8"));
        g.fillText("Press Enter or Esc to return", x, 158 * scale + all.length * size * 1.45 + 16 * scale);
    }

    private void drawScores(GraphicsContext g, double w) {
        double x = Math.max(30, w / 2 - 380 * scale);
        g.setFont(Font.font("Monospaced", FontWeight.BOLD, 17 * scale));
        g.setFill(Color.web("#f2c14e"));
        g.fillText("HIGH SCORES", x, 140 * scale);
        g.setFont(Font.font("Monospaced", 14 * scale));
        List<HighScoreTable.Entry> entries = scores.entries();
        if (entries.isEmpty()) {
            g.setFill(Color.web("#e4e2ee"));
            g.fillText("No runs recorded yet. Be the first!", x, 174 * scale);
        }
        for (int i = 0; i < entries.size(); i++) {
            HighScoreTable.Entry e = entries.get(i);
            g.setFill(i + 1 == highlightRank ? Color.web("#f2c14e") : Color.web("#e4e2ee"));
            String outcome = e.outcome() == GamePhase.VICTORY ? "GRADUATED" : "fell on depth " + e.depth();
            g.fillText(String.format("%2d. %7d  %-16s %-14s %-13s %s", i + 1, e.score(), e.name(), outcome,
                    e.difficulty().title(), e.date()), x, 174 * scale + i * 22 * scale);
        }
        g.setFill(Color.web("#a8a4b8"));
        g.fillText("Press Enter or Esc to return", x, 174 * scale + Math.max(1, entries.size()) * 22 * scale + 24 * scale);
    }
}
