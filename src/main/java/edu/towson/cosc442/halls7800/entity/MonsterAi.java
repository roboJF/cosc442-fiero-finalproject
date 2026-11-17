package edu.towson.cosc442.halls7800.entity;

import edu.towson.cosc442.halls7800.combat.Combat;
import edu.towson.cosc442.halls7800.effect.EffectType;
import edu.towson.cosc442.halls7800.engine.ActionRules;
import edu.towson.cosc442.halls7800.engine.GameEvent;
import edu.towson.cosc442.halls7800.engine.GameSession;
import edu.towson.cosc442.halls7800.util.RandomStream;
import edu.towson.cosc442.halls7800.util.Rng;
import edu.towson.cosc442.halls7800.world.Direction;
import edu.towson.cosc442.halls7800.world.Floor;
import edu.towson.cosc442.halls7800.world.LineOfSight;
import edu.towson.cosc442.halls7800.world.Pathfinder;
import edu.towson.cosc442.halls7800.world.Position;
import edu.towson.cosc442.halls7800.world.Room;
import edu.towson.cosc442.halls7800.world.Terrain;
import java.util.ArrayList;
import java.util.List;

/**
 * Monster decision making: a small state machine per monster.
 *
 * <pre>
 *   SLEEPING --notices player--> HUNTING
 *   WANDERING --sees player--> HUNTING
 *   HUNTING --loses track of player--> WANDERING
 *   HUNTING --badly hurt (FLEES trait)--> FLEEING
 *   FLEEING --recovered or cornered too long--> HUNTING
 * </pre>
 *
 * <p>All random choices draw from the session's {@link RandomStream#AI} stream.
 */
public final class MonsterAi {

    /** How far monsters can notice the player. */
    public static final int PERCEPTION_RANGE = 8;
    /** Percent chance per turn that a sleeping monster within range wakes up. */
    public static final int WAKE_PERCENT = 25;
    /** Wake chance when the player is within two tiles. */
    public static final int WAKE_PERCENT_CLOSE = 60;
    /** Percent chance an erratic monster moves randomly. */
    public static final int ERRATIC_PERCENT = 35;
    /** Percent chance a confused creature moves randomly. */
    public static final int CONFUSED_PERCENT = 50;
    /** HP fraction (percent) at or below which a FLEES monster runs. */
    public static final int FLEE_HP_PERCENT = 30;
    /** HP fraction (percent) at which a fleeing monster turns to fight again. */
    public static final int RECOVER_HP_PERCENT = 60;
    /** Turns a monster will flee before giving up and fighting. */
    public static final int MAX_FLEE_TURNS = 12;
    /** Percent chance per turn a regenerating monster heals 1 HP. */
    public static final int REGEN_PERCENT = 35;

    private MonsterAi() {
    }

