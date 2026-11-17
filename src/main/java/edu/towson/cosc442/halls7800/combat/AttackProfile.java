package edu.towson.cosc442.halls7800.combat;

import edu.towson.cosc442.halls7800.effect.EffectType;

/**
 * Everything about one attack that the attacker contributes: how accurate it is, how hard it
 * hits, and what it may inflict.
 *
 * @param accuracy attacker's accuracy rating
 * @param minDamage smallest damage roll
 * @param maxDamage largest damage roll
 * @param damageType kind of damage
 * @param critPercent percent chance of a critical hit
 * @param onHitEffect effect that may be inflicted on a damaging hit, or {@code null}
 * @param onHitPercent percent chance of inflicting the effect
 * @param effectDuration duration of the inflicted effect
 * @param effectMagnitude magnitude of the inflicted effect
 * @param verb how the attack is described, e.g. "hit" or "bite"
 */
public record AttackProfile(int accuracy, int minDamage, int maxDamage, DamageType damageType, int critPercent,
                            EffectType onHitEffect, int onHitPercent, int effectDuration, int effectMagnitude,
                            String verb) {

    /**
     * Validates the damage range.
     */
    public AttackProfile {
        if (minDamage < 0 || maxDamage < minDamage) {
            throw new IllegalArgumentException("Bad damage range " + minDamage + "-" + maxDamage);
        }
    }

    /**
     * Creates a simple physical attack with no special effect.
     *
     * @param accuracy accuracy rating
     * @param minDamage smallest damage
     * @param maxDamage largest damage
     * @param critPercent critical chance
     * @param verb description verb
     * @return the profile
     */
    public static AttackProfile basic(int accuracy, int minDamage, int maxDamage, int critPercent, String verb) {
        return new AttackProfile(accuracy, minDamage, maxDamage, DamageType.PHYSICAL, critPercent, null, 0, 0, 0, verb);
    }

    /**
     * @param bonus accuracy to add
     * @return a copy with adjusted accuracy
     */
    public AttackProfile withAccuracyBonus(int bonus) {
        return new AttackProfile(accuracy + bonus, minDamage, maxDamage, damageType, critPercent,
                onHitEffect, onHitPercent, effectDuration, effectMagnitude, verb);
    }
}
