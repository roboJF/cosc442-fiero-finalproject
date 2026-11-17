package edu.towson.cosc442.halls7800.progression;

import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.RunStats;
import edu.towson.cosc442.halls7800.quest.Quest;

/**
 * Computes a run's score.
 *
 * <pre>
 * base  = 150 x deepest depth
 *       + 2 x experience
 *       + gold collected
 *       + 500 x bosses defeated
 *       + 300 x side quests completed
 *       + on victory: 5000, plus a speed bonus of max(0, 3000 - turns / 2)
 * score = base x difficulty score percent / 100
 * </pre>
 *
 * Tutorial runs always score 0.
 */
public final class ScoreCalculator {

    /** Points per depth reached. */
    public static final int DEPTH_POINTS = 150;
    /** Points per experience point. */
    public static final int XP_POINTS = 2;
    /** Points per boss. */
    public static final int BOSS_POINTS = 500;
    /** Points per completed side quest. */
    public static final int QUEST_POINTS = 300;
    /** Points for winning. */
    public static final int VICTORY_POINTS = 5000;
    /** Largest possible speed bonus on a win. */
    public static final int MAX_SPEED_BONUS = 3000;

    private ScoreCalculator() {
    }

    /**
     * @param session a run (finished or not)
     * @return its score
     */
    public static int score(GameSession session) {
        if (session.isTutorial()) {
            return 0;
        }
        RunStats stats = session.stats();
        int base = DEPTH_POINTS * session.deepestReached()
                + XP_POINTS * session.player().experience()
                + stats.goldCollected()
                + BOSS_POINTS * stats.bossesDefeated()
                + QUEST_POINTS * completedSideQuests(session);
        int score = base * session.difficulty().scorePercent() / 100;
        if (session.phase() == GamePhase.VICTORY) {
            score += VICTORY_POINTS + Math.max(0, MAX_SPEED_BONUS - session.turn() / 2);
        }
        return score;
    }

    private static int completedSideQuests(GameSession session) {
        int count = 0;
        for (Quest quest : session.quests().started()) {
            if (quest.id() != Quest.Id.DIPLOMA && quest.state() == Quest.State.COMPLETED) {
                count++;
            }
        }
        return count;
    }
}