    /**
     * Runs one action for a monster.
     *
     * @param monster the acting monster
     * @param session the game
     * @return ticks the action took
     */
    public static int takeTurn(Monster monster, GameSession session) {
        int cost = monster.actionCost();
        boolean eligibleAtStart = ActionRules.canAct(monster);
        Rng rng = session.rng(RandomStream.AI);
        Floor floor = session.floor();

        session.reportEffects(monster, monster.effects().tick(monster, floor.terrainAt(monster.position())));
        if (!monster.isAlive()) {
            session.killMonster(monster, null);
            return cost;
        }
        if (monster.type().has(MonsterTrait.REGENERATES) && monster.hp() < monster.maxHp()
                && rng.percent(REGEN_PERCENT)) {
            monster.heal(1);
        }
        if (monster.type().has(MonsterTrait.STATIONARY)) {
            return cost;
        }
        boolean canAct = session.actionRules().canActAfterEffects(monster, eligibleAtStart);
        if (monster.isBoss() && (canAct || session.actionRules() == ActionRules.LEGACY)) {
            BossAi.checkPhases(monster, session);
        }
        if (!canAct) {
            return cost;
        }

        Player player = session.player();
        boolean seesPlayer = canSee(monster, player, floor);
        switch (monster.state()) {
            case SLEEPING -> {
                if (seesPlayer && wakes(monster, player, rng)) {
                    monster.setState(Monster.State.HUNTING);
                    monster.setLastKnownPlayerPosition(player.position());
                    session.messageIfVisible(monster.position(), monster.subject() + " " + monster.verb("notice") + " you!");
                    if (monster.isBoss()) {
                        session.signal(GameEvent.Type.BOSS_AWAKENED, monster.position());
                    }
                }
            }
            case WANDERING -> {
                if (seesPlayer) {
                    monster.setState(Monster.State.HUNTING);
                    monster.setLastKnownPlayerPosition(player.position());
                    hunt(monster, session, rng, true);
                } else {
                    wander(monster, session, rng);
                }
            }
            case HUNTING -> {
                if (seesPlayer) {
                    monster.setLastKnownPlayerPosition(player.position());
                }
                if (shouldFlee(monster)) {
                    monster.setState(Monster.State.FLEEING);
                    session.messageIfVisible(monster.position(), monster.subject() + " " + monster.verb("turn") + " to flee!");
                    flee(monster, session, rng);
                } else {
                    hunt(monster, session, rng, seesPlayer);
                }
            }
            case FLEEING -> {
                monster.countFleeingTurn();
                int hpPercent = monster.hp() * 100 / monster.maxHp();
                if (monster.fearTurns() > 0) {
                    monster.countDownFear();
                    flee(monster, session, rng);
                } else if (hpPercent >= RECOVER_HP_PERCENT || monster.turnsFleeing() > MAX_FLEE_TURNS) {
                    monster.setState(Monster.State.HUNTING);
                    hunt(monster, session, rng, seesPlayer);
                } else {
                    flee(monster, session, rng);
                }
            }
            default -> throw new IllegalStateException("Unknown state " + monster.state());
        }
        return cost;
    }

    /**
     * Whether a monster can currently see the player: within perception range and with a clear
     * line between them.
     *
     * @param monster the monster
     * @param player the player
     * @param floor the floor
     * @return {@code true} if the player is visible to the monster
     */
    public static boolean canSee(Monster monster, Player player, Floor floor) {
        Position from = monster.position();
        Position to = player.position();
        return from.distanceTo(to) <= PERCEPTION_RANGE && LineOfSight.isClear(floor, from, to);
    }

    private static boolean wakes(Monster monster, Player player, Rng rng) {
        int distance = monster.position().distanceTo(player.position());
        if (monster.isBoss()) {
            return true;
        }
        if (monster.type().has(MonsterTrait.AMBUSH)) {
            return distance <= 1;
        }
        return rng.percent(distance <= 2 ? WAKE_PERCENT_CLOSE : WAKE_PERCENT);
    }

    private static boolean shouldFlee(Monster monster) {
        return monster.type().has(MonsterTrait.FLEES) && monster.hp() * 100 / monster.maxHp() <= FLEE_HP_PERCENT;
    }

    private static void hunt(Monster monster, GameSession session, Rng rng, boolean seesPlayer) {
        Floor floor = session.floor();
        Player player = session.player();
        Position here = monster.position();
        int distance = here.distanceTo(player.position());

        if (seesPlayer && distance == 1 && canStrike(floor, here, player.position())) {
            Combat.attack(session, monster, player, false);
            return;
        }
        Companion doc = session.companion();
        if (doc != null && here.isAdjacentTo(doc.position()) && canStrike(floor, here, doc.position())) {
            Combat.attack(session, monster, doc, false);
            return;
        }
        if (seesPlayer && monster.type().has(MonsterTrait.RANGED) && distance > 1
                && distance <= monster.type().range()) {
            Combat.attack(session, monster, player, true);
            return;
        }
        if (randomlyStumbles(monster, rng)) {
            stepRandomly(monster, session, rng);
            return;
        }
        Position target = monster.lastKnownPlayerPosition();
        if (target == null) {
            monster.setState(Monster.State.WANDERING);
            return;
        }
        if (here.equals(target) && !seesPlayer) {
            monster.setLastKnownPlayerPosition(null);
            monster.setState(Monster.State.WANDERING);
            return;
        }
        stepToward(monster, session, target);
    }

