package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Creature;

/** Gameplay rules that must remain stable while replaying a saved run. */
public enum ActionRules {
    /** Rules used by version 1 run records. */
    LEGACY(1),
    /** Version 2 introduced updated stun and guardian rules. */
    VERSION_2(2),
    /** Rules used by new runs. */
    CURRENT(3);

    private final int recordVersion;

    ActionRules(int recordVersion) {
        this.recordVersion = recordVersion;
    }

    /** @return the record version that replays with these rules */
    public int recordVersion() {
        return recordVersion;
    }

    /**
     * @param version a run record version
     * @return the rules used by that version
     * @throws IllegalArgumentException if the version is unsupported
     */
    public static ActionRules forRecordVersion(int version) {
        for (ActionRules rules : values()) {
            if (rules.recordVersion == version) {
                return rules;
            }
        }
        throw new IllegalArgumentException("Unsupported run record version " + version);
    }

    /** @return whether the version 3 shop, quest and shield fixes apply */
    public boolean usesProgressFixes() {
        return recordVersion >= 3;
    }

    /** @return whether a stun allows the creature to act at the start of the turn */
    public static boolean canAct(Creature creature) {
        return !creature.effects().has(EffectType.STUNNED);
    }

    /**
     * Keeps the turn eligibility captured before effects tick. Legacy non-player turns used the
     * stun state after the tick.
     *
     * @param creature whose effects just ticked
     * @param eligibleAtStart whether the creature could act when its turn began
     * @return whether the creature may proceed with its action
     */
    public boolean canActAfterEffects(Creature creature, boolean eligibleAtStart) {
        return this == LEGACY ? canAct(creature) : eligibleAtStart;
    }
}
