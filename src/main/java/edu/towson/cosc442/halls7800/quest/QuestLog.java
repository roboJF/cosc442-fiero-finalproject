package edu.towson.cosc442.halls7800.quest;

import edu.towson.cosc442.halls7800.engine.GameRules;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.entity.MonsterType;
import edu.towson.cosc442.halls7800.item.Inventory;
import edu.towson.cosc442.halls7800.item.QuestItem;
import edu.towson.cosc442.halls7800.world.Floor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * All quests in a run, plus the small amount of extra state they need. Progress is recomputed
 * from the game state once per turn by {@link #update(GameSession)}.
 */
public class QuestLog {

    /** Fraction (percent) of a floor's open tiles that counts as "mostly explored". */
    public static final int EXPLORED_PERCENT = 70;

    private final Map<Quest.Id, Quest> quests = new EnumMap<>(Quest.Id.class);
    private int mutantsKilled;
    private int vaultsUnlocked;
    private boolean mutantsReleased;
    private boolean tigerBlessingUsed;

    /** Creates the log with every quest not yet started. */
    public QuestLog() {
        for (Quest.Id id : Quest.Id.values()) {
            quests.put(id, new Quest(id));
        }
    }

    /**
     * @param id a quest
     * @return its record
     */
    public Quest get(Quest.Id id) {
        return quests.get(id);
    }

    /**
     * @param id a quest
     * @return its state
     */
    public Quest.State state(Quest.Id id) {
        return quests.get(id).state();
    }

    /** @return started quests, in declaration order */
    public List<Quest> started() {
        List<Quest> result = new ArrayList<>();
        for (Quest q : quests.values()) {
            if (q.state() != Quest.State.NOT_STARTED) {
                result.add(q);
            }
        }
        return result;
    }

    /**
     * Recomputes progress of every open quest from the current game state.
     *
     * @param session the game
     */
    public void update(GameSession session) {
        Inventory pack = session.player().inventory();
        get(Quest.Id.DIPLOMA).setProgress(countQuestItems(pack, QuestItem.Kind.DIPLOMA));
        get(Quest.Id.LOST_REQUIREMENTS).setProgress(countQuestItems(pack, QuestItem.Kind.REQUIREMENTS_PAGE));
        get(Quest.Id.MUTATION_TRIAL).setProgress(mutantsKilled);
        get(Quest.Id.CARTOGRAPHY).setProgress(exploredFloors(session));
        get(Quest.Id.SECURE_VAULTS).setProgress(vaultsUnlocked);
        get(Quest.Id.TRANSLATION).setProgress(session.identification().knownScrolls().size());
    }

    /**
     * @param pack the player's pack
     * @param kind a quest item kind
     * @return how many of that item the pack holds
     */
    public static int countQuestItems(Inventory pack, QuestItem.Kind kind) {
        return (int) pack.ofType(QuestItem.class).stream()
                .filter(q -> q.kind() == kind)
                .count();
    }

    /**
     * @param session the game
     * @return how many visited floors below the Atrium (and above the Sanctum) are mostly explored
     */
    public static int exploredFloors(GameSession session) {
        int count = 0;
        for (int depth : session.visitedDepths()) {
            if (depth == GameRules.HUB_DEPTH || depth == GameRules.FINAL_DEPTH) {
                continue;
            }
            Floor floor = session.visitedFloor(depth);
            int open = floor.openTileCount();
            if (open > 0 && floor.exploredOpenTileCount() * 100 >= open * EXPLORED_PERCENT) {
                count++;
            }
        }
        return count;
    }

    /**
     * Notes that the player defeated a monster.
     *
     * @param type the monster's type
     */
    public void onMonsterKilled(MonsterType type) {
        if (type == MonsterType.MUTANT && get(Quest.Id.MUTATION_TRIAL).isOpen()) {
            mutantsKilled++;
        }
    }

    /** Notes that the player unlocked a vault door. */
    public void onVaultUnlocked() {
        vaultsUnlocked++;
    }

    /** @return vault doors unlocked this run */
    public int vaultsUnlocked() {
        return vaultsUnlocked;
    }

    /** @return mutants killed during the trial */
    public int mutantsKilled() {
        return mutantsKilled;
    }

    /** @return {@code true} once Warden Deng has released the mutants */
    public boolean mutantsReleased() {
        return mutantsReleased;
    }

    /** Records that the mutants have been released. */
    public void markMutantsReleased() {
        mutantsReleased = true;
    }

    /** @return {@code true} if the tiger statue's blessing has been used this run */
    public boolean tigerBlessingUsed() {
        return tigerBlessingUsed;
    }

    /** Records that the tiger statue's blessing has been used. */
    public void markTigerBlessingUsed() {
        tigerBlessingUsed = true;
    }
}
