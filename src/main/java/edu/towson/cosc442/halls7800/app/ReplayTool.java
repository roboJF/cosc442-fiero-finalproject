package edu.towson.cosc442.halls7800.app;

import edu.towson.cosc442.halls7800.engine.AsciiRenderer;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.MessageLog;
import edu.towson.cosc442.halls7800.engine.RunStats;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.item.Item;
import edu.towson.cosc442.halls7800.persist.ReplayRunner;
import edu.towson.cosc442.halls7800.persist.RunRecord;
import edu.towson.cosc442.halls7800.persist.SaveStore;
import edu.towson.cosc442.halls7800.progression.ScoreCalculator;
import edu.towson.cosc442.halls7800.world.Position;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Replays a recorded run without opening a window and prints what happened. Useful for checking
 * bug reports: attach {@code last-run.txt} (or a saved {@code current-run.txt}) and anyone can
 * reproduce the exact run.
 *
 * <pre>
 * ./mvnw -q exec:java                                   (replays ~/.halls7800/last-run.txt)
 * ./mvnw -q exec:java -Dreplay="path/to/run.txt"
 * ./mvnw -q exec:java -Dreplay="path/to/run.txt --steps=120 --map --log=20"
 * </pre>
 *
 * Options: {@code --steps=N} stops after the first N actions; {@code --map} prints the current
 * floor; {@code --log=N} prints the last N messages (default 10).
 */
public final class ReplayTool {

    private ReplayTool() {
    }

    /**
     * Entry point.
     *
     * @param args an optional record path followed by options
     */
    public static void main(String[] args) {
        System.exit(run(args, System.out));
    }

    /**
     * Runs the tool, writing to the given stream.
     *
     * @param args arguments as for {@link #main}
     * @param out where to print
     * @return a process exit code: 0 on success, 1 on error
     */
    static int run(String[] args, PrintStream out) {
        Path file = null;
        int steps = Integer.MAX_VALUE;
        boolean map = false;
        int logLines = 10;
        for (String arg : args) {
            if (arg.isBlank()) {
                continue;
            }
            if (arg.startsWith("--steps=")) {
                steps = Integer.parseInt(arg.substring("--steps=".length()));
            } else if (arg.equals("--map")) {
                map = true;
            } else if (arg.startsWith("--log=")) {
                logLines = Integer.parseInt(arg.substring("--log=".length()));
            } else {
                file = Path.of(arg);
            }
        }
        if (file == null) {
            file = SaveStore.standard().directory().resolve(SaveStore.LAST_RUN);
        }
        RunRecord record;
        try {
            record = RunRecord.parse(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException | RuntimeException e) {
            out.println("Could not read run record " + file + ": " + e.getMessage());
            return 1;
        }
        GameSession session;
        try {
            session = ReplayRunner.replay(record, steps);
        } catch (IllegalArgumentException e) {
            out.println("Could not replay run record " + file + ": " + e.getMessage());
            return 1;
        }
        printSummary(out, file, record, session, Math.min(steps, record.actions().size()));
        if (map) {
            out.println();
            out.println(renderMap(session));
        }
        out.println();
        out.println("Last messages:");
        List<MessageLog.Entry> recent = session.messages().recent(logLines);
        for (MessageLog.Entry e : recent) {
            out.println("  [turn " + e.turn() + "] " + e.display());
        }
        return 0;
    }

    private static void printSummary(PrintStream out, Path file, RunRecord record, GameSession session, int applied) {
        RunStats stats = session.stats();
        out.println("The Halls of 7800 - replay of " + file);
        out.println("Adventurer: " + record.playerName() + "   Difficulty: " + record.difficulty().title()
                + "   Seed: " + record.seed());
        out.println("Actions replayed: " + applied + " of " + record.actions().size());
        out.println("Outcome: " + session.phase() + "   Score: " + ScoreCalculator.score(session));
        out.println("Floor: " + session.currentFloor().name() + " (depth " + session.depth() + ", deepest "
                + session.deepestReached() + ")   Turn: " + session.turn());
        out.println("Player: level " + session.player().level() + ", HP " + session.player().hp() + "/"
                + session.player().maxHp() + ", gold " + session.player().gold() + ", at " + session.player().position());
        out.println("Kills: " + stats.totalKills() + "   Bosses: " + stats.bossesDefeated() + "   Damage dealt/taken: "
                + stats.damageDealt() + "/" + stats.damageTaken() + "   Traps: " + stats.trapsTriggered());
        if (stats.causeOfDeath() != null) {
            out.println("Cause of death: " + stats.causeOfDeath());
        }
        StringBuilder pack = new StringBuilder("Pack:");
        for (Item item : session.player().inventory().items()) {
            pack.append(' ').append(session.describe(item)).append(';');
        }
        out.println(pack);
    }

    private static String renderMap(GameSession session) {
        Map<Position, Character> overlay = new HashMap<>();
        for (Monster m : session.monsters()) {
            overlay.put(m.position(), m.type().glyph());
        }
        if (session.companion() != null) {
            overlay.put(session.companion().position(), 'd');
        }
        return AsciiRenderer.render(session.currentFloor(), session.player().position(), false, overlay);
    }
}
