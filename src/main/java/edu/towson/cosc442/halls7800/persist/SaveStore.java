package edu.towson.cosc442.halls7800.persist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import edu.towson.cosc442.halls7800.progression.Achievement;

/**
 * Reads and writes the game's files in one directory (by default {@code ~/.halls7800}):
 * <ul>
 *   <li>{@value #CURRENT_RUN} — the saved, unfinished run</li>
 *   <li>{@value #LAST_RUN} — the record of the most recently finished run, kept for bug reports</li>
 *   <li>{@value #SCORES} — the high-score table</li>
 *   <li>{@value #ACHIEVEMENTS} — achievements earned in any run</li>
 *   <li>{@value #LIFETIME} — totals across all finished runs</li>
 * </ul>
 */
public class SaveStore {

    /** File name of the saved run. */
    public static final String CURRENT_RUN = "current-run.txt";
    /** File name of the last finished run. */
    public static final String LAST_RUN = "last-run.txt";
    /** File name of the high scores. */
    public static final String SCORES = "scores.txt";
    /** File name of the earned achievements. */
    public static final String ACHIEVEMENTS = "achievements.txt";
    /** File name of the lifetime statistics. */
    public static final String LIFETIME = "lifetime-stats.txt";

    private final Path directory;

    /**
     * @param directory where to keep the files (created when first written)
     */
    public SaveStore(Path directory) {
        this.directory = directory;
    }

    /**
     * @return a store in the user's home directory, or in the directory named by the
     *         {@code halls7800.home} system property if it is set
     */
    public static SaveStore standard() {
        String override = System.getProperty("halls7800.home");
        Path dir = override != null && !override.isBlank()
                ? Path.of(override)
                : Path.of(System.getProperty("user.home"), ".halls7800");
        return new SaveStore(dir);
    }

    /** @return the directory holding the files */
    public Path directory() {
        return directory;
    }

    /** @return {@code true} if an unfinished run is saved */
    public boolean hasSavedRun() {
        return Files.isRegularFile(directory.resolve(CURRENT_RUN));
    }

    /**
     * @param record the unfinished run to save
     */
    public void saveRun(RunRecord record) {
        write(CURRENT_RUN, record.toText());
    }

    /**
     * @return the saved run, or empty if there is none
     * @throws IllegalArgumentException if the file exists but is not a valid record
     */
    public Optional<RunRecord> loadRun() {
        return read(CURRENT_RUN).map(RunRecord::parse);
    }

    /** Deletes the saved run, if any. */
    public void deleteRun() {
        try {
            Files.deleteIfExists(directory.resolve(CURRENT_RUN));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Keeps the record of a finished run for later replay.
     *
     * @param record the finished run
     */
    public void saveLastRun(RunRecord record) {
        write(LAST_RUN, record.toText());
    }

    /** @return the high-score table (empty if there is no file yet) */
    public HighScoreTable loadScores() {
        return read(SCORES).map(HighScoreTable::parse).orElseGet(HighScoreTable::new);
    }

    /**
     * @param table the table to save
     */
    public void saveScores(HighScoreTable table) {
        write(SCORES, table.toText());
    }

    /** @return totals across all finished runs (empty totals if there is no file yet) */
    public LifetimeStats loadLifetimeStats() {
        return read(LIFETIME).map(LifetimeStats::parse).orElseGet(LifetimeStats::new);
    }

    /**
     * @param stats the totals to save
     */
    public void saveLifetimeStats(LifetimeStats stats) {
        write(LIFETIME, stats.toText());
    }

    /** @return the directory's record of the last finished run, or empty */
    public Optional<RunRecord> loadLastRun() {
        return read(LAST_RUN).map(RunRecord::parse);
    }

    /** @return every achievement earned in any run (unknown names in the file are ignored) */
    public Set<Achievement> loadAchievements() {
        Set<Achievement> result = EnumSet.noneOf(Achievement.class);
        read(ACHIEVEMENTS).ifPresent(text -> {
            for (String line : text.split("\\R")) {
                try {
                    if (!line.isBlank()) {
                        result.add(Achievement.valueOf(line.trim()));
                    }
                } catch (IllegalArgumentException e) {
                    // an achievement from another version
                }
            }
        });
        return result;
    }

    /**
     * Adds newly earned achievements to the saved set.
     *
     * @param earned achievements earned in a run
     * @return the achievements that were not saved before
     */
    public Set<Achievement> recordAchievements(Set<Achievement> earned) {
        Set<Achievement> all = loadAchievements();
        Set<Achievement> fresh = EnumSet.noneOf(Achievement.class);
        for (Achievement a : earned) {
            if (all.add(a)) {
                fresh.add(a);
            }
        }
        if (!fresh.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Achievement a : all) {
                sb.append(a.name()).append('\n');
            }
            write(ACHIEVEMENTS, sb.toString());
        }
        return fresh;
    }

    private void write(String name, String text) {
        try {
            Files.createDirectories(directory);
            Path target = directory.resolve(name);
            Path temp = directory.resolve(name + ".tmp");
            Files.writeString(temp, text, StandardCharsets.UTF_8);
            Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write " + name, e);
        }
    }

    private Optional<String> read(String name) {
        Path file = directory.resolve(name);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + name, e);
        }
    }
}
