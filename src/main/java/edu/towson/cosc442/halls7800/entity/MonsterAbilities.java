package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.AttackResult;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.world.Position;

/**
 * Special reactions some monsters have during combat.
 *
 * <ul>
 *   <li>{@link MonsterTrait#SPLITS}: when wounded but not killed, the monster divides its
 *       remaining HP with a copy of itself on a free adjacent tile (at most
 *       {@link #MAX_SPLIT_GENERATIONS} times down a line of copies).</li>
 *   <li>{@link MonsterTrait#THIEF}: when it hurts the player, it grabs some gold and runs; the
 *       gold is dropped if it is defeated.</li>
 * </ul>
 */
public final class MonsterAbilities {

    /** How many times a line of splitting monsters can divide. */
    public static final int MAX_SPLIT_GENERATIONS = 2;
    /** A splitting monster needs at least this much HP left to divide. */
    public static final int MIN_SPLIT_HP = 4;
    /** Turns a thief flees after stealing. */
    public static final int THIEF_FLEE_TURNS = 30;

    private MonsterAbilities() {
    }

    /**
     * Called after a monster has been hit.
     *
     * @param session the game
     * @param monster the monster that was hit
     * @param result the attack's outcome
     */
    public static void afterHit(GameSession session, Monster monster, AttackResult result) {
        if (!monster.isAlive() || result.damage() <= 0) {
            return;
        }
        if (monster.type().has(MonsterTrait.SPLITS) && monster.generation() < MAX_SPLIT_GENERATIONS
                && monster.hp() >= MIN_SPLIT_HP) {
            split(session, monster);
        }
    }

    /**
     * Called after a monster has hit the player.
     *
     * @param session the game
     * @param monster the attacker
     * @param result the attack's outcome
     */
    public static void afterHittingPlayer(GameSession session, Monster monster, AttackResult result) {
        if (!monster.type().has(MonsterTrait.THIEF) || result.damage() <= 0 || monster.stolenGold() > 0) {
            return;
        }
        Player player = session.player();
        int take = Math.min(player.gold(), 15 + session.depth() * 5);
        if (take <= 0) {
            return;
        }
        player.spendGold(take);
        monster.setStolenGold(take);
        monster.frighten(THIEF_FLEE_TURNS);
        session.message(monster.subject() + " hijacks " + take + " of your gold and makes a run for it!");
    }

    private static void split(GameSession session, Monster parent) {
        for (Position p : parent.position().neighbors()) {
            if (session.floor().isPassable(p) && !session.isOccupied(p)) {
                int half = parent.hp() / 2;
                parent.takeDamage(half);
                Monster child = new Monster(parent.type(), p, false);
                child.takeDamage(child.hp() - half);
                parent.incrementGeneration();
                child.setGeneration(parent.generation());
                child.alert();
                child.setLastKnownPlayerPosition(session.player().position());
                session.addMonster(child);
                session.messageIfVisible(parent.position(), parent.subject() + " splits in two! The leak is spreading.");
                return;
            }
        }
    }
}