    private static void wander(Monster monster, GameSession session, Rng rng) {
        if (randomlyStumbles(monster, rng)) {
            stepRandomly(monster, session, rng);
            return;
        }
        Position target = monster.wanderTarget();
        if (target == null || target.equals(monster.position())) {
            target = pickWanderTarget(session.floor(), rng);
            monster.setWanderTarget(target);
        }
        if (target != null && !stepToward(monster, session, target)) {
            monster.setWanderTarget(null);
        }
    }

    private static Position pickWanderTarget(Floor floor, Rng rng) {
        List<Room> rooms = floor.rooms();
        if (!rooms.isEmpty()) {
            Room room = rng.pick(rooms);
            if (room.role() != Room.Role.VAULT && room.role() != Room.Role.HIDDEN) {
                Position p = rng.pick(room.interior());
                if (floor.isPassable(p)) {
                    return p;
                }
            }
            return null;
        }
        List<Position> open = floor.passablePositions();
        return open.isEmpty() ? null : rng.pick(open);
    }

    /** Moves to the neighboring tile that is farthest (by walking distance) from the player. */
    private static void flee(Monster monster, GameSession session, Rng rng) {
        Floor floor = session.floor();
        Position here = monster.position();
        Position playerPos = session.player().position();
        int[][] dist = Pathfinder.distanceMap(floor, playerPos, 20, !monster.type().has(MonsterTrait.NO_DOORS));
        int current = dist[here.y()][here.x()];
        Direction best = null;
        int bestDistance = current == Pathfinder.UNREACHABLE ? Integer.MIN_VALUE : current;
        for (Direction d : Direction.values()) {
            Position next = here.step(d);
            if (!floor.canStep(here, d, false) || session.isOccupied(next)) {
                continue;
            }
            int nd = dist[next.y()][next.x()];
            if (nd != Pathfinder.UNREACHABLE && nd > bestDistance) {
                bestDistance = nd;
                best = d;
            }
        }
        if (best != null) {
            session.moveCreature(monster, here.step(best));
        } else if (here.isAdjacentTo(playerPos) && canStrike(floor, here, playerPos)) {
            Combat.attack(session, monster, session.player(), false);
        }
    }

    private static boolean randomlyStumbles(Monster monster, Rng rng) {
        return (monster.effects().has(EffectType.CONFUSED) && rng.percent(CONFUSED_PERCENT))
                || (monster.type().has(MonsterTrait.ERRATIC) && rng.percent(ERRATIC_PERCENT));
    }

    private static void stepRandomly(Monster monster, GameSession session, Rng rng) {
        Floor floor = session.floor();
        List<Direction> options = new ArrayList<>();
        for (Direction d : Direction.values()) {
            Position next = monster.position().step(d);
            if (floor.canStep(monster.position(), d, false) && !session.isOccupied(next)) {
                options.add(d);
            }
        }
        if (!options.isEmpty()) {
            session.moveCreature(monster, monster.position().step(rng.pick(options)));
        }
    }

    /**
     * Takes one step along a path toward the target, opening a closed door if one is in the way.
     *
     * @return {@code false} if no step was possible
     */
    private static boolean stepToward(Monster monster, GameSession session, Position target) {
        Floor floor = session.floor();
        boolean opensDoors = !monster.type().has(MonsterTrait.NO_DOORS);
        Position here = monster.position();
        List<Position> path = Pathfinder.findPath(floor, here, target, opensDoors,
                p -> session.monsterAt(p) != null || session.npcAt(p) != null);
        if (path.isEmpty()) {
            return false;
        }
        Position next = path.get(0);
        if (session.isOccupied(next)) {
            return false;
        }
        if (floor.terrainAt(next) == Terrain.DOOR_CLOSED) {
            floor.setTerrain(next, Terrain.DOOR_OPEN);
            session.messageIfVisible(next, "A door swings open.");
            return true;
        }
        session.moveCreature(monster, next);
        return true;
    }

    private static boolean canStrike(Floor floor, Position from, Position to) {
        return Direction.toward(from, to).map(d -> floor.canStep(from, d, false)).orElse(false);
    }
}
