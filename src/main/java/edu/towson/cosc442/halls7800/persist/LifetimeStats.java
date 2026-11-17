package edu.towson.cosc442.halls7800.persist;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.RunStats;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Totals across every finished run (tutorials excluded), kept in a simple {@code key=value} text
 * file by {@link SaveStore}.
 */
public class LifetimeStats {

    private static final String CAUSE_PREFIX = "cause:";
    private static final String DIFFICULTY_PREFIX = "difficulty:";

    private int runs;
    private int wins;
    private int deaths;
    private long totalTurns;
    private int totalKills;
    private int bossesDefeated;
    private long goldCollected;
    private int deepestDepth;
    private int bestScore;
    private int potionsDrunk;
    private int scrollsRead;
    private final Map<String, Integer> causesOfDeath = new TreeMap<>();
    private final Map<Difficulty, Integer> runsByDifficulty = new EnumMap<>(Difficulty.class);

    /**
     * Adds a finished run to the totals.
     *
     * @param session the finished run
     * @param score its final score
     */
    public void record(GameSession session, int score) {
        if (session.isTutorial()) {
            return;
        }
        RunStats stats = session.stats();
        runs++;
        if (session.phase() == GamePhase.VICTORY) {
            wins++;
        } else if (session.phase() == GamePhase.DEAD) {
            deaths++;
            String cause = stats.causeOfDeath() == null ? "unknown causes" : stats.causeOfDeath();
            causesOfDeath.merge(cause, 1, Integer::sum);
        }
        totalTurns += session.turn();
        totalKills += stats.totalKills();
        bossesDefeated += stats.bossesDefeated();
        goldCollected += stats.goldCollected();
        deepestDepth = Math.max(deepestDepth, session.deepestReached());
        bestScore = Math.max(bestScore, score);
        potionsDrunk += stats.potionsDrunk();
        scrollsRead += stats.scrollsRead();
        runsByDifficulty.merge(session.difficulty(), 1, Integer::sum);
    }

    /** @return percentage of runs won, rounded down (0 if no runs) */
    public int winPercent() {
        return runs == 0 ? 0 : wins * 100 / runs;
    }

    /** @return average turns per run, rounded down (0 if no runs) */
    public long averageTurns() {
        return runs == 0 ? 0 : totalTurns / runs;
    }

    /** @return the most frequent cause of death, or {@code null} if the player has never died */
    public String mostCommonCause() {
        String best = null;
        int bestCount = 0;
        for (Map.Entry<String, Integer> e : causesOfDeath.entrySet()) {
            if (e.getValue() > bestCount) {
                best = e.getKey();
                bestCount = e.getValue();
            }
        }
        return best;
    }

    /** @return the file contents */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        line(sb, "runs", runs);
        line(sb, "wins", wins);
        line(sb, "deaths", deaths);
        line(sb, "totalTurns", totalTurns);
        line(sb, "totalKills", totalKills);
        line(sb, "bossesDefeated", bossesDefeated);
        line(sb, "goldCollected", goldCollected);
        line(sb, "deepestDepth", deepestDepth);
        line(sb, "bestScore", bestScore);
        line(sb, "potionsDrunk", potionsDrunk);
        line(sb, "scrollsRead", scrollsRead);
        for (Map.Entry<String, Integer> e : causesOfDeath.entrySet()) {
            line(sb, CAUSE_PREFIX + e.getKey().replace('=', '-'), e.getValue());
        }
        for (Map.Entry<Difficulty, Integer> e : runsByDifficulty.entrySet()) {
            line(sb, DIFFICULTY_PREFIX + e.getKey().name(), e.getValue());
        }
        return sb.toString();
    }

    private static void line(StringBuilder sb, String key, long value) {
        sb.append(key).append('=').append(value).append('\n');
    }

    /**
     * Reads totals written by {@link #toText()}. Unknown or malformed lines are ignored.
     *
     * @param text the file contents
     * @return the totals
     */
    public static LifetimeStats parse(String text) {
        LifetimeStats s = new LifetimeStats();
        for (String line : text.split("\\R")) {
            int eq = line.lastIndexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq);
            long value;
            try {
                value = Long.parseLong(line.substring(eq + 1).trim());
            } catch (NumberFormatException e) {
                continue;
            }
            s.apply(key, value);
        }
        return s;
    }

    private void apply(String key, long value) {
        int v = (int) value;
        switch (key) {
            case "runs" -> runs = v;
            case "wins" -> wins = v;
            case "deaths" -> deaths = v;
            case "totalTurns" -> totalTurns = value;
            case "totalKills" -> totalKills = v;
            case "bossesDefeated" -> bossesDefeated = v;
            case "goldCollected" -> goldCollected = value;
            case "deepestDepth" -> deepestDepth = v;
            case "bestScore" -> bestScore = v;
            case "potionsDrunk" -> potionsDrunk = v;
            case "scrollsRead" -> scrollsRead = v;
            default -> {
                if (key.startsWith(CAUSE_PREFIX)) {
                    causesOfDeath.put(key.substring(CAUSE_PREFIX.length()), v);
                } else if (key.startsWith(DIFFICULTY_PREFIX)) {
                    try {
                        runsByDifficulty.put(Difficulty.valueOf(key.substring(DIFFICULTY_PREFIX.length())), v);
                    } catch (IllegalArgumentException e) {
                        // a difficulty from another version
                    }
                }
            }
        }
    }

    /** @return finished runs */
    public int runs() {
        return runs;
    }

    /** @return runs won */
    public int wins() {
        return wins;
    }

    /** @return runs ended by death */
    public int deaths() {
        return deaths;
    }

    /** @return turns across all runs */
    public long totalTurns() {
        return totalTurns;
    }

    /** @return monsters defeated across all runs */
    public int totalKills() {
        return totalKills;
    }

    /** @return bosses defeated across all runs */
    public int bossesDefeated() {
        return bossesDefeated;
    }

    /** @return gold collected across all runs */
    public long goldCollected() {
        return goldCollected;
    }

    /** @return deepest depth ever reached */
    public int deepestDepth() {
        return deepestDepth;
    }

    /** @return best score ever */
    public int bestScore() {
        return bestScore;
    }

    /** @return potions drunk across all runs */
    public int potionsDrunk() {
        return potionsDrunk;
    }

    /** @return scrolls read across all runs */
    public int scrollsRead() {
        return scrollsRead;
    }

    /** @return deaths per cause */
    public Map<String, Integer> causesOfDeath() {
        return Collections.unmodifiableMap(causesOfDeath);
    }

    /** @return runs per difficulty */
    public Map<Difficulty, Integer> runsByDifficulty() {
        return Collections.unmodifiableMap(runsByDifficulty);
    }
}
