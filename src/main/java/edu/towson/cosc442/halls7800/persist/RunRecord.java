package edu.towson.cosc442.halls7800.persist;

import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Everything needed to recreate a run: the seed, difficulty, player name, and every action taken.
 * A saved game is a run record; so is a bug-report replay.
 *
 * <p>Text format (one item per line):
 * <pre>
 * HALLS7800-RUN 3
 * seed=12345
 * difficulty=UNDERGRADUATE
 * name=Ada
 * M:NORTH
 * U:2
 * ...
 * </pre>
 *
 * @param seed the run seed
 * @param difficulty the difficulty
 * @param playerName the adventurer's name
 * @param actions the recorded action tokens, oldest first
 * @param version the record version, which identifies the action rules used for replay
 */
public record RunRecord(long seed, Difficulty difficulty, String playerName, List<String> actions, int version) {

    /** First line of every record file. */
    public static final String HEADER = "HALLS7800-RUN";
    /** Version used for new runs. */
    public static final int VERSION = ActionRules.CURRENT.recordVersion();

    /**
     * Validates and copies the fields.
     */
    public RunRecord {
        ActionRules.forRecordVersion(version);
        Objects.requireNonNull(difficulty, "difficulty");
        Objects.requireNonNull(playerName, "playerName");
        actions = List.copyOf(actions);
    }

    /** Creates a record for a run using the current action rules. */
    public RunRecord(long seed, Difficulty difficulty, String playerName, List<String> actions) {
        this(seed, difficulty, playerName, actions, VERSION);
    }

    /**
     * Captures a session's record so far.
     *
     * @param session a running or finished session
     * @return its record
     */
    public static RunRecord of(GameSession session) {
        int version = session.actionRules().recordVersion();
        return new RunRecord(session.seed(), session.difficulty(), session.player().name(), session.recording(), version);
    }

    /** @return the record as text */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append(' ').append(version).append('\n');
        sb.append("seed=").append(seed).append('\n');
        sb.append("difficulty=").append(difficulty.name()).append('\n');
        sb.append("name=").append(playerName.replace('\n', ' ')).append('\n');
        for (String action : actions) {
            sb.append(action).append('\n');
        }
        return sb.toString();
    }

    /**
     * Parses a record written by {@link #toText()}.
     *
     * @param text the record text
     * @return the record
     * @throws IllegalArgumentException if the text is not a valid record of a supported version
     */
    public static RunRecord parse(String text) {
        String[] lines = text.replace("\r", "").split("\n");
        if (lines.length < 4 || !lines[0].startsWith(HEADER + " ")) {
            throw new IllegalArgumentException("Not a Halls of 7800 run record");
        }
        int version = Integer.parseInt(lines[0].substring(HEADER.length() + 1).trim());
        long seed = Long.parseLong(value(lines[1], "seed"));
        Difficulty difficulty = Difficulty.valueOf(value(lines[2], "difficulty"));
        String name = value(lines[3], "name");
        List<String> actions = new ArrayList<>();
        for (int i = 4; i < lines.length; i++) {
            if (!lines[i].isBlank()) {
                actions.add(lines[i].trim());
            }
        }
        return new RunRecord(seed, difficulty, name, actions, version);
    }

    ActionRules actionRules() {
        return ActionRules.forRecordVersion(version);
    }

    private static String value(String line, String key) {
        if (!line.startsWith(key + "=")) {
            throw new IllegalArgumentException("Expected '" + key + "=' but found '" + line + "'");
        }
        return line.substring(key.length() + 1);
    }
}
