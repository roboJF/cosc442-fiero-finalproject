package edu.towson.cosc442.halls7800.combat;

import edu.towson.cosc442.halls7800.engine.GameEvent;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.entity.Companion;
import edu.towson.cosc442.halls7800.entity.Creature;
import edu.towson.cosc442.halls7800.entity.Monster;
import edu.towson.cosc442.halls7800.entity.MonsterAbilities;
import edu.towson.cosc442.halls7800.util.RandomStream;

/**
 * Carries out attacks during play: resolves them with {@link CombatResolver} using the session's
 * combat random stream, reports what happened in the message log, wakes the victim, and handles
 * deaths.
 */
public final class Combat {

    private Combat() {
    }

    /**
     * Makes {@code attacker} attack {@code defender}.
     *
     * @param session the game
     * @param attacker who attacks
     * @param defender who is attacked
     * @param ranged {@code true} to use the attacker's ranged attack
     * @return the result
     * @throws IllegalArgumentException if a ranged attack is requested from a creature without one
     */
    public static AttackResult attack(GameSession session, Creature attacker, Creature defender, boolean ranged) {
        AttackProfile profile = ranged ? attacker.rangedAttack() : attacker.meleeAttack();
        if (profile == null) {
            throw new IllegalArgumentException(attacker.name() + " has no ranged attack");
        }
        return attackWith(session, attacker, defender, profile, ranged);
    }

    /**
     * Makes {@code attacker} attack {@code defender} with a specific attack, such as a thrown
     * missile.
     *
     * @param session the game
     * @param attacker who attacks
     * @param defender who is attacked
     * @param profile the attack to use
     * @param ranged whether the attack is made from a distance (affects message wording)
     * @return the result
     */
    public static AttackResult attackWith(GameSession session, Creature attacker, Creature defender,
                                          AttackProfile profile, boolean ranged) {
        AttackResult result = CombatResolver.resolve(attacker, defender, profile, session.rng(RandomStream.COMBAT));
        report(session, attacker, defender, profile, result, ranged);
        if (defender.isPlayer() && result.hit()) {
            session.signal(GameEvent.Type.PLAYER_HURT, defender.position());
        } else if (attacker.isPlayer() || attacker instanceof Companion || defender.isPlayer()) {
            session.signal(result.hit() ? GameEvent.Type.ATTACK_HIT : GameEvent.Type.ATTACK_MISSED, defender.position());
        }
        if (attacker.isPlayer()) {
            session.stats().addDamageDealt(result.damage());
        } else if (defender.isPlayer()) {
            session.stats().addDamageTaken(result.damage());
        }

        if (defender instanceof Monster monster && monster.isAlive()) {
            monster.alert();
            monster.setLastKnownPlayerPosition(session.player().position());
            if (result.hit()) {
                MonsterAbilities.afterHit(session, monster, result);
            }
        }
        if (attacker instanceof Monster monster && defender.isPlayer() && result.hit()) {
            MonsterAbilities.afterHittingPlayer(session, monster, result);
        }
        if (result.killed()) {
            if (defender.isPlayer()) {
                session.playerKilled("slain by " + attacker.object());
            } else if (defender instanceof Companion) {
                session.companionKnockedOut();
            } else if (defender instanceof Monster monster) {
                session.killMonster(monster, attacker);
            }
        }
        return result;
    }

    private static void report(GameSession session, Creature attacker, Creature defender, AttackProfile profile,
                               AttackResult result, boolean ranged) {
        boolean involvesPlayer = attacker.isPlayer() || defender.isPlayer()
                || attacker instanceof Companion || defender instanceof Companion;
        if (!involvesPlayer && !session.currentFloor().isVisible(defender.position())) {
            return;
        }
        StringBuilder text = new StringBuilder();
        if (!result.hit()) {
            if (ranged) {
                text.append(attacker.subject()).append(' ').append(attacker.verb(profile.verb())).append(' ')
                        .append(defender.object()).append(", but misses.");
            } else {
                text.append(attacker.subject()).append(' ').append(attacker.verb("miss")).append(' ')
                        .append(defender.object()).append('.');
            }
            session.message(text.toString());
            return;
        }
        if (result.critical()) {
            text.append("Critical! ");
        }
        text.append(attacker.subject()).append(' ').append(attacker.verb(profile.verb())).append(' ')
                .append(defender.object());
        if (result.immune()) {
            text.append(", but ").append(defender.object()).append(' ').append(defender.verb("are")).append(" unharmed.");
        } else if (result.damage() == 0 && result.absorbed() == 0) {
            text.append(", but it does no damage.");
        } else {
            text.append(" for ").append(result.damage());
            if (result.absorbed() > 0) {
                text.append(" (").append(result.absorbed()).append(" absorbed)");
            }
            text.append(result.critical() ? "!" : ".");
        }
        session.message(text.toString());
        if (result.inflicted() != null) {
            session.message(defender.subject() + " " + defender.verb("are") + " " + result.inflicted().adjective() + ".");
        }
    }
}
