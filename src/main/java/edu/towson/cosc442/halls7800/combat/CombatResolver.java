package edu.towson.cosc442.halls7800.combat;

import edu.towson.cosc442.halls7800.effect.EffectManager;
import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.entity.Creature;
import edu.towson.cosc442.halls7800.util.Rng;

/**
 * The combat formulas. Resolving an attack changes the defender's HP and effects but produces
 * no messages; see {@link Combat} for the version used during play.
 *
 * <p>Rules:
 * <ol>
 *   <li>Hit chance is {@value #BASE_HIT_PERCENT}% plus {@value #PER_POINT_PERCENT}% per point of
 *       accuracy above the defender's evasion, limited to {@value #MIN_HIT_PERCENT}–
 *       {@value #MAX_HIT_PERCENT}%. A defender that is asleep or stunned is always hit.</li>
 *   <li>A hit is critical with the attack's crit chance (always, against a sleeping defender).
 *       Critical hits deal 1.5x damage and ignore armor.</li>
 *   <li>Damage is rolled in the attack's range and scaled by the attacker's weakness.</li>
 *   <li>Physical damage is reduced by a roll from 0 to the defender's armor.</li>
 *   <li>Immune defenders take no damage. A shield absorbs what it can.</li>
 *   <li>If damage got through, the attack's on-hit effect may take hold.</li>
 * </ol>
 */
public final class CombatResolver {

    /** Hit chance when accuracy equals evasion. */
    public static final int BASE_HIT_PERCENT = 70;
    /** Hit chance change per point of accuracy minus evasion. */
    public static final int PER_POINT_PERCENT = 5;
    /** Lowest possible hit chance. */
    public static final int MIN_HIT_PERCENT = 5;
    /** Highest possible hit chance. */
    public static final int MAX_HIT_PERCENT = 95;

    private CombatResolver() {
    }

    /**
     * @param accuracy attacker accuracy
     * @param evasion defender evasion
     * @return percent chance to hit
     */
    public static int hitChance(int accuracy, int evasion) {
        int chance = BASE_HIT_PERCENT + PER_POINT_PERCENT * (accuracy - evasion);
        return Math.max(MIN_HIT_PERCENT, Math.min(MAX_HIT_PERCENT, chance));
    }

    /**
     * Resolves one attack and applies its results to the defender.
     *
     * @param attacker the attacking creature
     * @param defender the defending creature
     * @param attack the attack being made
     * @param rng randomness source (the combat stream during play)
     * @return what happened
     */
    public static AttackResult resolve(Creature attacker, Creature defender, AttackProfile attack, Rng rng) {
        boolean helpless = defender.isHelpless();
        int accuracy = attack.accuracy() + attacker.effects().accuracyModifier();
        if (!helpless && rng.nextInt(100) >= hitChance(accuracy, defender.evasion())) {
            return AttackResult.miss();
        }

        boolean critical = defender.isAsleep() || rng.nextInt(100) < attack.critPercent();
        int damage = rng.nextInt(attack.minDamage(), attack.maxDamage());
        damage = damage * attacker.effects().damagePercent() / 100;
        if (critical) {
            damage = (damage * 3 + 1) / 2;
        } else if (attack.damageType().reducedByArmor() && defender.armor() > 0) {
            damage -= rng.nextInt(0, defender.armor());
        }
        damage = Math.max(0, damage);

        if (defender.isImmuneTo(attack.damageType())) {
            return new AttackResult(true, critical, 0, 0, true, false, null);
        }
        EffectManager defenderEffects = defender.effects();
        int afterShield = defenderEffects.absorb(damage);
        int absorbed = damage - afterShield;
        int dealt = defender.takeDamage(afterShield);

        EffectType inflicted = null;
        if (damage > 0 && defender.isAlive() && attack.onHitEffect() != null
                && rng.nextInt(100) < attack.onHitPercent()) {
            EffectManager.Outcome outcome = defender.applyEffect(attack.onHitEffect(),
                    attack.effectDuration(), attack.effectMagnitude());
            if (outcome == EffectManager.Outcome.APPLIED || outcome == EffectManager.Outcome.STACKED) {
                inflicted = attack.onHitEffect();
            }
        }
        return new AttackResult(true, critical, dealt, absorbed, false, !defender.isAlive(), inflicted);
    }
}
