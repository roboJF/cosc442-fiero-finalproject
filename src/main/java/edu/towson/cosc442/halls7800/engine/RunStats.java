package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.entity.MonsterType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Statistics collected over a run, used for the end-of-run summary and the score.
 */
public class RunStats {

    private final Map<MonsterType, Integer> kills = new EnumMap<>(MonsterType.class);
    private int damageDealt;
    private int damageTaken;
    private int potionsDrunk;
    private int scrollsRead;
    private int goldCollected;
    private int trapsTriggered;
    private int bossesDefeated;
    private String causeOfDeath;

    /**
     * @param type a defeated monster's type
     */
    public void recordKill(MonsterType type) {
        kills.merge(type, 1, Integer::sum);
    }

    /** @return kills per monster type */
    public Map<MonsterType, Integer> killsByType() {
        return Collections.unmodifiableMap(kills);
    }

    /** @return total monsters defeated */
    public int totalKills() {
        int total = 0;
        for (int n : kills.values()) {
            total += n;
        }
        return total;
    }

    /** @param amount damage the player dealt */
    public void addDamageDealt(int amount) {
        damageDealt += Math.max(0, amount);
    }

    /** @param amount damage the player took */
    public void addDamageTaken(int amount) {
        damageTaken += Math.max(0, amount);
    }

    /** Counts one potion drunk. */
    public void recordPotion() {
        potionsDrunk++;
    }

    /** Counts one scroll read. */
    public void recordScroll() {
        scrollsRead++;
    }

    /** @param amount gold picked up from the floor */
    public void addGoldCollected(int amount) {
        goldCollected += Math.max(0, amount);
    }

    /** Counts one trap set off. */
    public void recordTrap() {
        trapsTriggered++;
    }

    /** Counts one boss defeated. */
    public void recordBoss() {
        bossesDefeated++;
    }

    /** @param cause how the run ended in death */
    public void setCauseOfDeath(String cause) {
        causeOfDeath = cause;
    }

    /** @return damage the player dealt */
    public int damageDealt() {
        return damageDealt;
    }

    /** @return damage the player took */
    public int damageTaken() {
        return damageTaken;
    }

    /** @return potions drunk */
    public int potionsDrunk() {
        return potionsDrunk;
    }

    /** @return scrolls read */
    public int scrollsRead() {
        return scrollsRead;
    }

    /** @return gold picked up from the floor */
    public int goldCollected() {
        return goldCollected;
    }

    /** @return traps set off */
    public int trapsTriggered() {
        return trapsTriggered;
    }

    /** @return bosses defeated (including the Registrar) */
    public int bossesDefeated() {
        return bossesDefeated;
    }

    /** @return cause of death, or {@code null} if the player has not died */
    public String causeOfDeath() {
        return causeOfDeath;
    }
}
