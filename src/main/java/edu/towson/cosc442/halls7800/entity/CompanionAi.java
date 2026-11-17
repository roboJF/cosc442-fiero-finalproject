package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.Combat;
import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.Pathfinder;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.List;

/**
 * Doc's decisions, in priority order:
 * <ol>
 *   <li>attack an adjacent monster;</li>
 *   <li>if healthy, chase the nearest monster the player can see within {@link #HUNT_RANGE};</li>
 *   <li>otherwise stay within {@link #FOLLOW_DISTANCE} tiles of the player.</li>
 * </ol>
 * Doc ignores sleeping monsters unless they are adjacent, so Doc will not wake a whole room.
 */
public final class CompanionAi {

    /** Doc chases monsters up to this far away. */
    public static final int HUNT_RANGE = 6;
    /** Doc tries to stay at most this far from the player. */
    public static final int FOLLOW_DISTANCE = 2;
    /** Below this HP percentage Doc stops chasing and stays close. */
    public static final int CAUTIOUS_HP_PERCENT = 30;

    private CompanionAi() {
    }

    /**
     * Runs one of Doc's turns.
     *
     * @param doc the companion
     * @param session the game
     * @return ticks used
     */
    public static int takeTurn(Companion doc, GameSession session) {
        int cost = doc.actionCost();
        boolean eligibleAtStart = ActionRules.canAct(doc);
        Floor floor = session.floor();
        doc.syncLevel(session.player().level());
        session.reportEffects(doc, doc.effects().tick(doc, floor.terrainAt(doc.position())));
        if (!doc.isAlive()) {
            session.companionKnockedOut();
            return cost;
        }
        doc.regenerate();
        if (!session.actionRules().canActAfterEffects(doc, eligibleAtStart)) {
            return cost;
        }
        Monster adjacent = adjacentEnemy(doc, session);
        if (adjacent != null) {
            Combat.attack(session, doc, adjacent, false);
            return cost;
        }
        boolean healthy = doc.hp() * 100 >= doc.maxHp() * CAUTIOUS_HP_PERCENT;
        Monster target = healthy ? nearestAwakeEnemy(doc, session) : null;
        if (target != null) {
            step(doc, session, target.position());
        } else if (doc.position().distanceTo(session.player().position()) > FOLLOW_DISTANCE) {
            step(doc, session, session.player().position());
        }
        return cost;
    }

    private static Monster adjacentEnemy(Companion doc, GameSession session) {
        for (Monster m : session.monsters()) {
            if (m.position().isAdjacentTo(doc.position()) && !m.type().has(MonsterTrait.STATIONARY)) {
                Direction d = Direction.toward(doc.position(), m.position()).orElseThrow();
                if (session.floor().canStep(doc.position(), d, false)) {
                    return m;
                }
            }
        }
        return null;
    }

    private static Monster nearestAwakeEnemy(Companion doc, GameSession session) {
        Monster best = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Monster m : session.monsters()) {
            int d = doc.position().distanceTo(m.position());
            if (!m.isAsleep() && d <= HUNT_RANGE && d < bestDistance
                    && session.currentFloor().isVisible(m.position())) {
                best = m;
                bestDistance = d;
            }
        }
        return best;
    }

    private static void step(Companion doc, GameSession session, Position goal) {
        Floor floor = session.floor();
        List<Position> path = Pathfinder.findPath(floor, doc.position(), goal, true,
                p -> session.isOccupied(p) && !p.equals(goal));
        if (path.isEmpty()) {
            return;
        }
        Position next = path.get(0);
        if (session.isOccupied(next)) {
            return;
        }
        if (floor.terrainAt(next) == Terrain.DOOR_CLOSED) {
            floor.setTerrain(next, Terrain.DOOR_OPEN);
            return;
        }
        session.moveCreature(doc, next);
    }
}
