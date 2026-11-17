package edu.towson.cosc442.halls7800.progression;

import edu.towson.cosc442.halls7800.engine.Difficulty;
import edu.towson.cosc442.halls7800.engine.GamePhase;
import edu.towson.cosc442.halls7800.engine.GameEvent;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.engine.RunStats;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.item.Potion;
import edu.towson.cosc442.halls7800.quest.Quest;
import edu.towson.cosc442.halls7800.world.Zone;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Notices achievements earned during one run. Most are checked from the game state once per
 * turn by {@link #check(GameSession)}; a few depend on a moment in time and are reported
 * directly with {@link #unlock(GameSession, Achievement)}.
 *
 * <p>Earning an achievement never changes the game state (it only adds a message), so it does
 * not affect replays.
 */
public class AchievementTracker {

    /** Undercroft kills needed for {@link Achievement#BUG_SQUASHER}. */
    public static final int BUG_SQUASHER_KILLS = 20;
    /** Gold needed for {@link Achievement#TUITION_PAID}. */
    public static final int TUITION_GOLD = 1000;
    /** Turn limit for {@link Achievement#ACCELERATED}. */
    public static final int ACCELERATED_TURNS = 4000;
    /** Traps needed for {@link Achievement#EDGE_CASES}. */
    public static final int EDGE_CASE_TRAPS = 5;
    /** Companion kills needed for {@link Achievement#TIGER_PRIDE}. */
    public static final int TIGER_PRIDE_KILLS = 10;

    private final Set<Achievement> earned = EnumSet.noneOf(Achievement.class);

    /**
     * Records an achievement if it is new this run, and announces it.
     *
     * @param session the game (for the announcement)
     * @param achievement the achievement
     * @return {@code true} if it was newly earned
     */
    public boolean unlock(GameSession session, Achievement achievement) {
        if (!earned.add(achievement)) {
            return false;
        }
        session.message("Achievement unlocked: " + achievement.title() + "!");
        session.signal(GameEvent.Type.ACHIEVEMENT, session.player().position());
        return true;
    }

    /**
     * Checks every state-based achievement.
     *
     * @param session the game
     */
    public void check(GameSession session) {
        RunStats stats = session.stats();
        if (session.isTutorial()) {
            if (session.phase() == GamePhase.VICTORY) {
                unlock(session, Achievement.ORIENTATION);
            }
            return;
        }
        Map<MonsterType, Integer> kills = stats.killsByType();
        if (stats.totalKills() >= 1) {
            unlock(session, Achievement.HELLO_WORLD);
        }
        if (undercroftKills(kills) >= BUG_SQUASHER_KILLS) {
            unlock(session, Achievement.BUG_SQUASHER);
        }
        if (kills.containsKey(MonsterType.SPAGHETTI_HYDRA)) {
            unlock(session, Achievement.UNTANGLED);
        }
        if (kills.containsKey(MonsterType.DEADLOCK_COLOSSUS)) {
            unlock(session, Achievement.DEADLOCK_BREAKER);
        }
        if (kills.containsKey(MonsterType.ZERO_DAY_WYRM)) {
            unlock(session, Achievement.PATCH_TUESDAY);
        }
        if (session.player().gold() >= TUITION_GOLD) {
            unlock(session, Achievement.TUITION_PAID);
        }
        if (stats.trapsTriggered() >= EDGE_CASE_TRAPS && session.player().isAlive()) {
            unlock(session, Achievement.EDGE_CASES);
        }
        if (session.companion() != null && session.companion().kills() >= TIGER_PRIDE_KILLS) {
            unlock(session, Achievement.TIGER_PRIDE);
        }
        if (session.identification().knownPotions().size() == Potion.Kind.values().length) {
            unlock(session, Achievement.FULL_COVERAGE);
        }
        checkQuest(session, Quest.Id.LOST_REQUIREMENTS, Achievement.REQUIREMENTS_MET);
        checkQuest(session, Quest.Id.MUTATION_TRIAL, Achievement.MUTATION_SCORE);
        checkQuest(session, Quest.Id.CARTOGRAPHY, Achievement.LEVEL_DESIGNER);
        checkQuest(session, Quest.Id.SECURE_VAULTS, Achievement.SECURE_BY_DESIGN);
        checkQuest(session, Quest.Id.TRANSLATION, Achievement.POLYGLOT);
        if (session.phase() == GamePhase.VICTORY) {
            unlock(session, Achievement.COMMENCEMENT);
            if (session.difficulty() == Difficulty.GRADUATE) {
                unlock(session, Achievement.DEANS_LIST);
            }
            if (session.turn() < ACCELERATED_TURNS) {
                unlock(session, Achievement.ACCELERATED);
            }
        }
    }

    /**
     * Called when a boss falls, for {@link Achievement#CLEAN_CODE}.
     *
     * @param session the game
     */
    public void onBossDefeated(GameSession session) {
        if (session.stats().potionsDrunk() == 0) {
            unlock(session, Achievement.CLEAN_CODE);
        }
    }

    private void checkQuest(GameSession session, Quest.Id quest, Achievement achievement) {
        if (session.quests().state(quest) == Quest.State.COMPLETED) {
            unlock(session, achievement);
        }
    }

    private static int undercroftKills(Map<MonsterType, Integer> kills) {
        int total = 0;
        for (Map.Entry<MonsterType, Integer> e : kills.entrySet()) {
            MonsterType t = e.getKey();
            if (t.minDepth() >= Zone.UNDERCROFT.firstDepth() && t.maxDepth() <= Zone.UNDERCROFT.lastDepth() + 2
                    && t.minDepth() <= Zone.UNDERCROFT.lastDepth() && t.spawnWeight() > 0) {
                total += e.getValue();
            }
        }
        return total;
    }

    /** @return achievements earned this run */
    public Set<Achievement> earned() {
        return Collections.unmodifiableSet(earned);
    }
}
