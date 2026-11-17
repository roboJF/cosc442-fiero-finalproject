package edu.towson.cosc442.halls7800.engine;

import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Creature;

/** Gameplay rules that must remain stable while replaying a saved run. */
public enum ActionRules {
    /** Rules used by version 1 run records. */
    LEGACY,
    /** Rules used by new runs. */
    CURRENT;

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
