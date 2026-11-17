package edu.towson.cosc442.halls7800.persist;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The best {@link #CAPACITY} runs, highest score first. Ties go to the deeper run, then to the
 * faster one, then to the earlier entry.
 */
public class HighScoreTable {

    /** Number of entries kept. */
    public static final int CAPACITY = 10;

    /**
     * One finished run.
     *
     * @param score final score
     * @param name adventurer's name
     * @param depth deepest depth reached
     * @param outcome how the run ended
     * @param difficulty difficulty played
     * @param turns turns taken
     * @param seed run seed
     * @param date when the run ended, as text (e.g. "2026-11-09")
     */
    public record Entry(int score, String name, int depth, GamePhase outcome, Difficulty difficulty, int turns,
                        long seed, String date) {

        /** @return the entry as one line of the scores file */
        public String toLine() {
            return String.join("|", String.valueOf(score), clean(name), String.valueOf(depth), outcome.name(),
                    difficulty.name(), String.valueOf(turns), String.valueOf(seed), clean(date));
        }

        /**
         * @param line a line written by {@link #toLine()}
         * @return the entry
         * @throws IllegalArgumentException if the line is malformed
         */
        public static Entry fromLine(String line) {
            String[] f = line.split("\\|", -1);
            if (f.length != 8) {
                throw new IllegalArgumentException("Expected 8 fields but found " + f.length);
            }
            return new Entry(Integer.parseInt(f[0]), f[1], Integer.parseInt(f[2]), GamePhase.valueOf(f[3]),
                    Difficulty.valueOf(f[4]), Integer.parseInt(f[5]), Long.parseLong(f[6]), f[7]);
        }

        private static String clean(String text) {
            return text == null ? "" : text.replace('|', '/').replace('\n', ' ');
        }
    }

    private static final Comparator<Entry> ORDER = Comparator.comparingInt(Entry::score).reversed()
            .thenComparing(Comparator.comparingInt(Entry::depth).reversed())
            .thenComparingInt(Entry::turns);

    private final List<Entry> entries = new ArrayList<>();

    /**
     * Adds a run if it is good enough to be listed.
     *
     * @param entry the run
     * @return its rank (1 = best), or 0 if it did not make the table
     */
    public int add(Entry entry) {
        entries.add(entry);
        entries.sort(ORDER);
        int index = entries.indexOf(entry);
        while (entries.size() > CAPACITY) {
            entries.remove(entries.size() - 1);
        }
        return index < CAPACITY ? index + 1 : 0;
    }

    /**
     * @param score a score
     * @return {@code true} if a run with that score would be listed
     */
    public boolean qualifies(int score) {
        return entries.size() < CAPACITY || score > entries.get(entries.size() - 1).score();
    }

    /** @return the entries, best first */
    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    /** @return the table as text, one entry per line */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            sb.append(e.toLine()).append('\n');
        }
        return sb.toString();
    }

    /**
     * Reads a table written by {@link #toText()}. Malformed lines are skipped.
     *
     * @param text the file contents
     * @return the table
     */
    public static HighScoreTable parse(String text) {
        HighScoreTable table = new HighScoreTable();
        for (String line : text.replace("\r", "").split("\n")) {
            if (line.isBlank()) {
                continue;
            }
            try {
                table.add(Entry.fromLine(line));
            } catch (IllegalArgumentException e) {
                // skip lines from older or damaged files
            }
        }
        return table;
    }
}
