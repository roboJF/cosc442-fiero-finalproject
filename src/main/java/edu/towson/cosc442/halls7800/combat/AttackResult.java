package edu.towson.cosc442.halls7800.combat;

import edu.towson.cosc442.halls7800.effect.EffectType;

/**
 * What happened in one attack.
 *
 * @param hit whether the attack connected
 * @param critical whether it was a critical hit
 * @param damage HP actually removed from the defender
 * @param absorbed damage soaked by a shield
 * @param immune whether the defender was immune to the damage type
 * @param killed whether the defender died
 * @param inflicted effect that took hold on the defender, or {@code null}
 */
public record AttackResult(boolean hit, boolean critical, int damage, int absorbed, boolean immune,
                           boolean killed, EffectType inflicted) {

    /** @return a result for a missed attack */
    public static AttackResult miss() {
        return new AttackResult(false, false, 0, 0, false, false, null);
    }
}
